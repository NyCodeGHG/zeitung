package dev.nycode.build

import dev.nycode.version.Version
import dev.nycode.version.VersionGroup
import kotlinx.coroutines.flow.Flow
import org.litote.kmongo.Id

interface BuildService {
    suspend fun findById(id: Id<Build>): Build?

    fun findByVersion(versionId: Id<Version>): Flow<Build>

    suspend fun findByVersionGroup(versionGroupId: Id<VersionGroup>): Flow<Build>

    suspend fun findByVersionAndNumber(versionId: Id<Version>, number: Int): Build?

    suspend fun createBuild(number: Int, versionId: Id<Version>, changes: List<Change>, download: BuildDownload): Build
}
