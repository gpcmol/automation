package nl.molnet.ta

import nl.molnet.ta.web.Position
import nl.molnet.ta.web.WebResult
import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import io.vertx.core.eventbus.Message
import io.vertx.core.http.HttpMethod
import io.vertx.core.json.Json
import io.vertx.core.json.JsonObject
import io.vertx.ext.bridge.BridgeEventType
import io.vertx.ext.bridge.PermittedOptions
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.CorsHandler
import io.vertx.ext.web.handler.StaticHandler
import io.vertx.ext.web.handler.sockjs.BridgeOptions
import io.vertx.ext.web.handler.sockjs.SockJSHandler
import org.apache.log4j.LogManager

class MainVerticle : AbstractVerticle() {

    var logger = LogManager.getLogger(MainVerticle::class.java.getName())

    @Override
    override fun start(done: Future<Void>) {
        val router = Router.router(vertx)
        router.route().handler(CorsHandler.create("*")
                .allowedMethod(HttpMethod.GET)
                .allowedMethod(HttpMethod.POST)
                .allowedMethod(HttpMethod.PUT)
                .allowedMethod(HttpMethod.OPTIONS)
                .allowedHeader("X-PINGARUNER")
                .allowedHeader("Content-Type"))

        api(router)
        eventBus(router)

        router.route("/static/*").handler(StaticHandler.create().setCachingEnabled(false))

        vertx.createHttpServer().requestHandler(router::accept).listen(8080)

        done.complete()
    }

    private fun api(router: Router) {
        router.get("/api/test").produces("application/json").handler { context ->
            if (context.failed()) {
                context.response().setStatusCode(500).end(Json.encodePrettily(WebResult("test not ack")))
            } else {
                context.response().end(Json.encodePrettily(WebResult("test ack")))
            }
        }

        router.get("/api/register/:clientId").produces("application/json").handler { context ->
            val clientId = context.request().getParam("clientId")

            this.registerSocketClient(clientId)

            context.response().end(Json.encodePrettily(WebResult(clientId)))
        }
    }

    val connectedClients = mutableMapOf<String, String>()

    fun eventBus(router: Router) {
        val options = BridgeOptions()
            .addInboundPermitted(PermittedOptions().setAddressRegex(Constants.SOCKET_CHANNEL_REGEX)) // to backend
            .addOutboundPermitted(PermittedOptions().setAddressRegex(Constants.SOCKET_CHANNEL_REGEX)) // to browser

        val eventbusHandler = SockJSHandler.create(vertx).bridge(options, {
            if (it.type() == BridgeEventType.SOCKET_CREATED) {
                println("A socket was created")
            }
            it.complete(true)
        })

        router.route("/eventbus/*").handler(eventbusHandler)
    }

    fun registerSocketClient(clientId: String) {
        connectedClients.put(clientId, clientId)
        vertx.eventBus().consumer<JsonObject>("c.id.${clientId}") { message -> consumeClientMessages(message) }
    }

    fun consumeClientMessages(msg: Message<JsonObject>) {
        try {
            val eventClient = msg.body()
            val x = eventClient.getLong("x")
            val y = eventClient.getLong("y")
            val color = eventClient.getString("color")
            val clientId = eventClient.getString("clientId")

            val position = Position(x, y, color)

            val clientAddress = msg.address()

            println("clientAddress=$clientAddress, x=$x, y=$y, color=$color, clientId=$clientId")

            this.broadcastToClients(clientId, position)

            msg.reply(position)
        } catch (e: Exception) {
            msg.fail(500, e.message)
        }
    }

    fun broadcastToClients(clientId: String, position: Position) {
        for (clientItem in connectedClients) {
            val clientItemId = clientItem.key
            if (clientItemId == clientId) {
                continue
            }
            this.sendToClient(clientItemId, JsonObject(Json.encode(position)))
        }
    }

    fun sendToClient(clientId: String, message: JsonObject) {
        vertx.eventBus().send("c.id.${clientId}", message)
    }

}
