package dev.nycode.project

import dev.nycode.project.responses.ProjectResponse
import dev.nycode.project.responses.ProjectsResponse
import dev.nycode.project.responses.VersionGroupResponse
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

@Controller("/projects", produces = [MediaType.APPLICATION_JSON])
class ProjectController(
    private val projectService: ProjectService,
    private val versionService: VersionService,
    private val groupService: VersionGroupService
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
}
