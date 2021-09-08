package dev.nycode.project

import dev.nycode.build.BuildService
import dev.nycode.project.request.CreateBuildRequest
import dev.nycode.project.request.CreateVersionRequest
import dev.nycode.project.responses.*
import dev.nycode.util.getLogger
import dev.nycode.version.Version
import dev.nycode.version.VersionGroup
import dev.nycode.version.VersionGroupService
import dev.nycode.version.VersionService
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.*
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.SecurityRule
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.litote.kmongo.newId
import java.net.URI
import java.security.Principal

@Controller("/projects", produces = [MediaType.APPLICATION_JSON])
class ProjectController(
    private val projectService: ProjectService,
    private val versionService: VersionService,
    private val groupService: VersionGroupService,
    private val buildService: BuildService
) {

    companion object {
        private val logger = getLogger<ProjectController>()
    }

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

    @Secured(SecurityRule.IS_AUTHENTICATED)
    @Put("/{project}/version_group/{versionGroupName}")
    suspend fun createVersionGroup(
        @PathVariable("project") projectName: String,
        versionGroupName: String,
        principal: Principal
    ): VersionGroupResponse {
        val project = projectService.findByName(projectName) ?: throw ProjectNotFoundException()
        val versionGroup = VersionGroup(newId(), project.id, versionGroupName)
        groupService.createVersionGroup(versionGroup)
        logger.info("User ${principal.name} created version group '${versionGroup.name}' in project ${project.name}")
        return VersionGroupResponse(project.name, project.friendlyName, versionGroup.name, emptyList())
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
        val (project, version) = findProjectAndVersion(projectName, versionName)
        val builds = buildService.findByVersion(version.id).map { it.number }.toList()
        return VersionResponse(project.name, project.friendlyName, version.name, builds)
    }

    @Secured(SecurityRule.IS_AUTHENTICATED)
    @Post("/{project}/versions")
    suspend fun createVersion(
        @PathVariable("project") projectName: String,
        @Body body: CreateVersionRequest,
        principal: Principal
    ): VersionResponse {
        val project = projectService.findByName(projectName) ?: throw ProjectNotFoundException()
        val versionGroup =
            groupService.findByNameAndProject(body.versionGroup, project.id) ?: throw VersionGroupNotFoundException()
        val version = versionService.createVersion(body.name, project.id, versionGroup.id)
        logger.info("User ${principal.name} created version '${version.name}' in project ${project.name}")
        return VersionResponse(project.name, project.friendlyName, version.name, emptyList())
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

    @Secured(SecurityRule.IS_AUTHENTICATED)
    @Post("/{project}/versions/{version}/builds")
    suspend fun createBuild(
        @PathVariable("project") projectName: String,
        @PathVariable("version") versionName: String,
        @Body body: CreateBuildRequest,
        principal: Principal
    ): BuildResponse {
        val (project, version) = findProjectAndVersion(projectName, versionName)
        val build = buildService.createBuild(body.number, version.id, body.changes, body.download)
        logger.info("User ${principal.name} created build '${build.number}' for version ${version.name} in project ${project.name}")
        return BuildResponse(
            project.name,
            project.friendlyName,
            version.name,
            build.number,
            build.time,
            build.changes,
            Download(build.download.fileName, build.download.sha256)
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
