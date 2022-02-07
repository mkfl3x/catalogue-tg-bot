package server

import com.github.fge.jsonschema.core.report.ProcessingReport
import com.google.gson.annotations.SerializedName
import keyboards.models.Button
import keyboards.models.Keyboard
import keyboards.models.KeyboardLocation
import utils.GsonMapper
import utils.SchemaValidator

enum class Schemas(val path: String) {
    ADD_KEYBOARD_REQUEST("json-schemas/requests/add_keyboard_request.json"),
    ADD_BUTTON_REQUEST("json-schemas/requests/add_button_request.json"),
    DELETE_KEYBOARD_REQUEST("json-schemas/requests/delete_keyboard_request.json"),
    DELETE_BUTTON_REQUEST("json-schemas/requests/delete_button_request.json"),
    LINK_KEYBOARD_REQUEST("json-schemas/requests/link_keyboard_request.json"),
    DETACH_KEYBOARD_REQUEST("json-schemas/requests/detach_keyboard_request.json")
}

abstract class Request(val schema: Schemas) {

    fun validateSchema(): ProcessingReport {
        return SchemaValidator.validate(GsonMapper.serialize(this), this.schema)
    }
}

data class AddKeyboardRequest(
    @SerializedName("new_keyboard") val newKeyboard: Keyboard
) : Request(Schemas.ADD_KEYBOARD_REQUEST)

data class DeleteKeyboardRequest(
    @SerializedName("keyboard_name") val keyboard: String,
    @SerializedName("recursively") val recursively: Boolean
) : Request(Schemas.DELETE_KEYBOARD_REQUEST)

data class LinkKeyboardRequest(
    @SerializedName("keyboard_name") val keyboardName: String,
    @SerializedName("keyboard_location") val keyboardLocation: KeyboardLocation
) : Request(Schemas.LINK_KEYBOARD_REQUEST)

data class DetachKeyboardRequest(
    @SerializedName("keyboard_name") val keyboard: String
) : Request(Schemas.DETACH_KEYBOARD_REQUEST)

data class AddButtonRequest(
    @SerializedName("keyboard") val keyboard: String,
    @SerializedName("new_button") val newButton: Button
) : Request(Schemas.ADD_BUTTON_REQUEST)

data class DeleteButtonRequest(
    @SerializedName("keyboard") val keyboard: String,
    @SerializedName("button_text") val buttonText: String
) : Request(Schemas.DELETE_BUTTON_REQUEST)