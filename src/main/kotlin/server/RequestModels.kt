package server

import com.google.gson.annotations.SerializedName
import keyboards.Button
import keyboards.Keyboard
import keyboards.KeyboardLocation

enum class Schemas(val path: String) {
    ADD_KEYBOARD_REQUEST("json-schemas/requests/add_keyboard_request.json"),
    ADD_BUTTON_REQUEST("json-schemas/requests/add_button_request.json"),
    DELETE_KEYBOARD_REQUEST("json-schemas/requests/delete_keyboard_request.json"),
    DELETE_BUTTON_REQUEST("json-schemas/requests/delete_button_request.json"),
    LINK_KEYBOARD_REQUEST("json-schemas/requests/link_keyboard_request.json"),
    DETACH_KEYBOARD_REQUEST("json-schemas/requests/detach_keyboard_request.json"),

    KEYBOARD("json-schemas/models/keyboard.json"),
    BUTTON("json-schemas/models/button.json")
}

data class AddKeyboardRequest(
    @SerializedName("new_keyboard") val newKeyboard: Keyboard
)

data class DeleteKeyboardRequest(
    @SerializedName("keyboard_name") val keyboard: String,
    @SerializedName("recursively") val recursively: Boolean
)

data class LinkKeyboardRequest(
    @SerializedName("keyboard_name") val keyboardName: String,
    @SerializedName("keyboard_location") val keyboardLocation: KeyboardLocation
)

data class DetachKeyboardRequest(
    @SerializedName("keyboard_name") val keyboard: String
)

data class AddButtonRequest(
    @SerializedName("keyboard") val keyboard: String,
    @SerializedName("new_button") val newButton: Button
)

data class DeleteButtonRequest(
    @SerializedName("keyboard") val keyboard: String,
    @SerializedName("button_text") val buttonText: String
)

data class MoveButtonRequest(
    @SerializedName("button_text") val buttonText: String,
    @SerializedName("from_keyboard") val fromKeyboard: String,
    @SerializedName("to_keyboard") val toKeyboard: String
)