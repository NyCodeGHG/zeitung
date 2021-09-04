package dev.nycode.project.responses

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import dev.nycode.build.Change
import java.time.Instant

class BuildResponse(
    @JsonProperty("project_id")
    val projectId: String,
    @JsonProperty("project_name")
    val projectName: String,
    val version: String,
    val build: Int,
    val time: Instant,
    @JsonInclude
    val changes: List<Change>,
    val downloads: Download
)
