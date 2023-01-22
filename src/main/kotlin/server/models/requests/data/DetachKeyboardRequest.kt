package server.models.requests.data

import com.google.gson.annotations.SerializedName
import common.ReservedNames
import database.mongo.managers.DataManager
import server.RequestActions.deleteKeyboard
import server.models.requests.Request
import server.validations.RequestValidationException
import server.validations.RequestValidator

data class DetachKeyboardRequest(
    @SerializedName("keyboard_id") val keyboardId: String
) : Request {

    override val schemaPath: String
        get() = "json-schemas/models/requests/detach_keyboard_request.json"

    override fun validateData() {
        RequestValidator.validateIds(keyboardId)
        RequestValidator.validateKeyboardExistence(keyboardId)
        DataManager.getKeyboard(keyboardId)?.let {
            if (it.name == ReservedNames.MAIN_KEYBOARD.text)
                throw RequestValidationException("\"${ReservedNames.MAIN_KEYBOARD.text}\" can't be linked/detached")
            if (it.leadButtons.isEmpty())
                throw RequestValidationException("Keyboard \"$keyboardId\" already detached")
        }
    }

    override fun relatedAction() = deleteKeyboard(DataManager.getKeyboard(keyboardId)!!, true)
}