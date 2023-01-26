package server.models.requests.data

import com.google.gson.annotations.SerializedName
import common.ReservedNames
import server.RequestActions.renameKeyboard
import server.models.requests.Request
import server.validations.RequestDataValidators
import server.validations.RequestValidationException

data class RenameKeyboardRequest(
    @SerializedName("keyboard_id") val keyboardId: String,
    @SerializedName("new_name") val newName: String
) : Request {

    override val schemaPath: String
        get() = "json-schemas/models/requests/rename_keyboard_request.json"

    override fun validateData() {
        RequestDataValidators.validateIds(keyboardId)
        RequestDataValidators.validateReservedNames(newName)
        RequestDataValidators.validateKeyboardExists(keyboardId)
        RequestDataValidators.validateKeyboardDoesNotExist(newName)
        if (newName == ReservedNames.MAIN_KEYBOARD.text)
            throw RequestValidationException("\"${ReservedNames.MAIN_KEYBOARD.text}\" can't be renamed")
    }

    override fun relatedAction() = renameKeyboard(keyboardId, newName)
}