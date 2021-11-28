package server

import com.google.gson.annotations.SerializedName
import keyboards.Button
import keyboards.Keyboard

enum class Schemas(val path: String) {
    ADD_KEYBOARD_REQUEST("json-schemas/requests/add_keyboard_request.json"),
    ADD_BUTTON_REQUEST("json-schemas/requests/add_button_request.json"),
    DELETE_KEYBOARD_REQUEST("json-schemas/requests/add_keyboard_request.json"),
    DELETE_BUTTON_REQUEST("json-schemas/requests/delete_button_request.json"),

    KEYBOARD("json-schemas/models/keyboard.json"),
    BUTTON("json-schemas/models/button.json")
}

data class AddKeyboardRequest(
    @SerializedName("new_button") val newButton: String,
    @SerializedName("new_keyboard") val newKeyboard: Keyboard
)

data class DeleteKeyboardRequest(
    @SerializedName("keyboard") val keyboard: String,
    @SerializedName("host_keyboard") val hostKeyboard: String,
)

data class AddButtonRequest(
    @SerializedName("keyboard") val keyboard: String,
    @SerializedName("new_button") val newButton: Button
)

data class DeleteButtonRequest(
    @SerializedName("keyboard") val keyboard: String,
    @SerializedName("button_text") val buttonText: String
)
