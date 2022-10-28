package utils

import com.github.fge.jackson.JsonLoader
import com.github.fge.jsonschema.core.report.ProcessingReport
import com.github.fge.jsonschema.main.JsonSchemaFactory

object SchemaValidator {

    private val schemaFactory = JsonSchemaFactory.byDefault()

    fun validate(json: String, schemaPath: String): ProcessingReport {
        val schemaPath = javaClass.classLoader.getResource(schemaPath).toURI().toString()
        return schemaFactory.getJsonSchema(schemaPath).validate(JsonLoader.fromString(json))
    }
}