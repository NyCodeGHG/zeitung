package dev.nycode.build

interface BuildSource {

    /**
     * Retrieves a build from an external source (e.g. CI Server) with the given project name and build number.
     *
     * @param project the project the build belongs to
     * @param number the number of the build
     */
    fun retrieveBuild(project: String, number: Int): Build

}
