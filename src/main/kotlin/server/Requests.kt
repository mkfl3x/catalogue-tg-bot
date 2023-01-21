package server

import server.models.Response
import server.models.Result
import server.models.requests.*


enum class Requests(
    val schemaPath: String,
    val type: Class<out Request>
) {
    ADD_KEYBOARD_REQUEST(
        "json-schemas/models/requests/objects/keyboard.json",
        AddKeyboardRequest::class.java
    ),
    ADD_BUTTON_REQUEST(
        "json-schemas/models/requests/objects/button.json",
        AddButtonRequest::class.java
    ),
    ADD_PAYLOAD_REQUEST(
        "json-schemas/models/requests/objects/payload.json",
        AddPayloadRequest::class.java
    ),
    EDIT_BUTTON_REQUEST(
        "json-schemas/models/requests/edit_button_request.json",
        EditButtonRequest::class.java
    ),
    DELETE_KEYBOARD_REQUEST(
        "json-schemas/models/requests/delete_keyboard_request.json",
        DeleteKeyboardRequest::class.java
    ),
    DELETE_BUTTON_REQUEST(
        "json-schemas/models/requests/delete_button_request.json",
        DeleteButtonRequest::class.java
    ),
    DELETE_PAYLOAD_REQUEST(
        "json-schemas/models/requests/delete_payload_request.json",
        DeletePayloadRequest::class.java
    ),
    LINK_BUTTON_REQUEST(
        "json-schemas/models/requests/link_button_request.json",
        LinkButtonRequest::class.java
    ),
    DETACH_KEYBOARD_REQUEST(
        "json-schemas/models/requests/detach_keyboard_request.json",
        DetachKeyboardRequest::class.java
    )
}

abstract class Request {

    abstract fun validateData(): Result?

    abstract fun relatedAction(): Response
}