package dev.nycode.project.request

import com.fasterxml.jackson.annotation.JsonProperty

class CreateProjectRequest(val name: String, @JsonProperty("friendly_name") val friendlyName: String)
