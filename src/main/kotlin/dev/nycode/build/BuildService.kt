package dev.nycode.build

import kotlinx.coroutines.flow.Flow

interface BuildService {

    /**
     * Retrieves a build from an external source (e.g. CI Server) with the given project name and build number.
     *
     * @param project the project the build belongs to
     * @param number the number of the build
     */
    suspend fun retrieveBuild(project: String, number: Int): Build

    /**
     * Retrieves all builds from an external source (e.g. CI Server) from the given project and version.
     */
    fun retrieveBuilds(project: String, version: String): Flow<Build>

}
