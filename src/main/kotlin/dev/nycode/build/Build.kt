package dev.nycode.build

import dev.nycode.version.Version
import org.bson.codecs.pojo.annotations.BsonId
import org.litote.kmongo.Id
import java.time.Instant

data class Build(
    @BsonId
    val id: Id<Build>,
    val versionId: Id<Version>,
    val number: Int,
    val time: Instant,
    val changes: List<Change>,
    val download: BuildDownload
)

data class BuildDownload(
    val url: String,
    val fileName: String,
    val sha256: String
)
