package utils

import com.github.fge.jackson.JsonLoader
import com.github.fge.jsonschema.main.JsonSchemaFactory
import server.RequestSchemas

object SchemaValidator {

    private val schemaFactory = JsonSchemaFactory.byDefault()

    fun isValid(json: String, schema: RequestSchemas): Boolean {
        val schemaPath = javaClass.classLoader.getResource(schema.path).toURI().toString()
        val report = schemaFactory.getJsonSchema(schemaPath)
            .validate(JsonLoader.fromString(json))
        return report.isSuccess
    }
}