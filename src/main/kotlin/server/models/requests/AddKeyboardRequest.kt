package server.models.requests

import com.google.gson.annotations.SerializedName
import database.DataManager
import database.DataManager.isKeyboardExist
import database.DataManager.keyboardHasButton
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

        // validate keyboard name
        validateReservedNames(name)

        // check is keyboard with same name already exists
        if (isKeyboardExist(name))
            return Result.error(Error.KEYBOARD_ALREADY_EXISTS, name)

        // check are buttons exists
        buttons.forEach {
            if(DataManager.getButton(ObjectId(it)) == null)
                return Result.error(Error.BUTTON_DOES_NOT_EXIST, it)
        }

        // check location
        location?.let {

            // validate lead button text
            validateReservedNames(it.leadButtonText)

            // check is host keyboard exists
            if (!isKeyboardExist(ObjectId(it.hostKeyboard)))
                return Result.error(Error.KEYBOARD_DOES_NOT_EXIST, it.hostKeyboard)

            // check is leads button already exists on host keyboard
            if (keyboardHasButton(ObjectId(it.hostKeyboard), it.leadButtonText))
                return Result.error(Error.BUTTON_ALREADY_EXISTS, it.leadButtonText, it.hostKeyboard)
        }
        return null
    }
}