package dev.nycode.build

import dev.nycode.project.BuildAlreadyExistsException
import dev.nycode.version.Version
import dev.nycode.version.VersionGroup
import dev.nycode.version.VersionService
import io.micronaut.context.annotation.Property
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.litote.kmongo.*
import org.litote.kmongo.coroutine.CoroutineClient
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.util.KMongoUtil
import java.time.Instant

@Singleton
class BuildServiceImpl(
    client: CoroutineClient,
    @Property(name = "mongodb.database") database: String,
    private val versionService: VersionService
) : BuildService {

    private val collection: CoroutineCollection<Build> by lazy {
        client.getDatabase(database)
            .database
            .getCollection(KMongoUtil.defaultCollectionName(Build::class), Build::class.java)
            .coroutine
    }

    override suspend fun findById(id: Id<Build>): Build? {
        return collection.findOneById(id)
    }

    override fun findByVersion(versionId: Id<Version>): Flow<Build> {
        return collection.find(Build::versionId eq versionId).toFlow()
    }

    override suspend fun findByVersionGroup(versionGroupId: Id<VersionGroup>): Flow<Build> {
        val versions = versionService.findByGroupId(versionGroupId).map { it.id }.toList()
        return collection.find(Build::versionId `in` versions).toFlow()
    }

    override suspend fun findByVersionAndNumber(versionId: Id<Version>, number: Int): Build? {
        return collection.findOne(and(Build::versionId eq versionId, Build::number eq number))
    }

    override suspend fun createBuild(
        number: Int,
        versionId: Id<Version>,
        changes: List<Change>,
        download: BuildDownload
    ): Build {
        if (collection.findOne(and(Build::number eq number, Build::versionId eq versionId)) != null) {
            throw BuildAlreadyExistsException()
        }

        val build = Build(newId(), versionId, number, Instant.now(), changes, download)
        collection.save(build)
        return build
    }
}
