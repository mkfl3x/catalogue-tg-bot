package server.handlers

import com.mongodb.BasicDBObject
import database.MongoClient
import io.ktor.http.*
import keyboards.Button
import keyboards.Keyboard
import keyboards.KeyboardLocation
import keyboards.KeyboardsManager
import org.bson.BsonNull
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import server.*
import utils.Properties

data class Result(
    val responseCode: HttpStatusCode,
    val responseData: Any
)

class KeyboardsHandler {

    // TODO: add logging

    private val logger: Logger = LoggerFactory.getLogger(this.javaClass.name)
    private val mongoCollection = Properties.get("mongo.collection.keyboards")

    // Keyboards

    fun getKeyboards(filter: String): Result {
        val keyboards = when (filter) {
            "all" -> KeyboardsManager.getKeyboards()
            "detached" -> KeyboardsManager.getKeyboards().filter { it.keyboardLocation == null }.toList()
            else -> return Result(HttpStatusCode.BadRequest, "Unknown '$filter' filter parameter")
        }
        return Result(HttpStatusCode.OK, keyboards)
    }

    fun addKeyboard(request: AddKeyboardRequest): Result {
        if (!request.validateSchema().isSuccess)
            return Result(HttpStatusCode.BadRequest, "Not valid json schema")
        if (isKeyboardExist(request.newKeyboard.name))
            return Result(HttpStatusCode.OK, "Keyboard '${request.newKeyboard.name}' already exists")

        val location = request.newKeyboard.keyboardLocation
        if (location != null) {
            if (!isKeyboardExist(location.hostKeyboard))
                return Result(HttpStatusCode.OK, "Keyboard '${location.hostKeyboard}' doesn't exist")
            if (isButtonExist(location.hostKeyboard, location.linkButton))
                return Result(HttpStatusCode.OK, "Button '${location.linkButton}' already exists")
            addButton(
                location.hostKeyboard,
                Button(location.linkButton, "keyboard", keyboard = request.newKeyboard.name)
            )
        }
        addKeyboard(request.newKeyboard)

        KeyboardsManager.reloadKeyboards()
        return Result(HttpStatusCode.OK, "Keyboard successfully added")
    }

    fun deleteKeyboard(request: DeleteKeyboardRequest): Result {
        if (!request.validateSchema().isSuccess)
            return Result(HttpStatusCode.BadRequest, "Not valid json schema")
        if (request.keyboard == "MainKeyboard") // TODO: move to constants
            return Result(HttpStatusCode.BadRequest, "'MainKeyboard' can't be deleted")
        if (!isKeyboardExist(request.keyboard))
            return Result(HttpStatusCode.OK, "'${request.keyboard}' keyboard doesn't exist")

        val keyboard = KeyboardsManager.getKeyboard(request.keyboard)
        if (keyboard?.keyboardLocation != null)
            deleteButton(keyboard.keyboardLocation.hostKeyboard, keyboard.keyboardLocation.linkButton)

        deleteKeyboard(keyboard!!.name)
        // TODO: delete from states

        if (request.recursively) {
            // TODO: delete all nested buttons and keyboards
        } else {
            // TODO: delete nested buttons, detach keyboards
        }

        KeyboardsManager.reloadKeyboards()
        return Result(HttpStatusCode.OK, "Keyboard successfully deleted")
    }

    fun linkKeyboard(request: LinkKeyboardRequest): Result {
        if (!request.validateSchema().isSuccess)
            return Result(HttpStatusCode.BadRequest, "Not valid json schema")
        if (request.keyboardName == "MainKeyboard") // TODO: move to constants
            return Result(HttpStatusCode.BadRequest, "'MainKeyboard' can't be linked")
        if (!isKeyboardExist(request.keyboardName))
            return Result(HttpStatusCode.OK, "Keyboard '${request.keyboardName}' doesn't exist")
        if (getKeyboard(request.keyboardName)?.keyboardLocation != null) // TODO: add ability to relink
            return Result(HttpStatusCode.BadRequest, "Keyboard '${request.keyboardName}' already linked")
        if (!isKeyboardExist(request.keyboardLocation.hostKeyboard))
            return Result(HttpStatusCode.OK, "Keyboard '${request.keyboardLocation.hostKeyboard}' doesn't exist")
        if (isButtonExist(request.keyboardLocation.hostKeyboard, request.keyboardLocation.linkButton))
            return Result(HttpStatusCode.OK, "Button '${request.keyboardLocation.linkButton}' already exists")

        addButton(
            request.keyboardLocation.hostKeyboard,
            Button(request.keyboardLocation.linkButton, "keyboard", keyboard = request.keyboardName)
        )
        setKeyboardLocation(request.keyboardName, request.keyboardLocation)

        KeyboardsManager.reloadKeyboards()
        return Result(HttpStatusCode.OK, "Keyboard successfully linked")
    }

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
    }

    // Buttons

    fun addButton(request: AddButtonRequest): Result {
        if (!request.validateSchema().isSuccess)
            return Result(HttpStatusCode.BadRequest, "Not valid json schema")
        if (!isKeyboardExist(request.keyboard))
            return Result(HttpStatusCode.OK, "Keyboard '${request.keyboard}' doesn't exist")
        if (isButtonExist(request.keyboard, request.newButton.text))
            return Result(HttpStatusCode.OK, "Button '${request.newButton.text}' already exists")
        // TODO: add check for closure

        addButton(request.keyboard, request.newButton)

        KeyboardsManager.reloadKeyboards()
        return Result(HttpStatusCode.OK, "Button successfully added")
    }

    fun deleteButton(request: DeleteButtonRequest): Result {
        if (!request.validateSchema().isSuccess)
            return Result(HttpStatusCode.BadRequest, "Not valid json schema")
        if (!isKeyboardExist(request.keyboard))
            return Result(HttpStatusCode.OK, "Keyboard '${request.keyboard}' doesn't exist")

        val button = getButton(request.keyboard, request.buttonText)
            ?: return Result(HttpStatusCode.OK, "Button '${request.buttonText}' doesn't exist")

        if (button.type == "keyboard") {
            // TODO: detach keyboard
        }
        deleteButton(request.keyboard, request.buttonText)

        // if button has keyboard type - detach keyboard
        // if  remove all nested keyboards
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

    private fun addKeyboard(keyboard: Keyboard) {
        MongoClient.create(mongoCollection, keyboard, Keyboard::class.java)
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
}
