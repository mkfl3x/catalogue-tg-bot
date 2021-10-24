package utils

import com.worldturner.medeia.api.UrlSchemaSource
import com.worldturner.medeia.api.ValidationFailedException
import com.worldturner.medeia.api.gson.MedeiaGsonApi
import keyboards.Schemas
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

object SchemaValidator {

    private val schemaApi = MedeiaGsonApi()

    fun isValid(json: String, schema: Schemas): Boolean {
        try {
            val loadedSchema = schemaApi.loadSchema(UrlSchemaSource(javaClass.getResource(schema.path)))
            val targetJson = InputStreamReader(json.byteInputStream(StandardCharsets.UTF_8))
            val validator = schemaApi.createJsonReader(loadedSchema, targetJson)
            schemaApi.parseAll(validator)
        } catch (e: ValidationFailedException) {
            // TODO: print error to log
            return false
        }
        // TODO: add positive log
        return true
    }
}