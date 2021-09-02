package dev.nycode.version

import dev.nycode.project.Project
import io.micronaut.context.annotation.Property
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.Flow
import org.litote.kmongo.Id
import org.litote.kmongo.and
import org.litote.kmongo.coroutine.CoroutineClient
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.eq
import org.litote.kmongo.util.KMongoUtil

@Singleton
class VersionGroupService(
    client: CoroutineClient,
    @Property(name = "mongodb.database") database: String
) {

    private val collection: CoroutineCollection<VersionGroup> by lazy {
        client
            .getDatabase(database)
            .database
            .getCollection(KMongoUtil.defaultCollectionName(VersionGroup::class), VersionGroup::class.java)
            .coroutine
    }

    suspend fun findById(id: Id<VersionGroup>): VersionGroup? = collection.findOneById(id)

    suspend fun findByNameAndProject(name: String, projectId: Id<Project>): VersionGroup? =
        collection.findOne(and(VersionGroup::name eq name, VersionGroup::projectId eq projectId))

    fun findByProject(projectId: Id<Project>): Flow<VersionGroup> =
        collection.find(VersionGroup::projectId eq projectId).toFlow()
}
