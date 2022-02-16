package server.models

import com.google.gson.annotations.SerializedName
import common.ReservedNames
import io.ktor.http.*
import keyboards.KeyboardsManager
import keyboards.KeyboardsManager.isButtonExist
import keyboards.KeyboardsManager.isKeyboardExist
import keyboards.models.Button
import keyboards.models.Keyboard
import keyboards.models.KeyboardLocation
import utils.GsonMapper
import utils.SchemaValidator

enum class Schemas(val path: String) {
    ADD_KEYBOARD_REQUEST("json-schemas/requests/add_keyboard_request.json"),
    ADD_BUTTON_REQUEST("json-schemas/requests/add_button_request.json"),
    DELETE_KEYBOARD_REQUEST("json-schemas/requests/delete_keyboard_request.json"),
    DELETE_BUTTON_REQUEST("json-schemas/requests/delete_button_request.json"),
    LINK_KEYBOARD_REQUEST("json-schemas/requests/link_keyboard_request.json"),
    DETACH_KEYBOARD_REQUEST("json-schemas/requests/detach_keyboard_request.json")
}

abstract class Request {

    abstract val successMessage: String

    protected abstract val schema: Schemas

    abstract fun validateData(): Result?

    protected fun validateNames(vararg names: String?): Result? {
        names.forEach { name ->
            if (ReservedNames.values().any { it.text == name })
                return Result(HttpStatusCode.BadRequest, "'$name' is reserved and can't be used")
        }
        return null
    }

    fun validateSchema(): Result? {
        val schemaReport = SchemaValidator.validate(GsonMapper.serialize(this), schema)
        return if (!schemaReport.isSuccess)
            Result.error(Error.NOT_VALID_JSON_SCHEMA)
        else null
    }
}

data class AddKeyboardRequest(
    @SerializedName("new_keyboard") val newKeyboard: Keyboard
) : Request() {

    override val schema: Schemas
        get() = Schemas.ADD_KEYBOARD_REQUEST

    override val successMessage: String
        get() = "Keyboard ${newKeyboard.name} successfully added"

    override fun validateData(): Result? {
        validateNames(newKeyboard.name, newKeyboard.keyboardLocation?.linkButton ?: " ")
        if (isKeyboardExist(newKeyboard.name))
            return Result.error(Error.KEYBOARD_ALREADY_EXISTS, newKeyboard.name)
        newKeyboard.keyboardLocation?.let {
            if (!isKeyboardExist(it.hostKeyboard))
                return Result.error(Error.KEYBOARD_DOES_NOT_EXIST, it.hostKeyboard)
            if (isButtonExist(it.hostKeyboard, it.linkButton))
                return Result.error(Error.BUTTON_ALREADY_EXISTS, it.linkButton)
        }
        return null
    }
}

data class DeleteKeyboardRequest(
    @SerializedName("keyboard_name") val keyboard: String,
    @SerializedName("recursively") val recursively: Boolean
) : Request() {

    override val schema: Schemas
        get() = Schemas.DELETE_KEYBOARD_REQUEST

    override val successMessage: String
        get() = "Keyboard '$keyboard' successfully deleted"

    override fun validateData(): Result? {
        if (keyboard == ReservedNames.MAIN_KEYBOARD.text)
            return Result.error(Error.DELETE_MAIN_KEYBOARD)
        if (isKeyboardExist(keyboard))
            return Result.error(Error.KEYBOARD_DOES_NOT_EXIST, keyboard)
        return null
    }
}

data class LinkKeyboardRequest(
    @SerializedName("keyboard_name") val keyboardName: String,
    @SerializedName("keyboard_location") val keyboardLocation: KeyboardLocation
) : Request() {

    override val schema: Schemas
        get() = Schemas.LINK_KEYBOARD_REQUEST

    override val successMessage: String
        get() = "Keyboard '$keyboardName' successfully linked to '${keyboardLocation.hostKeyboard}' with '${keyboardLocation.linkButton}' button"

    override fun validateData(): Result? {
        if (keyboardName == ReservedNames.MAIN_KEYBOARD.text)
            return Result.error(Error.LINK_DETACH_MAIN_KEYBOARD)
        if (!isKeyboardExist(keyboardName))
            return Result.error(Error.KEYBOARD_DOES_NOT_EXIST, keyboardName)
        KeyboardsManager.getKeyboard(keyboardName)?.keyboardLocation?.let {
            Result.error(Error.KEYBOARD_ALREADY_LINKED, keyboardName)
        }
        if (!isKeyboardExist(keyboardLocation.hostKeyboard))
            Result.error(Error.KEYBOARD_DOES_NOT_EXIST, keyboardLocation.hostKeyboard)
        if (isButtonExist(keyboardLocation.hostKeyboard, keyboardLocation.linkButton))
            Result.error(Error.BUTTON_ALREADY_EXISTS, keyboardLocation.linkButton)
        return null
    }
}

data class DetachKeyboardRequest(
    @SerializedName("keyboard_name") val keyboard: String
) : Request() {

    override val schema: Schemas
        get() = Schemas.DETACH_KEYBOARD_REQUEST

    override val successMessage: String
        get() = "Keyboard '$keyboard' successfully detached"

    override fun validateData(): Result? {
        if (keyboard == ReservedNames.MAIN_KEYBOARD.text)
            return Result.error(Error.LINK_DETACH_MAIN_KEYBOARD)
        if (!isKeyboardExist(keyboard))
            return Result.error(Error.KEYBOARD_DOES_NOT_EXIST)
        if (KeyboardsManager.getKeyboard(keyboard)!!.keyboardLocation == null)
            return Result.error(Error.KEYBOARD_ALREADY_DETACHED)
        return null
    }
}

data class AddButtonRequest(
    @SerializedName("keyboard") val keyboard: String,
    @SerializedName("new_button") val newButton: Button
) : Request() {

    override val schema: Schemas
        get() = Schemas.ADD_BUTTON_REQUEST

    override val successMessage: String
        get() = "Button '${newButton.text}' successfully added to '$keyboard' keyboard"

    override fun validateData(): Result? {
        validateNames(newButton.text, newButton.payload)
        if (!isKeyboardExist(keyboard))
            return Result.error(Error.KEYBOARD_DOES_NOT_EXIST, keyboard)
        if (isButtonExist(keyboard, newButton.text))
            return Result.error(Error.BUTTON_ALREADY_EXISTS, newButton.text)
        if (newButton.type == "keyboard" && (keyboard == newButton.keyboard))
            return Result.error(Error.LOOPED_BUTTON)
        return null
    }
}

data class DeleteButtonRequest(
    @SerializedName("keyboard") val keyboard: String,
    @SerializedName("button_text") val buttonText: String
) : Request() {

    override val schema: Schemas
        get() = Schemas.DELETE_BUTTON_REQUEST

    override val successMessage: String
        get() = "Button '$buttonText' successfully deleted from '$keyboard' keyboard"

    override fun validateData(): Result? {
        if (!isKeyboardExist(keyboard))
            return Result.error(Error.KEYBOARD_DOES_NOT_EXIST, keyboard)
        if (!isButtonExist(keyboard, buttonText))
            return Result.error(Error.BUTTON_DOES_NOT_EXIST, buttonText)
        return null
    }
}