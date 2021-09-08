package dev.nycode.version

import dev.nycode.project.Project
import dev.nycode.project.VersionAlreadyExistsException
import io.micronaut.context.annotation.Property
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.Flow
import org.litote.kmongo.*
import org.litote.kmongo.coroutine.CoroutineClient
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.util.KMongoUtil

@Singleton
class VersionService(
    client: CoroutineClient,
    @Property(name = "mongodb.database") database: String
) {

    private val collection: CoroutineCollection<Version> by lazy {
        client
            .getDatabase(database)
            .database
            .getCollection(KMongoUtil.defaultCollectionName(Version::class), Version::class.java)
            .coroutine
    }

    suspend fun findById(id: Id<Version>): Version? = collection.findOneById(id)

    suspend fun findByNameAndProject(name: String, projectId: Id<Project>): Version? =
        collection.findOne(and(Version::name eq name, Version::projectId eq projectId))

    fun findByProject(projectId: Id<Project>): Flow<Version> =
        collection.find(Version::projectId eq projectId).toFlow()

    fun findByGroupId(groupId: Id<VersionGroup>): Flow<Version> =
        collection.find(Version::groupId eq groupId).toFlow()

    suspend fun createVersion(name: String, projectId: Id<Project>, versionGroupId: Id<VersionGroup>): Version {
        if (collection.findOne(and(Version::projectId eq projectId, Version::name eq name)) != null) {
            throw VersionAlreadyExistsException()
        }
        val version = Version(newId(), projectId, versionGroupId, name)
        collection.save(version)
        return version
    }
}
