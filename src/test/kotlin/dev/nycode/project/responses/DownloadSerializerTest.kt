package dev.nycode.project.responses

import com.fasterxml.jackson.databind.ObjectMapper
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

@MicronautTest
internal class DownloadSerializerTest(private val mapper: ObjectMapper) {

    @Test
    fun serialize() {
        val name = "paper-1.16.4-274.jar"
        val hash = "a167fddcb40d50d1e8c913ed83bc21365691f0c006d51a38e17535fa6ecf2e53"
        val download = Download(name, hash)
        val json = mapper.writeValueAsString(download)
        assertEquals(
            """
            {"application":{"name":"$name","sha256":"$hash"}}
        """.trimIndent(), json
        )
    }
}
