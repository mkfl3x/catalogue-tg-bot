package server.models.requests

import com.google.gson.annotations.SerializedName
import common.ReservedNames
import database.mongo.DataManager
import server.Request
import server.RequestValidator
import server.models.Error
import server.models.Result
import server.RequestActions.deleteKeyboard

data class DetachKeyboardRequest(
    @SerializedName("keyboard_id") val keyboardId: String
) : Request() {

    override fun validateData(): Result? {
        RequestValidator.validateIds(keyboardId)?.let { return it }
        DataManager.getKeyboard(keyboardId)?.let {
            if (it.name == ReservedNames.MAIN_KEYBOARD.text)
                return Result.error(Error.LINK_DETACH_MAIN_KEYBOARD)
            if (it.leadButtons.isEmpty())
                return Result.error(Error.KEYBOARD_ALREADY_DETACHED)
        } ?: return Result.error(Error.KEYBOARD_DOES_NOT_EXIST)
        return null
    }

    override fun relatedAction() = deleteKeyboard(DataManager.getKeyboard(keyboardId)!!, true)
}