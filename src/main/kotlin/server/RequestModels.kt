package server

import com.google.gson.annotations.SerializedName
import keyboards.Button
import keyboards.Keyboard

enum class RequestSchemas(val path: String) {
    ADD_KEYBOARD_REQUEST("/json-schemas/requests/add_keyboard_request.json"),
    ADD_BUTTON_REQUEST("/json-schemas/requests/add_button_request.json"),
    DELETE_KEYBOARD_REQUEST("/json-schemas/requests/add_keyboard_request.json"),
    DELETE_BUTTON_REQUEST("/json-schemas/requests/delete_button_request.json")
}

data class AddKeyboardRequest(
    @SerializedName("host_keyboard") val parenKeyboard: String,
    @SerializedName("new_button") val newButton: String,
    @SerializedName("new_keyboard") val newKeyboard: Keyboard
)

data class DeleteKeyboardRequest(
    @SerializedName("keyboard") val keyboard: String
)

data class AddButtonRequest(
    @SerializedName("keyboard") val keyboard: String,
    @SerializedName("new_button") val newButton: Button
)

data class DeleteButtonRequest(
    @SerializedName("keyboard") val keyboard: String,
    @SerializedName("button_text") val buttonText: String
)
