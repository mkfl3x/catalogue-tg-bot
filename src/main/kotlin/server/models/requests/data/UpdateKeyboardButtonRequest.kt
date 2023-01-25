package server.models.requests.data

import com.google.gson.annotations.SerializedName
import server.RequestActions
import server.models.requests.Request
import server.validations.RequestValidator

data class UpdateKeyboardButtonRequest(
    @SerializedName("keyboard_id") val keyboardId: String,
    @SerializedName("button_id") val buttonId: String,
    @SerializedName("action") val action: String
) : Request {

    override val schemaPath: String
        get() = "json-schemas/models/requests/update_keyboard_button_request.json"

    override fun validateData() {
        RequestValidator.validateIds(keyboardId, buttonId)
        RequestValidator.validateIsInList(action, listOf("add", "delete"))
        RequestValidator.validateKeyboardExistence(keyboardId)
        RequestValidator.validateButtonExistence(buttonId)
        if (action == "add") {
            RequestValidator.validateLoopButton(keyboardId, buttonId)
            RequestValidator.validateKeyboardButtonsDuplication(keyboardId, buttonId)
        }
    }

    override fun relatedAction() = RequestActions.updateKeyboardButton(this, action)
}