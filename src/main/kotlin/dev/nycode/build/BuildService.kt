package dev.nycode.build

import kotlinx.coroutines.flow.Flow

interface BuildService {

    /**
     * Retrieves a build from an external source (e.g. CI Server) with the given project name, build number and version.
     *
     * @param projectName the project the build belongs to
     * @param versionName the build's version
     * @param number the number of the build
     */
    suspend fun retrieveBuild(projectName: String, versionName: String, number: Int): Build

    /**
     * Retrieves all builds from an external source (e.g. CI Server) from the given project and version.
     */
    suspend fun retrieveBuilds(projectName: String, versionName: String): Flow<Build>

}
