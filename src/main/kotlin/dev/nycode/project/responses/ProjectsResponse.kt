package dev.nycode.project.responses

import com.fasterxml.jackson.annotation.JsonInclude

class ProjectsResponse(@JsonInclude val projects: List<String>)
