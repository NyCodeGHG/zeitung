package dev.nycode.version

import dev.nycode.project.Project
import org.bson.codecs.pojo.annotations.BsonId
import org.litote.kmongo.Id

data class VersionGroup(
    @BsonId
    val id: Id<VersionGroup>,
    val projectId: Id<Project>,
    val name: String
) : Comparable<VersionGroup> {
    override fun compareTo(other: VersionGroup): Int {
        return this.name.compareTo(other.name)
    }
}
