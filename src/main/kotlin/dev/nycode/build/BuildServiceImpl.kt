package dev.nycode.build

import dev.nycode.project.Project
import dev.nycode.project.ProjectNotFoundException
import dev.nycode.project.ProjectService
import dev.nycode.project.VersionNotFoundException
import dev.nycode.version.Version
import dev.nycode.version.VersionService
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class BuildServiceImpl(
    private val projectService: ProjectService,
    private val versionService: VersionService
) : BuildService {

    override suspend fun retrieveBuild(projectName: String, versionName: String, number: Int): Build {
        val (project, version) = retrieveProjectAndVersion(projectName, versionName)
        return project.backend.retrieveBuild(version, number).toBuild(project.id, version.id)
    }

    override suspend fun retrieveBuilds(projectName: String, versionName: String): Flow<Build> {
        val (project, version) = retrieveProjectAndVersion(projectName, versionName)
        return project.backend.retrieveBuilds(version).map { it.toBuild(project.id, version.id) }
    }

    private suspend fun retrieveProjectAndVersion(
        projectName: String,
        versionName: String
    ): Pair<Project, Version> {
        val project = projectService.findByName(projectName)
            ?: throw ProjectNotFoundException()
        val version = versionService.findByNameAndProject(versionName, project.id)
            ?: throw VersionNotFoundException()
        return Pair(project, version)
    }
}
