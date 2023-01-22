package server.models.requests.data

import com.google.gson.annotations.SerializedName
import database.mongo.managers.DataManager
import server.RequestActions.deleteKeyboard
import server.models.requests.Request
import server.validations.RequestValidator

data class DeleteKeyboardRequest(
    @SerializedName("keyboard_id") val keyboardId: String
) : Request {

    override val schemaPath: String
        get() = "json-schemas/models/requests/delete_keyboard_request.json"

    override fun validateData() {
        RequestValidator.validateIds(keyboardId)
        RequestValidator.validateKeyboardExistence(keyboardId)
        RequestValidator.validateKeyboardDeletion(keyboardId)
    }

    override fun relatedAction() = deleteKeyboard(DataManager.getKeyboard(keyboardId)!!, false)
}