package server.models.requests

import com.google.gson.annotations.SerializedName
import server.models.Result

data class DeleteKeyboardRequest(
    @SerializedName("keyboard_id") val keyboardId: String
) : Request() {

    override val successMessage: String
        get() = "Keyboard with ID \"$keyboardId\" successfully deleted"

    override fun validateData(): Result? {
        RequestValidator.validateIds(keyboardId)?.let { return it }
        RequestValidator.validateKeyboardExistence(keyboardId)?.let { return it }
        RequestValidator.tryDeleteMainKeyboard(keyboardId)?.let { return it }
        return null
    }
}