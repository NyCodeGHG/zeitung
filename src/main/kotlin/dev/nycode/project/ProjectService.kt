package dev.nycode.project

import io.micronaut.context.annotation.Property
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.runBlocking
import org.litote.kmongo.Id
import org.litote.kmongo.coroutine.CoroutineClient
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.eq
import org.litote.kmongo.newId
import org.litote.kmongo.util.KMongoUtil

@Singleton
class ProjectService(
    client: CoroutineClient,
    @Property(name = "mongodb.database") database: String
) {

    private val collection: CoroutineCollection<Project> by lazy {
        client
            .getDatabase(database)
            .database
            .getCollection(KMongoUtil.defaultCollectionName(Project::class), Project::class.java)
            .coroutine
    }

    init {
        runBlocking {
            collection.ensureUniqueIndex(Project::name, Project::friendlyName)
        }
    }

    suspend fun findById(id: Id<Project>): Project? = collection.findOneById(id)

    suspend fun findByName(name: String): Project? = collection.findOne(Project::name eq name)

    fun find(): Flow<Project> = collection.find().toFlow()

    suspend fun createProject(name: String, friendlyName: String): Project {
        val project = Project(newId(), name, friendlyName)
        collection.save(project)
        return project
    }

}
