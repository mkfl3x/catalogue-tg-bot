package server.models.requests

import com.google.gson.annotations.SerializedName
import common.ReservedNames
import database.DataManager
import org.bson.types.ObjectId
import server.models.Error
import server.models.Result


data class DetachKeyboardRequest(
    @SerializedName("keyboard_id") val keyboardId: String
) : Request() {

    override val schema: Schemas
        get() = Schemas.DETACH_KEYBOARD_REQUEST

    override val successMessage: String
        get() = "Keyboard '$keyboardId' successfully detached"

    override fun validateData(): Result? {
        DataManager.getKeyboard(ObjectId(keyboardId))?.let {
            if (it.name == ReservedNames.MAIN_KEYBOARD.text)
                return Result.error(Error.LINK_DETACH_MAIN_KEYBOARD)
            if (it.leadButton == null)
                return Result.error(Error.KEYBOARD_ALREADY_DETACHED)
        } ?: return Result.error(Error.KEYBOARD_DOES_NOT_EXIST)
        return null
    }
}