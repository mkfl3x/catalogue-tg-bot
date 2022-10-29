package server.models

import common.ReservedNames
import io.ktor.http.*

data class Result(
    val responseCode: HttpStatusCode,
    val responseData: Any
) {
    companion object {

        fun success(message: Any) = Result(HttpStatusCode.OK, message)

        fun error(error: Error, vararg args: String) =
            Result(error.code, error.message.format(*args))
    }
}

enum class Error(val message: String, val code: HttpStatusCode = HttpStatusCode.OK) {
    UNKNOWN_PARAMETER_VALUE("Unknown \"%s\" value for \"%s\" parameter"),
    NOT_VALID_JSON_SCHEMA("Request body is not valid: %s", HttpStatusCode.BadRequest),
    KEYBOARD_ALREADY_EXISTS("Keyboard \"%s\" already exists"),
    KEYBOARD_DOES_NOT_EXIST("Keyboard \"%s\" doesn't exists"),
    BUTTON_DOES_NOT_EXIST("Button \"%s\" doesn't exist"),
    BUTTON_ALREADY_EXISTS("Button \"%s\" already exists on \"%s\" keyboard"),
    PAYLOAD_ALREADY_EXISTS("Payload \"%s\" already exists"),
    PAYLOAD_DOES_NOT_EXISTS("Payload \"%s\" doesn't exist"),
    LOOPED_BUTTON("Button can't leads to it's host keyboard"),
    DELETE_MAIN_KEYBOARD("\"${ReservedNames.MAIN_KEYBOARD.text}\" can't be deleted"),
    LINK_DETACH_MAIN_KEYBOARD("\"${ReservedNames.MAIN_KEYBOARD.text}\" can't be linked/detached"),
    KEYBOARD_ALREADY_DETACHED("Keyboard \"%s\" already detached"),
    RESOURCE_DOES_NOT_EXISTS("Keyboard/payload \"%s\" doesn't exist"),
    NOT_VALID_ID("ID \"%s\" format is not applicable")
}