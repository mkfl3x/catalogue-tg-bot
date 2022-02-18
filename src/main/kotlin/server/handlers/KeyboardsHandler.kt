package server.handlers

import com.mongodb.BasicDBObject
import database.MongoClient
import keyboards.KeyboardsManager
import keyboards.KeyboardsManager.getAllKeyboards
import keyboards.KeyboardsManager.getButton
import keyboards.KeyboardsManager.getKeyboard
import keyboards.KeyboardsManager.keyboardStates
import keyboards.models.Button
import keyboards.models.Keyboard
import keyboards.models.KeyboardLocation
import org.bson.BsonNull
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import server.models.*

class KeyboardsHandler(private val mongoKeyboards: String) {

    private val logger: Logger = LoggerFactory.getLogger(this.javaClass.name)

    fun getKeyboards(filter: String): Result {
        val keyboards = when (filter) {
            "all" -> getAllKeyboards()
            "detached" -> getAllKeyboards()
                .filter { it.keyboardLocation == null }
                .toList()
            else -> return Result.error(Error.UNKNOWN_PARAMETER)
        }
        return Result.success(keyboards)
    }

    fun addKeyboard(request: AddKeyboardRequest): Result {
        return handleRequest(request) {
            request.newKeyboard.keyboardLocation?.let {
                addButton(it.hostKeyboard, Button(it.linkButton, "keyboard", keyboard = request.newKeyboard.name))
            }
            addKeyboard(request.newKeyboard)
        }
    }

    fun deleteKeyboard(request: DeleteKeyboardRequest): Result {
        return handleRequest(request) {

            val keyboard = getKeyboard(request.keyboard)

            // Drop from states
            keyboardStates.delete(keyboard!!.name)

            // Delete leads button on host keyboard
            keyboard.keyboardLocation?.let {
                deleteButton(it.hostKeyboard, it.linkButton)
            }

            // Detach/delete nested keyboards and remove buttons
            if (request.recursively)
                deleteKeyboard(request.keyboard, true)
            else
                keyboard.buttons.filter { it.type == "keyboard" }.map { detachKeyboard(it.keyboard!!) }

            // Delete keyboard
            deleteKeyboard(keyboard.name, false)
        }
    }

    fun linkKeyboard(request: LinkKeyboardRequest): Result {
        return handleRequest(request) {
            addButton(
                request.keyboardLocation.hostKeyboard,
                Button(request.keyboardLocation.linkButton, "keyboard", keyboard = request.keyboardName)
            )
            setKeyboardLocation(request.keyboardName, request.keyboardLocation)
        }
    }

    fun detachKeyboard(request: DetachKeyboardRequest): Result {
        return handleRequest(request) {
            detachKeyboard(request.keyboard)
        }
    }

    fun addButton(request: AddButtonRequest): Result {
        return handleRequest(request) {
            addButton(request.hostKeyboard, request.newButton)
        }
    }

    fun deleteButton(request: DeleteButtonRequest): Result {
        return handleRequest(request) {
            val button = getButton(request.keyboard, request.buttonText)!!
            if (button.type == "keyboard")
                setKeyboardLocation(button.keyboard!!, null)
            deleteButton(request.keyboard, request.buttonText)
        }
    }

    private fun handleRequest(request: Request, code: () -> Unit): Result {
        request.validateSchema()?.let { return it }
        request.validateData()?.let { return it }
        code.invoke()
        KeyboardsManager.reloadKeyboards()
        return Result.success(request.successMessage)
    }

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
        if (recursively) {
            getKeyboard(keyboardName)!!.buttons
                .filter { it.type == "keyboard" }
                .map { deleteKeyboard(it.keyboard!!, true) }
        }
        MongoClient.delete(mongoKeyboards, BasicDBObject("name", keyboardName))
    }

    private fun detachKeyboard(keyboard: String) {
        getKeyboard(keyboard)?.let {
            deleteButton(it.keyboardLocation!!.hostKeyboard, it.keyboardLocation.linkButton)
            setKeyboardLocation(it.name, null)
            keyboardStates.delete(keyboard)
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
}
