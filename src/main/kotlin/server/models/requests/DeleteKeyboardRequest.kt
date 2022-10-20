package server.models.requests

import com.google.gson.annotations.SerializedName
import common.ReservedNames
import database.DataManager
import org.bson.types.ObjectId
import server.models.Error
import server.models.Result

data class DeleteKeyboardRequest(
    @SerializedName("keyboard_id") val keyboardId: String
) : Request() {

    override val schema: Schemas
        get() = Schemas.DELETE_KEYBOARD_REQUEST

    override val successMessage: String
        get() = "Keyboard with ID \"$keyboardId\" successfully deleted"

    override fun validateData(): Result? {
        DataManager.getKeyboard(ObjectId(keyboardId))?.let {
            if (it.name == ReservedNames.MAIN_KEYBOARD.text)
                return Result.error(Error.DELETE_MAIN_KEYBOARD)
        } ?: return Result.error(Error.KEYBOARD_DOES_NOT_EXIST, keyboardId)
        return null
    }
}