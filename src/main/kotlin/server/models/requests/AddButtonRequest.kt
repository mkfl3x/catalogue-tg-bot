package server.models.requests

import com.google.gson.annotations.SerializedName
import database.DataManager
import database.DataManager.isKeyboardExist
import database.DataManager.keyboardHasButton
import org.bson.types.ObjectId
import server.models.Error
import server.models.Result

data class AddButtonRequest(
    @SerializedName("text") val text: String,
    @SerializedName("type") val type: String,
    @SerializedName("link") val link: String,
    @SerializedName("host_keyboard") val hostKeyboard: String?
) : Request() {

    override val schema: Schemas
        get() = Schemas.ADD_BUTTON_REQUEST

    override val successMessage: String
        get() = "Button \"${text}\" successfully added"

    override fun validateData(): Result? {

        // validate button text
        validateReservedNames(text)

        // check is resource to link exists
        if (!DataManager.getKeyboards().any { it.id == ObjectId(link) } &&
            !DataManager.getPayloads().any { it.id == ObjectId(link) })
            return Result.error(Error.BUTTON_ALREADY_EXISTS, link)

        // check location
        hostKeyboard?.let {

            // check is host keyboard exists
            if (!isKeyboardExist(ObjectId(it)))
                return Result.error(Error.KEYBOARD_DOES_NOT_EXIST, it)

            // check for loop link
            if (type == "keyboard" && link == it) {
                return Result.error(Error.LOOPED_BUTTON)
            }

            // check is leads button already exists on host keyboard
            if (keyboardHasButton(ObjectId(it), text))
                return Result.error(Error.BUTTON_ALREADY_EXISTS, text, it)
        }
        return null
    }
}