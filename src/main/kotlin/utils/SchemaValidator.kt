package utils

import com.github.fge.jackson.JsonLoader
import com.github.fge.jsonschema.core.report.ProcessingReport
import com.github.fge.jsonschema.main.JsonSchemaFactory
import server.Schemas

object SchemaValidator {

    private val schemaFactory = JsonSchemaFactory.byDefault()

    fun validate(json: String, schema: Schemas): ProcessingReport {
        val schemaPath = javaClass.classLoader.getResource(schema.path).toURI().toString()
        return schemaFactory.getJsonSchema(schemaPath)
            .validate(JsonLoader.fromString(json))
    }
}