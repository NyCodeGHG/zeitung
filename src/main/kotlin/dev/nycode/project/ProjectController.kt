package dev.nycode.project

import dev.nycode.build.BuildService
import dev.nycode.project.responses.*
import dev.nycode.version.Version
import dev.nycode.version.VersionGroupService
import dev.nycode.version.VersionService
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Error
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import java.net.URI
import java.net.URL

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
        val builds = async {
            buildService.findByVersionGroup(versionGroup.id)
                .map {
                    VersionGroupBuild(
                        it.number,
                        it.time,
                        it.changes,
                        Download(it.download.fileName, it.download.sha256)
                    )
                }
                .toList()
        }
        VersionGroupBuildsResponse(
            project.name,
            project.friendlyName,
            versionGroup.name,
            versions.await(),
            builds.await()
        )
    }

    @Get("/{project}/versions/{version}")
    suspend fun getVersion(
        @PathVariable("project") projectName: String,
        @PathVariable("version") versionName: String
    ): VersionResponse {
        val project = projectService.findByName(projectName) ?: throw ProjectNotFoundException()
        val version = versionService.findByNameAndProject(versionName, project.id) ?: throw VersionNotFoundException()
        val builds = buildService.findByVersion(version.id).map { it.number }.toList()
        return VersionResponse(project.name, project.friendlyName, version.name, builds)
    }

    @Get("/{project}/versions/{version}/builds/{build}")
    suspend fun getVersionBuild(
        @PathVariable("project") projectName: String,
        @PathVariable("version") versionName: String,
        @PathVariable("build") buildNumber: Int
    ): BuildResponse {
        val (project, version) = findProjectAndVersion(projectName, versionName)
        val build = buildService.findByVersionAndNumber(version.id, buildNumber) ?: throw BuildNotFoundException()
        val download = Download(build.download.fileName, build.download.sha256)
        return BuildResponse(
            project.name,
            project.friendlyName,
            version.name,
            build.number,
            build.time,
            build.changes,
            download
        )
    }

    @Get("/{project}/versions/{version}/builds/{build}/downloads/{download}")
    suspend fun downloadBuild(
        @PathVariable("project") projectName: String,
        @PathVariable("version") versionName: String,
        @PathVariable("build") buildNumber: Int,
        @PathVariable("download") fileName: String
    ): HttpResponse<Nothing> {
        val (_, version) = findProjectAndVersion(projectName, versionName)
        val build = buildService.findByVersionAndNumber(version.id, buildNumber) ?: throw BuildNotFoundException()
        return HttpResponse.redirect(URI.create(build.download.url))
    }

    private suspend fun findProjectAndVersion(
        projectName: String,
        versionName: String
    ): Pair<Project, Version> {
        val project = projectService.findByName(projectName) ?: throw ProjectNotFoundException()
        val version = versionService.findByNameAndProject(versionName, project.id) ?: throw VersionNotFoundException()
        return Pair(project, version)
    }

    @Error
    fun errorHandler(request: HttpRequest<*>, e: ZeitungException): HttpResponse<RequestError> {
        if (e.message == null) {
            return HttpResponse.serverError()
        }
        return HttpResponse.notFound(RequestError(e.message!!))
    }
}
