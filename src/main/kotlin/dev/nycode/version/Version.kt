package dev.nycode.version

import dev.nycode.project.Project
import org.bson.codecs.pojo.annotations.BsonId
import org.litote.kmongo.Id

data class Version(
    @BsonId
    val id: Id<Version>,
    val projectId: Id<Project>,
    val groupId: Id<VersionGroup>,
    val name: String
)
