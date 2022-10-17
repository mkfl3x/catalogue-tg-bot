package server.models.requests

import common.ReservedNames
import io.ktor.http.*
import server.models.Error
import server.models.Result
import utils.GsonMapper
import utils.SchemaValidator

enum class Schemas(val path: String) {
    ADD_KEYBOARD_REQUEST("json-schemas/models/requests/objects/keyboard.json"),
    ADD_BUTTON_REQUEST("json-schemas/models/requests/objects/button.json"),
    ADD_PAYLOAD_REQUEST("json-schemas/models/requests/objects/payload.json"),
    DELETE_KEYBOARD_REQUEST("json-schemas/models/requests/delete_keyboard_request.json"),
    DELETE_BUTTON_REQUEST("json-schemas/models/requests/delete_button_request.json"),
    DELETE_PAYLOAD_REQUEST("json-schemas/models/requests/delete_payload_request.json"),
    LINK_KEYBOARD_REQUEST("json-schemas/models/requests/link_button_request.json"),
    DETACH_KEYBOARD_REQUEST("json-schemas/models/requests/detach_keyboard_request.json")
}

abstract class Request {

    abstract val successMessage: String

    protected abstract val schema: Schemas

    abstract fun validateData(): Result?

    protected fun validateReservedNames(vararg names: String?): Result? {
        names.forEach { name ->
            if (ReservedNames.values().any { it.text == name })
                return Result(HttpStatusCode.BadRequest, "'$name' is reserved and can't be used")
        }
        return null
    }

    fun validateSchema(): Result? {
        val schemaReport = SchemaValidator.validate(GsonMapper.serialize(this), schema)
        return if (!schemaReport.isSuccess)
            Result.error(Error.NOT_VALID_JSON_SCHEMA)
        else null
    }
}