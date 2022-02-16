package server.handlers

import com.mongodb.BasicDBObject
import database.MongoClient
import io.ktor.http.*
import keyboards.KeyboardsManager
import keyboards.KeyboardsManager.getButton
import keyboards.KeyboardsManager.getKeyboard
import keyboards.models.Button
import keyboards.models.Keyboard
import keyboards.models.KeyboardLocation
import org.bson.BsonNull
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import server.models.*

class KeyboardsHandler(private val mongoKeyboards: String) {

    // TODO:
    //  - handle exceptions/errors inside methods interacted with mongo
    //  - add logging
    //  - add javadocs


    private val logger: Logger = LoggerFactory.getLogger(this.javaClass.name)

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
        return handleRequest(request) {
            request.newKeyboard.keyboardLocation?.let {
                addButton(it.hostKeyboard, Button(it.linkButton, "keyboard", keyboard = request.newKeyboard.name))
            }
            addKeyboard(request.newKeyboard)
        }
    }

    fun deleteKeyboard(request: DeleteKeyboardRequest): Result {
        return handleRequest(request) {
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
            addButton(request.keyboard, request.newButton)
        }
    }

    fun deleteButton(request: DeleteButtonRequest): Result {
        return handleRequest(request) {
            val button = getButton(request.keyboard, request.buttonText)!!
            if (button.type == "keyboard")
                setKeyboardLocation(request.keyboard, null)
            deleteButton(request.keyboard, request.buttonText)
        }
    }

    // Service

    private fun handleRequest(request: Request, code: () -> Unit): Result {
        request.validateSchema()?.let { return it }
        request.validateRequest()?.let { return it }
        run { code } // TODO: should handle result
        KeyboardsManager.reloadKeyboards()
        return Result(HttpStatusCode.OK, "Keyboard successfully added")
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
}
