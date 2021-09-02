package dev.nycode.project

import dev.nycode.backend.ProjectBackend
import org.bson.codecs.pojo.annotations.BsonId
import org.litote.kmongo.Id

data class Project(
    @BsonId
    val id: Id<Project>,
    val name: String,
    val friendlyName: String,
    val backend: ProjectBackend<*>
)
