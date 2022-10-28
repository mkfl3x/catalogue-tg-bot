package server.models.requests

import com.google.gson.annotations.SerializedName
import common.ReservedNames
import database.mongo.DataManager
import server.models.Error
import server.models.Result

data class DeleteKeyboardRequest(
    @SerializedName("keyboard_id") val keyboardId: String
) : Request() {

    override val successMessage: String
        get() = "Keyboard with ID \"$keyboardId\" successfully deleted"

    override fun validateData(): Result? {
        RequestValidator.validateIds(keyboardId)?.let { return it }
        DataManager.getKeyboard(keyboardId)?.let {
            if (it.name == ReservedNames.MAIN_KEYBOARD.text)
                return Result.error(Error.DELETE_MAIN_KEYBOARD)
        } ?: return Result.error(Error.KEYBOARD_DOES_NOT_EXIST, keyboardId)
        return null
    }
}