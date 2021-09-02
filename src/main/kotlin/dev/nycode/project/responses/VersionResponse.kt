package dev.nycode.project.responses

import com.fasterxml.jackson.annotation.JsonProperty

class VersionResponse(
    @JsonProperty("project_id")
    val projectId: String,
    @JsonProperty("project_name")
    val projectName: String,
    val version: String,
    val builds: List<Int>
)
