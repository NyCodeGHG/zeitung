package dev.nycode.project.responses

import com.fasterxml.jackson.annotation.JsonInclude

class ProjectResponse(@JsonInclude val projects: List<String>)
