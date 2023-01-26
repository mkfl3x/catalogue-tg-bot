package server.models.requests.data

import com.google.gson.annotations.SerializedName
import server.RequestActions
import server.models.requests.Request
import server.validations.RequestDataValidators

data class UpdateKeyboardButtonRequest(
    @SerializedName("keyboard_id") val keyboardId: String,
    @SerializedName("button_id") val buttonId: String,
    @SerializedName("action") val action: String
) : Request {

    override val schemaPath: String
        get() = "json-schemas/models/requests/update_keyboard_button_request.json"

    override fun validateData() {
        RequestDataValidators.validateIds(keyboardId, buttonId)
        RequestDataValidators.validateValueInList(action, listOf("add", "delete"))
        RequestDataValidators.validateKeyboardExists(keyboardId)
        RequestDataValidators.validateButtonExists(buttonId)
        if (action == "add") {
            RequestDataValidators.validateLoopButton(keyboardId, buttonId)
            RequestDataValidators.validateKeyboardDoesNotContainButton(keyboardId, buttonId)
        }
    }

    override fun relatedAction() = RequestActions.updateKeyboardButton(this, action)
}