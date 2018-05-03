package nl.molnet.ta.web

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class Position @JsonCreator(mode = JsonCreator.Mode.DEFAULT) constructor(
        @JsonProperty("x")
        val x: Long,
        @JsonProperty("y")
        val y: Long,
        @JsonProperty("color")
        val color: String,
        @JsonProperty("clientId")
        val clientId: String)
