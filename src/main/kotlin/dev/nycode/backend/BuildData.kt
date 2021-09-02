package dev.nycode.backend

import dev.nycode.build.Build
import dev.nycode.project.Project
import dev.nycode.version.Version
import org.litote.kmongo.Id

interface BuildData {

    fun toBuild(projectId: Id<Project>, versionId: Id<Version>): Build

}
