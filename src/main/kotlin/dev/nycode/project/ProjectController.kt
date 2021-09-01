package dev.nycode.project

import dev.nycode.project.responses.ProjectResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList

@Controller("/projects", produces = [MediaType.APPLICATION_JSON])
class ProjectController(private val service: ProjectService) {

    @Get
    suspend fun listProjects(): ProjectResponse {
        val projects = service.find().map { it.name }.toList()
        return ProjectResponse(projects)
    }
}
