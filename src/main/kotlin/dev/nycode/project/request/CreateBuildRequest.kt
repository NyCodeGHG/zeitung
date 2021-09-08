package dev.nycode.project.request

import dev.nycode.build.BuildDownload
import dev.nycode.build.Change

class CreateBuildRequest(
    val number: Int,
    val changes: List<Change>,
    val download: BuildDownload
)
