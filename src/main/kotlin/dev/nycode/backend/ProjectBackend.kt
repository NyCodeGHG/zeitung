package dev.nycode.backend

import com.fasterxml.jackson.annotation.JsonTypeInfo
import dev.nycode.version.Version
import kotlinx.coroutines.flow.Flow

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
abstract class ProjectBackend<T : BuildData> {

    abstract fun retrieveBuilds(version: Version): Flow<T>

    abstract suspend fun retrieveBuild(version: Version, number: Int): T

}
