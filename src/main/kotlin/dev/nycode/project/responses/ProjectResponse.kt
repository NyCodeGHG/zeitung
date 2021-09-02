package dev.nycode.project.responses

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

class ProjectResponse(
    @JsonProperty("project_id")
    val projectId: String,
    @JsonProperty("project_name")
    val projectName: String,
    @JsonProperty("version_groups")
    @JsonInclude
    val versionGroups: List<String>,
    @JsonInclude
    val versions: List<String>
)
