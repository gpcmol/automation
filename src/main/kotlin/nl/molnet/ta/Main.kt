package nl.molnet.ta

import io.vertx.core.AbstractVerticle
import io.vertx.core.DeploymentOptions
import io.vertx.core.Vertx
import org.apache.log4j.LogManager


object Main {

    var logger = LogManager.getLogger(MainVerticle::class.java.getName())

    @JvmStatic
    fun main(args: Array<String>) {
        println("Start app")

        var vertx = Vertx.vertx()

        deploy(vertx, MainVerticle::class.java, DeploymentOptions())
    }


    private fun deploy(vertx: Vertx, clazz: Class<out AbstractVerticle>, options: DeploymentOptions) {
        vertx.deployVerticle(clazz.name, options) { handler ->
            if (handler.succeeded()) {
                logger.debug("{${clazz.simpleName}} started successfully (deployment identifier: {${handler.result()}})")
            } else {
                logger.error("{${clazz.simpleName}} deployment failed due to: ${handler.cause()}")
            }
        }
    }

}
