package server.models.requests

import com.google.gson.annotations.SerializedName
import database.mongo.DataManager
import server.Request
import server.RequestValidator
import server.models.Result
import server.RequestActions.deleteKeyboard

data class DeleteKeyboardRequest(
    @SerializedName("keyboard_id") val keyboardId: String
) : Request() {

    override fun validateData(): Result? {
        RequestValidator.validateIds(keyboardId)?.let { return it }
        RequestValidator.validateKeyboardExistence(keyboardId)?.let { return it }
        RequestValidator.tryDeleteMainKeyboard(keyboardId)?.let { return it }
        return null
    }

    override fun relatedAction() = deleteKeyboard(DataManager.getKeyboard(keyboardId)!!, false)
}