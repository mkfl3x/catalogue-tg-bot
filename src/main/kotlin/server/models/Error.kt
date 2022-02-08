package server.models

import io.ktor.http.*

enum class Error(val message: String, val code: HttpStatusCode = HttpStatusCode.OK) {
    UNKNOWN_PARAMETER("Unknown '%s' parameter"),
    NOT_VALID_JSON_SCHEMA("Request body is not valid", HttpStatusCode.BadRequest),
    KEYBOARD_ALREADY_EXISTS("Keyboard '%s' already exists"),
    KEYBOARD_DOES_NOT_EXIST("Keyboard '%s' doesn't exists"),
    BUTTON_DOES_NOT_EXIST("Button '%s' doesn't exist"),
    BUTTON_ALREADY_EXISTS("Button '%s' already exists"),
    LOOPED_BUTTON("Button can't leads to it's host keyboard"),
    DELETE_MAIN_KEYBOARD("'MainKeyboard' can't be deleted"),
    LINK_DETACH_MAIN_KEYBOARD("'MainKeyboard' can't be linked/detached"),
    KEYBOARD_ALREADY_LINKED("Keyboard '%s' already linked"),
    KEYBOARD_ALREADY_DETACHED("Keyboard '%s' already detached")
}