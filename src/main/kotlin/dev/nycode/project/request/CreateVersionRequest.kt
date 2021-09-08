package dev.nycode.project.request

import com.fasterxml.jackson.annotation.JsonProperty

class CreateVersionRequest(
    val name: String,
    @JsonProperty("version_group")
    val versionGroup: String
)
