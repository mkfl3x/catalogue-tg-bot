package server.handlers

import com.mongodb.BasicDBObject
import common.ReservedNames
import database.MongoClient
import io.ktor.http.*
import keyboards.KeyboardsManager
import keyboards.models.Button
import keyboards.models.Keyboard
import keyboards.models.KeyboardLocation
import org.bson.BsonNull
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import server.*
import server.models.Error
import server.models.Result

class KeyboardsHandler(private val mongoKeyboards: String) {

    private val logger: Logger = LoggerFactory.getLogger(this.javaClass.name)

    // Keyboards handling

    fun getKeyboards(filter: String): Result {
        val keyboards = when (filter) {
            "all" -> KeyboardsManager.getKeyboards()
            "detached" -> KeyboardsManager.getKeyboards()
                .filter { it.keyboardLocation == null }
                .toList()
            else -> return error(Error.UNKNOWN_PARAMETER)
        }
        return Result(HttpStatusCode.OK, keyboards)
    }

    fun addKeyboard(request: AddKeyboardRequest): Result {
        return validateRequest(request) ?: let {

            request.newKeyboard.keyboardLocation?.let {
                addButton(it.hostKeyboard, Button(it.linkButton, "keyboard", keyboard = request.newKeyboard.name))
            }
            addKeyboard(request.newKeyboard)

            KeyboardsManager.reloadKeyboards()
            return Result(HttpStatusCode.OK, "Keyboard successfully added")
        }
    }

    fun deleteKeyboard(request: DeleteKeyboardRequest): Result {
        return validateRequest(request) ?: let {

            val keyboard = KeyboardsManager.getKeyboard(request.keyboard)

            // Drop from states
            KeyboardsManager.keyboardStates.delete(keyboard!!.name)

            // Delete leads button on host keyboard
            if (keyboard.keyboardLocation != null)
                deleteButton(keyboard.keyboardLocation.hostKeyboard, keyboard.keyboardLocation.linkButton)

            // Detach/delete nested keyboards and remove buttons
            if (request.recursively) {
                // TODO:
            } else {
                keyboard.buttons.forEach { button ->
                    if (button.type == "keyboard")
                        detachKeyboard(button.keyboard!!)
                    if (button.type == "button")
                        deleteButton(keyboard.name, button.text)
                }
            }

            // Delete keyboard
            deleteKeyboard(keyboard.name, false)

            KeyboardsManager.reloadKeyboards()
            return Result(HttpStatusCode.OK, "Keyboard successfully deleted")
        }
    }

    fun linkKeyboard(request: LinkKeyboardRequest): Result {
        return validateRequest(request) ?: let {

            addButton(
                request.keyboardLocation.hostKeyboard,
                Button(request.keyboardLocation.linkButton, "keyboard", keyboard = request.keyboardName)
            )
            setKeyboardLocation(request.keyboardName, request.keyboardLocation)

            KeyboardsManager.reloadKeyboards()
            return Result(HttpStatusCode.OK, "Keyboard successfully linked")
        }
    }

    fun detachKeyboard(request: DetachKeyboardRequest): Result {
        return validateRequest(request) ?: let {
            detachKeyboard(request.keyboard)
            KeyboardsManager.reloadKeyboards()
            return Result(HttpStatusCode.OK, "Keyboard successfully detached")
        }
    }


    // Buttons handling

    fun addButton(request: AddButtonRequest): Result {
        return validateRequest(request) ?: let {

            addButton(request.keyboard, request.newButton)

            KeyboardsManager.reloadKeyboards()
            return Result(HttpStatusCode.OK, "Button successfully added")
        }
    }

    fun deleteButton(request: DeleteButtonRequest): Result {
        return validateRequest(request) ?: let {

            val button = getButton(request.keyboard, request.buttonText)!!
            if (button.type == "keyboard")
                setKeyboardLocation(request.keyboard, null)
            deleteButton(request.keyboard, request.buttonText)

            KeyboardsManager.reloadKeyboards()
            return Result(HttpStatusCode.OK, "Button deleted successfully")
        }
    }


    // Service

    private fun getKeyboard(keyboardName: String): Keyboard? =
        KeyboardsManager.getKeyboard(keyboardName)

    private fun getButton(keyboardName: String, buttonText: String): Button? =
        getKeyboard(keyboardName)?.buttons?.firstOrNull { it.text == buttonText }

    private fun isKeyboardExist(keyboard: String): Boolean =
        KeyboardsManager.getKeyboard(keyboard) != null

    private fun isButtonExist(keyboard: String, button: String): Boolean =
        KeyboardsManager.getKeyboard(keyboard)!!.buttons.firstOrNull { it.text == button } != null

    private fun addKeyboard(keyboard: Keyboard) {
        MongoClient.create(mongoKeyboards, keyboard, Keyboard::class.java)
    }

    private fun addButton(keyboard: String, button: Button) {
        MongoClient.update(
            mongoKeyboards,
            Keyboard::class.java,
            BasicDBObject("name", keyboard),
            BasicDBObject("\$push", BasicDBObject("buttons", button))
        )
    }

    private fun deleteKeyboard(keyboardName: String, recursively: Boolean) {
        MongoClient.delete(mongoKeyboards, BasicDBObject("name", keyboardName))
    }

    private fun detachKeyboard(keyboard: String) {
        getKeyboard(keyboard)?.let {
            deleteButton(it.keyboardLocation!!.hostKeyboard, it.keyboardLocation.linkButton)
            setKeyboardLocation(it.name, null)
            KeyboardsManager.keyboardStates.delete(keyboard)
        }
    }

    private fun deleteButton(keyboard: String, buttonText: String) {
        MongoClient.update(
            mongoKeyboards,
            Keyboard::class.java,
            BasicDBObject("name", keyboard),
            BasicDBObject("\$pull", BasicDBObject("buttons", BasicDBObject("text", buttonText)))
        )
    }

    private fun setKeyboardLocation(keyboard: String, location: KeyboardLocation?) {
        val query = if (location != null)
            BasicDBObject("\$set", BasicDBObject("keyboard_location", location))
        else
            BasicDBObject("\$unset", BasicDBObject("keyboard_location", BsonNull()))
        MongoClient.update(mongoKeyboards, Keyboard::class.java, BasicDBObject("name", keyboard), query)
    }

    private fun error(error: Error, vararg args: String): Result {
        // TODO: add logging here
        return Result(error.code, error.message.format(args))
    }

    // Validators

    private fun validateSchema(request: Request): Result? {
        if (!request.validateSchema().isSuccess)
            return error(Error.NOT_VALID_JSON_SCHEMA)
        return null
    }

    private fun validateRequest(request: Request): Result? {
        var validationResult = validateSchema(request)
        if (validationResult != null) return validationResult

        validationResult = when (request) {
            is AddKeyboardRequest -> validateKeyboardAddition(request)
            is DeleteKeyboardRequest -> validateKeyboardDeletion(request)
            is LinkKeyboardRequest -> validateKeyboardLinking(request)
            is DetachKeyboardRequest -> validateKeyboardDetaching(request)
            is AddButtonRequest -> validateButtonAddition(request)
            is DeleteButtonRequest -> validateButtonDeletion(request)
            else -> throw Exception("Unexpected request for validation")
        }
        return validationResult
    }

    private fun validateNames(vararg names: String?): Result? {
        names.forEach { name ->
            if (ReservedNames.values().any { it.text == name })
                return Result(HttpStatusCode.BadRequest, "'$name' is reserved and can't be used")
        }
        return null
    }

    private fun validateKeyboardAddition(request: AddKeyboardRequest): Result? {
        val keyboard = request.newKeyboard
        validateNames(keyboard.name, keyboard.keyboardLocation?.linkButton ?: " ")
        if (isKeyboardExist(keyboard.name))
            return error(Error.KEYBOARD_ALREADY_EXISTS, keyboard.name)
        keyboard.keyboardLocation?.let {
            if (!isKeyboardExist(it.hostKeyboard))
                return error(Error.KEYBOARD_DOES_NOT_EXIST, it.hostKeyboard)
            if (isButtonExist(it.hostKeyboard, it.linkButton))
                return error(Error.BUTTON_ALREADY_EXISTS, it.linkButton)
        }
        return null
    }

    private fun validateButtonAddition(request: AddButtonRequest): Result? {
        validateNames(request.newButton.text, request.newButton.payload)
        if (!isKeyboardExist(request.keyboard))
            return error(Error.KEYBOARD_DOES_NOT_EXIST, request.keyboard)
        if (isButtonExist(request.keyboard, request.newButton.text))
            return error(Error.BUTTON_ALREADY_EXISTS, request.newButton.text)
        if (request.newButton.type == "keyboard" && (request.keyboard == request.newButton.keyboard))
            return error(Error.LOOPED_BUTTON)
        return null
    }

    private fun validateKeyboardDeletion(request: DeleteKeyboardRequest): Result? {
        if (request.keyboard == ReservedNames.MAIN_KEYBOARD.text)
            return error(Error.DELETE_MAIN_KEYBOARD)
        if (!isKeyboardExist(request.keyboard))
            return error(Error.KEYBOARD_DOES_NOT_EXIST, request.keyboard)
        return null
    }

    private fun validateKeyboardDetaching(request: DetachKeyboardRequest): Result? {
        if (request.keyboard == ReservedNames.MAIN_KEYBOARD.text)
            return error(Error.LINK_DETACH_MAIN_KEYBOARD)
        if (!isKeyboardExist(request.keyboard))
            return error(Error.KEYBOARD_DOES_NOT_EXIST)
        if (getKeyboard(request.keyboard)!!.keyboardLocation == null)
            return error(Error.KEYBOARD_ALREADY_DETACHED)
        return null
    }

    private fun validateButtonDeletion(request: DeleteButtonRequest): Result? {
        if (!isKeyboardExist(request.keyboard))
            error(Error.KEYBOARD_DOES_NOT_EXIST, request.keyboard)
        if (!isButtonExist(request.keyboard, request.buttonText))
            return error(Error.BUTTON_DOES_NOT_EXIST, request.buttonText)
        return null
    }

    private fun validateKeyboardLinking(request: LinkKeyboardRequest): Result? {
        if (request.keyboardName == ReservedNames.MAIN_KEYBOARD.text)
            return error(Error.LINK_DETACH_MAIN_KEYBOARD)
        if (!isKeyboardExist(request.keyboardName))
            return error(Error.KEYBOARD_DOES_NOT_EXIST, request.keyboardName)
        getKeyboard(request.keyboardName)?.keyboardLocation?.let {
            // TODO: add ability to re-link linked keyboard
            error(Error.KEYBOARD_ALREADY_LINKED, request.keyboardName)
        }
        if (!isKeyboardExist(request.keyboardLocation.hostKeyboard))
            error(Error.KEYBOARD_DOES_NOT_EXIST, request.keyboardLocation.hostKeyboard)
        if (isButtonExist(request.keyboardLocation.hostKeyboard, request.keyboardLocation.linkButton))
            error(Error.BUTTON_ALREADY_EXISTS, request.keyboardLocation.linkButton)
        return null
    }
}
