package server.models.requests

import com.google.gson.annotations.SerializedName
import database.DataManager
import org.bson.types.ObjectId
import server.models.Error
import server.models.Result
import server.models.objects.Location

data class AddKeyboardRequest(
    @SerializedName("name") val name: String,
    @SerializedName("buttons") val buttons: List<String>,
    @SerializedName("location") val location: Location?
) : Request() {

    override val schema: Schemas
        get() = Schemas.ADD_KEYBOARD_REQUEST

    override val successMessage: String
        get() = "Keyboard \"${name}\" successfully added"

    override fun validateData(): Result? {
        RequestValidator.validateIds(*buttons.toTypedArray(), location?.hostKeyboard)?.let { return it }
        RequestValidator.validateReservedNames(name)?.let { return it }
        RequestValidator.validateKeyboardExistence(name)?.let { return it }
        if (DataManager.getKeyboards().any { it.name == name })
            return Result.error(Error.KEYBOARD_ALREADY_EXISTS, name)
        buttons.forEach {
            if (DataManager.getButton(ObjectId(it)) == null)
                return Result.error(Error.BUTTON_DOES_NOT_EXIST, it)
        }
        location?.let { location ->
            RequestValidator.validateLocation(location)?.let { return it }
            // TODO: add check for existence of button on host keyboard
        }
        return null
    }
}