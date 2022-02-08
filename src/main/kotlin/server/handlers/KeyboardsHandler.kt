package server.handlers

import com.mongodb.BasicDBObject
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
import server.models.Result
import utils.Properties

enum class ReservedString(val text: String) {
    START("/start"),
    BACK("Back"),
    MAIN_KEYBOARD("MainKeyboard")
}

enum class Error(cause: String, httpCode: HttpStatusCode = HttpStatusCode.OK) {
    NOT_VALID_JSON_SCHEMA("Request body is not valid", HttpStatusCode.BadRequest),
    KEYBOARD_ALREADY_EXISTS("Keyboard '%s' already exists"),
    KEYBOARD_DOES_NOT_EXIST("Keyboard '%s' doesn't exists"),
    BUTTON_DOES_NOT_EXIST("Button '%s' doesn't exist"),
    BUTTON_ALREADY_EXISTS("Button '%s' already exists");
}

class KeyboardsHandler {

    private val logger: Logger = LoggerFactory.getLogger(this.javaClass.name)
    private val mongoCollection = Properties.get("mongo.collection.keyboards")


    // Keyboards handling

    fun getKeyboards(filter: String): Result {
        val keyboards = when (filter) {
            "all" -> KeyboardsManager.getKeyboards()
            "detached" -> KeyboardsManager.getKeyboards().filter { it.keyboardLocation == null }.toList()
            else -> return Result(HttpStatusCode.BadRequest, "Unknown '$filter' filter parameter")
        }
        return Result(HttpStatusCode.OK, keyboards)
    }

    fun addKeyboard(request: AddKeyboardRequest): Result {
        validateSchema(request)
        validateKeyboardAddition(request.newKeyboard)
        request.newKeyboard.keyboardLocation?.let {
            // TODO: !!! critical importance
            // addButton(it.hostKeyboard, Button(it.linkButton, "keyboard", keyboard = request.newKeyboard.name))
        }
        addKeyboard(request.newKeyboard)

        KeyboardsManager.reloadKeyboards()

        return Result(HttpStatusCode.OK, "Keyboard successfully added")
    }

    fun deleteKeyboard(request: DeleteKeyboardRequest): Result {
        validateSchema(request)
        validateKeyboardDeletion(request)

        val keyboard = KeyboardsManager.getKeyboard(request.keyboard)
        if (keyboard?.keyboardLocation != null)
            deleteButton(keyboard.keyboardLocation.hostKeyboard, keyboard.keyboardLocation.linkButton)

        deleteKeyboard(keyboard!!.name)
        KeyboardsManager.keyboardStates.delete(keyboard.name)

        if (request.recursively) {
            keyboard.buttons.forEach {
                if (it.type == "payload")
                    deleteButton(keyboard.name, it.text)
                if (it.type == "keyboard")
                    deleteKeyboard(request.keyboard, true)
            }
        } else {
            keyboard.buttons.forEach {
                if (it.type == "payload")
                    deleteButton(keyboard.name, it.text)
                if (it.type == "keyboard")
                    setKeyboardLocation(keyboard.name, null)
            }
        }

        KeyboardsManager.reloadKeyboards()
        return Result(HttpStatusCode.OK, "Keyboard successfully deleted")
    }

    fun linkKeyboard(request: LinkKeyboardRequest): Result {
        validateSchema(request)
        validateKeyboardLinking(request)

        // TODO: !!! critical importance
        // addButton(
        //     request.keyboardLocation.hostKeyboard,
        //     Button(request.keyboardLocation.linkButton, "keyboard", keyboard = request.keyboardName)
        // )
        setKeyboardLocation(request.keyboardName, request.keyboardLocation)

        KeyboardsManager.reloadKeyboards()
        return Result(HttpStatusCode.OK, "Keyboard successfully linked")
    }

    /* TODO: probably it's not necessary
   fun detachKeyboard(request: DetachKeyboardRequest): Result {
       if (!request.validateSchema().isSuccess)
           return Result(HttpStatusCode.BadRequest, "Not valid json schema")
       if (request.keyboard == "MainKeyboard") // TODO: move to constants
           return Result(HttpStatusCode.BadRequest, "'MainKeyboard' can't be detached")
       if (!isKeyboardExist(request.keyboard))
           return Result(HttpStatusCode.OK, "Keyboard '${request.keyboard}' doesn't exist")

       val keyboard = getKeyboard(request.keyboard)
       if (keyboard?.keyboardLocation == null)
           return Result(HttpStatusCode.BadRequest, "Keyboard '${request.keyboard}' already detached")

       deleteButton(keyboard.keyboardLocation.hostKeyboard, keyboard.keyboardLocation.linkButton)
       setKeyboardLocation(keyboard.name, null)

       KeyboardsManager.reloadKeyboards()
       return Result(HttpStatusCode.OK, "Keyboard successfully detached")
   } */


    // Buttons handling

    fun addButton(request: AddButtonRequest): Result {
        validateSchema(request)
        validateButtonAddition(request.newButton)
        addButton(request.newButton)

        KeyboardsManager.reloadKeyboards()
        return Result(HttpStatusCode.OK, "Button successfully added")
    }

    fun deleteButton(request: DeleteButtonRequest): Result {
        validateSchema(request)
        validateButtonDeletion(request)

        val button = getButton(request.keyboard, request.buttonText)!!
        if (button.type == "keyboard")
            setKeyboardLocation(request.keyboard, null)
        deleteButton(request.keyboard, request.buttonText)

        KeyboardsManager.reloadKeyboards()
        return Result(HttpStatusCode.OK, "Button deleted successfully")
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

    private fun isTextReserved(text: String): Boolean = ReservedString.values().find { it.text == text } != null

    private fun addKeyboard(keyboard: Keyboard) {
        MongoClient.create(mongoCollection, keyboard, Keyboard::class.java)
    }

    private fun addButton(button: Button) {
        MongoClient.update(
            mongoCollection,
            Keyboard::class.java,
            BasicDBObject("name", button.hostKeyboard),
            BasicDBObject("\$push", BasicDBObject("buttons", button))
        )
    }

    private fun addButton(keyboard: String, button: Button) {
        MongoClient.update(
            mongoCollection,
            Keyboard::class.java,
            BasicDBObject("name", keyboard),
            BasicDBObject("\$push", BasicDBObject("buttons", button))
        )
    }

    private fun deleteKeyboard(keyboardName: String) {
        MongoClient.delete(mongoCollection, BasicDBObject("name", keyboardName))
    }

    private fun deleteKeyboard(keyboardName: String, recursively: Boolean) {
        if (recursively) {
            val keyboard = getKeyboard(keyboardName)
            keyboard!!.buttons.forEach {
                if (it.type == "payload")
                    deleteButton(keyboard.name, it.text)
                if (it.type == "keyboard") {
                    deleteKeyboard(keyboard.name)
                    deleteKeyboard(keyboard.name, true)
                }
            }
        }
    }

    private fun deleteButton(keyboard: String, buttonText: String) {
        MongoClient.update(
            mongoCollection,
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
        MongoClient.update(mongoCollection, Keyboard::class.java, BasicDBObject("name", keyboard), query)
    }


    // Validators

    private fun validateSchema(request: Request): Result? {
        if (!request.validateSchema().isSuccess)
            return Result(HttpStatusCode.BadRequest, "Not valid json schema")
        return null
    }

    private fun validateNames(vararg names: String?): Result? {
        names.forEach { name ->
            if (ReservedString.values().any { it.text == name })
                return Result(HttpStatusCode.BadRequest, "'$name' is reserved and can't be used")
        }
        return null
    }

    private fun validateKeyboardAddition(keyboard: Keyboard): Result? {
        validateNames(keyboard.name, keyboard.keyboardLocation?.linkButton ?: " ")
        if (isKeyboardExist(keyboard.name))
            return Result(HttpStatusCode.OK, "Keyboard '${keyboard.name}' already exists")
        keyboard.keyboardLocation?.let {
            if (!isKeyboardExist(it.hostKeyboard))
                return Result(HttpStatusCode.OK, "Keyboard '${it.hostKeyboard}' doesn't exist")
            if (isButtonExist(it.hostKeyboard, it.linkButton))
                return Result(HttpStatusCode.OK, "Button '${it.linkButton}' already exists")
        }
        return null
    }

    private fun validateButtonAddition(button: Button): Result? {
        validateNames(button.text, button.payload)
        if (!isKeyboardExist(button.hostKeyboard))
            return Result(HttpStatusCode.OK, "Keyboard '${button.hostKeyboard}' doesn't exist")
        if (isButtonExist(button.hostKeyboard, button.text))
            return Result(HttpStatusCode.OK, "Button '${button.text}' already exists")
        if (button.type == "keyboard" && (button.hostKeyboard == button.keyboard))
            return Result(HttpStatusCode.OK, "Button can't leads to it's host keyboard")
        return null
    }

    private fun validateKeyboardDeletion(request: DeleteKeyboardRequest): Result? {
        if (request.keyboard == ReservedString.MAIN_KEYBOARD.text)
            return Result(HttpStatusCode.BadRequest, "'MainKeyboard' can't be deleted")
        if (!isKeyboardExist(request.keyboard))
            return Result(HttpStatusCode.OK, "'${request.keyboard}' keyboard doesn't exist")

        return null
    }

    private fun validateButtonDeletion(request: DeleteButtonRequest): Result? {
        if (!isKeyboardExist(request.keyboard))
            return Result(HttpStatusCode.OK, "Keyboard '${request.keyboard}' doesn't exist")
        if (!isButtonExist(request.keyboard, request.buttonText))
            return Result(HttpStatusCode.OK, "Button '${request.buttonText}' doesn't exist")
        return null
    }

    private fun validateKeyboardLinking(request: LinkKeyboardRequest): Result? {
        if (request.keyboardName == ReservedString.MAIN_KEYBOARD.text)
            return Result(HttpStatusCode.BadRequest, "'MainKeyboard' can't be linked/detached")
        if (!isKeyboardExist(request.keyboardName))
            return Result(HttpStatusCode.OK, "Keyboard '${request.keyboardName}' doesn't exist")
        getKeyboard(request.keyboardName)?.keyboardLocation?.let {
            // TODO: add ability to re-link linked keyboard
            return Result(HttpStatusCode.BadRequest, "Keyboard '${request.keyboardName}' already linked")
        }
        if (!isKeyboardExist(request.keyboardLocation.hostKeyboard))
            return Result(HttpStatusCode.OK, "Keyboard '${request.keyboardLocation.hostKeyboard}' doesn't exist")
        if (isButtonExist(request.keyboardLocation.hostKeyboard, request.keyboardLocation.linkButton))
            return Result(HttpStatusCode.OK, "Button '${request.keyboardLocation.linkButton}' already exists")
        return null
    }

    private fun validation(code: KeyboardsHandler.() -> Result?): Result? {
        return let(code)
    }
}
