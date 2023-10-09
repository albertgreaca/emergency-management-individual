package de.unisaarland.cs.se.selab.util

import org.everit.json.schema.Schema
import org.everit.json.schema.loader.SchemaClient
import org.everit.json.schema.loader.SchemaLoader
import org.json.JSONObject
import org.slf4j.LoggerFactory
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.stream.Collectors

/**
 * Loads a resource from the classpath.
 */
fun loadResource(subclass: Class<*>, name: String?): Result<String> {
    LoggerFactory.getLogger(subclass)
        .trace("loading {}", subclass.classLoader.getResource(name))
    try {
        InputStreamReader(
            Objects.requireNonNull(subclass.classLoader.getResourceAsStream(name)),
            StandardCharsets.UTF_8
        ).use { input ->
            BufferedReader(input).use { reader ->
                return Result.success(reader.lines().collect(Collectors.joining("\n")))
            }
        }
    } catch (e: IOException) {
        LoggerFactory.getLogger(subclass).error("{}", e.message, e)
        return Result.failure("File $name not present")
    }
}

/**
 * Loads a schema from the classpath.
 */
fun getSchema(subclass: Class<*>, name: String): Schema? {
    return SchemaLoader.builder().schemaClient(SchemaClient.classPathAwareClient()).schemaJson(
        JSONObject(
            Objects.requireNonNull<String>(
                loadResource(
                    subclass,
                    "schema/$name"
                ).getOrThrow(IllegalArgumentException("schema location invalid"))
            )
        )
    )
        .resolutionScope("classpath://schema/").build().load().build()
}
