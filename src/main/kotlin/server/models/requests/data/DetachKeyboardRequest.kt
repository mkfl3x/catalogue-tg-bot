package server.models.requests.data

import com.google.gson.annotations.SerializedName
import database.mongo.managers.DataManager
import server.RequestActions.deleteKeyboard
import server.models.requests.Request
import server.validations.RequestDataValidators
import server.validations.RequestValidationException

data class DetachKeyboardRequest(
    @SerializedName("keyboard_id") val keyboardId: String
) : Request {

    override val schemaPath: String
        get() = "json-schemas/models/requests/detach_keyboard_request.json"

    override fun validateData() {
        RequestDataValidators.validateIds(keyboardId)
        DataManager.getKeyboard(keyboardId).apply {
            RequestDataValidators.validateMainKeyboardLinkingDetaching(name)
            if (leadButtons.isEmpty())
                throw RequestValidationException("Keyboard \"$keyboardId\" already detached")
        }
    }

    override fun relatedAction() = deleteKeyboard(DataManager.getKeyboard(keyboardId), true)
}