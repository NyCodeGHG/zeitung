package dev.nycode.project

import org.bson.codecs.pojo.annotations.BsonId
import org.litote.kmongo.Id

data class Project(
    @BsonId
    val id: Id<Project>,
    val name: String,
    val friendlyName: String
)
