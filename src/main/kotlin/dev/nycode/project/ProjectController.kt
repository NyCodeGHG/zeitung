package dev.nycode.project

import dev.nycode.build.BuildService
import dev.nycode.build.Change
import dev.nycode.project.responses.*
import dev.nycode.version.VersionGroupService
import dev.nycode.version.VersionService
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import java.time.Instant

@Controller("/projects", produces = [MediaType.APPLICATION_JSON])
class ProjectController(
    private val projectService: ProjectService,
    private val versionService: VersionService,
    private val groupService: VersionGroupService,
    private val buildService: BuildService
) {

    @Get
    suspend fun listProjects(): ProjectsResponse {
        val projects = projectService.find().map { it.name }.toList()
        return ProjectsResponse(projects)
    }

    @Get("/{project}")
    suspend fun getProject(@PathVariable("project") name: String): ProjectResponse = coroutineScope {
        val project = projectService.findByName(name) ?: throw ProjectNotFoundException()
        val groups = async { groupService.findByProject(project.id).map { it.name }.toList() }
        val versions = async { versionService.findByProject(project.id).map { it.name }.toList() }
        ProjectResponse(project.name, project.friendlyName, groups.await(), versions.await())
    }

    @Get("/{project}/version_group/{versionGroupName}")
    suspend fun getVersionGroup(@PathVariable("project") name: String, versionGroupName: String): VersionGroupResponse {
        val project = projectService.findByName(name) ?: throw ProjectNotFoundException()
        val versionGroup =
            groupService.findByNameAndProject(versionGroupName, project.id) ?: throw VersionGroupNotFoundException()
        val versions = versionService.findByGroupId(versionGroup.id).map { it.name }.toList()
        return VersionGroupResponse(project.name, project.friendlyName, versionGroup.name, versions)
    }

    @Get("/{project}/version_group/{versionGroupName}/builds")
    suspend fun getVersionGroupBuilds(
        @PathVariable("project") name: String,
        versionGroupName: String
    ): VersionGroupBuildsResponse = coroutineScope {
        val project = projectService.findByName(name) ?: throw ProjectNotFoundException()
        val versionGroup =
            groupService.findByNameAndProject(versionGroupName, project.id) ?: throw VersionGroupNotFoundException()
        val versions = async { versionService.findByGroupId(versionGroup.id).map { it.name }.toList() }
        VersionGroupBuildsResponse(
            project.name, project.friendlyName, versionGroup.name, versions.await(), listOf(
                VersionGroupBuild(
                    1,
                    Instant.now(),
                    listOf(Change("Hello World", "Hello World", "Hello World")),
                    Download("paper.jar", "187")
                )
            )
        )
    }

    @Get("/{project}/versions/{version}")
    suspend fun getVersion(
        @PathVariable("project") projectName: String,
        @PathVariable("version") versionName: String
    ): VersionResponse {
        val project = projectService.findByName(projectName) ?: throw ProjectNotFoundException()
        val version = versionService.findByNameAndProject(versionName, project.id) ?: throw VersionNotFoundException()
        val builds = buildService.retrieveBuilds(project.name, version.name)
            .toList()
            .asSequence()
            .sortedBy { it.time }
            .map { it.number }
            .toList()
        return VersionResponse(project.name, project.friendlyName, version.name, builds)
    }
}
