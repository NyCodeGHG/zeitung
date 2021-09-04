package dev.nycode.project.responses

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import dev.nycode.build.Change
import java.time.Instant

class VersionGroupBuildsResponse(
    @JsonProperty("project_id")
    val projectId: String,
    @JsonProperty("project_name")
    val projectName: String,
    @JsonProperty("version_group")
    val versionGroup: String,
    @JsonInclude
    val versions: List<String>,
    @JsonInclude
    val builds: List<VersionGroupBuild>
)

class VersionGroupBuild(
    val build: Int,
    val time: Instant,
    @JsonInclude
    val changes: List<Change>,
    val downloads: Download
)

class DownloadSerializer : JsonSerializer<Download>() {
    override fun serialize(value: Download, gen: JsonGenerator, serializers: SerializerProvider) {
        gen.writeStartObject()
        gen.writeObjectFieldStart("application")
        gen.writeStringField("name", value.name)
        gen.writeStringField("sha256", value.sha256)
        gen.writeEndObject()
        gen.writeEndObject()
    }
}

@JsonSerialize(using = DownloadSerializer::class)
class Download(
    val name: String,
    val sha256: String
)
