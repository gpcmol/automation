package nl.molnet.ta.web

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class WebResult @JsonCreator(mode = JsonCreator.Mode.DEFAULT) constructor(
        @JsonProperty("result")
        val result: String)
