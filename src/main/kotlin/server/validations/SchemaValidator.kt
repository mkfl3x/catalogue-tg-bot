package server.validations

import com.github.fge.jackson.JsonLoader
import com.github.fge.jsonschema.main.JsonSchemaFactory

object SchemaValidator {

    private val schemaFactory = JsonSchemaFactory.byDefault()

    @Throws(RequestValidationException::class)
    fun validate(json: String, schemaPath: String) {
        schemaFactory.getJsonSchema(javaClass.classLoader.getResource(schemaPath).toURI().toString())
            .validate(JsonLoader.fromString(json)).apply {
                if (this.isSuccess.not()) {
                    val error = this.toString().split("\n")
                        .first { it.startsWith("error:") }
                        .replace("error: ", "")
                    throw RequestValidationException("Request body is not valid: $error")
                }
            }
    }
}