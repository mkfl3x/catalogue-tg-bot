package server.handlers

import com.mongodb.BasicDBObject
import database.MongoClient
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.util.pipeline.*
import keyboards.Button
import keyboards.Keyboard
import keyboards.KeyboardsManager
import org.bson.BsonNull
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import server.*
import utils.GsonMapper
import utils.Properties
import utils.SchemaValidator

class KeyboardsHandler {

    // TODO: add returns after errorReport

    private val logger: Logger = LoggerFactory.getLogger(this.javaClass.name)
    private val mongoCollection = Properties.get("mongo.collection.keyboards")

    // Keyboards handling

    suspend fun getAllKeyboards(pipeline: PipelineContext<Unit, ApplicationCall>) {
        KeyboardsManager.getKeyboards()
        pipeline.call.respond(KeyboardsManager.getKeyboards())
    }

    suspend fun addKeyboard(request: AddKeyboardRequest, pipeline: PipelineContext<Unit, ApplicationCall>) {
        // TODO: support 'nullable' fields in schema
        // TODO: print results of validation
        // if (!SchemaValidator.isValid(GsonMapper.serialize(request), Schemas.ADD_KEYBOARD_REQUEST)) {
        //     reportError("Schema is not valid", pipeline)
        //     return
        // }

        if (getKeyboard(request.newKeyboard.name) != null) {
            reportError("Keyboard with name '${request.newKeyboard.name}' already exists", pipeline)
            return
        }

        if (request.newKeyboard.keyboardLocation != null) {
            val location = request.newKeyboard.keyboardLocation
            if (getKeyboard(location.hostKeyboard) == null) {
                reportError("Host keyboard '${location.hostKeyboard}' doesn't exist", pipeline)
                return
            }
            if (getButton(
                    location.hostKeyboard,
                    location.linkButton
                ) != null
            ) { // TODO: add method getButtons (of some keyboard)
                reportError("Button '${location.linkButton}' already exists on host keyboard", pipeline)
                return
            }

            addButton(
                location.hostKeyboard,
                Button(location.linkButton, "keyboard", keyboard = request.newKeyboard.name)
            )
        }

        addKeyboard(request.newKeyboard)

        KeyboardsManager.reloadKeyboards()
        pipeline.call.respond(HttpStatusCode.OK, "Keyboard added successfully")
    }

    suspend fun deleteKeyboard(request: DeleteKeyboardRequest, pipeline: PipelineContext<Unit, ApplicationCall>) {
        if (request.keyboard == "MainKeyboard") {
            reportError("MainKeyboard can't be deleted", pipeline)
            return
        }

        if (!SchemaValidator.isValid(GsonMapper.serialize(request), Schemas.DELETE_KEYBOARD_REQUEST)) {
            reportError("Schema is not valid", pipeline)
            return
        }

        val keyboard = getKeyboard(request.keyboard)
        if (keyboard == null) {
            reportError("'${request.keyboard}' keyboard doesn't exist", pipeline)
            return
        } else {


            // detach nested keyboards


            if (keyboard.keyboardLocation != null)
                deleteButton(keyboard.keyboardLocation.hostKeyboard, keyboard.keyboardLocation.linkButton)
            deleteKeyboard(keyboard.name)
        }

        // TODO: delete keyboard from states
        // TODO: set as detached (empty host)
        // TODO: delete all nested keyboards
        KeyboardsManager.reloadKeyboards()
        pipeline.call.respond(HttpStatusCode.OK, "Keyboard deleted successfully")
    }

    suspend fun linkKeyboard(request: LinkKeyboardRequest, pipeline: PipelineContext<Unit, ApplicationCall>) {
        if (request.keyboardName == "MainKeyboard") {
            reportError("MainKeyboard can't be re-linked", pipeline)
            return
        }

        if (getKeyboard(request.keyboardName) == null) {
            reportError("${request.keyboardName} doesn't exist", pipeline)
            return
        }

        if (getKeyboard(request.keyboardName)!!.keyboardLocation != null) {
            reportError("Can't relink linked keyboard", pipeline)
            return
        }

        if (!SchemaValidator.isValid(GsonMapper.serialize(request), Schemas.LINK_KEYBOARD_REQUEST)) {
            reportError("Schema is not valid", pipeline)
            return
        }

        val hostKeyboard = getKeyboard(request.keyboardLocation.hostKeyboard)
        if (hostKeyboard == null) {
            reportError("Host keyboard '${request.keyboardLocation.hostKeyboard}' doesn't exist", pipeline)
            return
        }

        if (hostKeyboard.buttons.firstOrNull { it.text == request.keyboardLocation.linkButton } != null) {
            reportError("Button '${request.keyboardLocation.linkButton}' already exists on host keyboard", pipeline)
            return
        }

        addButton(
            request.keyboardLocation.hostKeyboard,
            Button(request.keyboardLocation.linkButton, "keyboard", keyboard = request.keyboardName)
        )
        MongoClient.update(
            mongoCollection,
            Keyboard::class.java,
            BasicDBObject("name", request.keyboardName),
            BasicDBObject("\$set", BasicDBObject("keyboard_location", request.keyboardLocation))
        )

        KeyboardsManager.reloadKeyboards()
        pipeline.call.respond(HttpStatusCode.OK, "Keyboard linked successfully")
    }

    suspend fun detachKeyboard(request: DetachKeyboardRequest, pipeline: PipelineContext<Unit, ApplicationCall>) {
        if (request.keyboard == "MainKeyboard") {
            reportError("MainKeyboard can't be detached", pipeline)
            return
        }

        if (getKeyboard(request.keyboard) == null) {
            reportError("${request.keyboard} doesn't exist", pipeline)
            return
        }

        val location = getKeyboard(request.keyboard)!!.keyboardLocation
        if (location == null) {
            reportError("Keyboard already detached", pipeline)
            return
        }

        if (!SchemaValidator.isValid(GsonMapper.serialize(request), Schemas.DETACH_KEYBOARD_REQUEST)) {
            reportError("Schema is not valid", pipeline)
            return
        }

        deleteButton(location.hostKeyboard, location.linkButton)
        MongoClient.update(
            mongoCollection,
            Keyboard::class.java,
            BasicDBObject("name", request.keyboard),
            BasicDBObject("\$unset", BasicDBObject("keyboard_location", BsonNull()))
        )

        KeyboardsManager.reloadKeyboards()
        pipeline.call.respond(HttpStatusCode.OK, "Keyboard detached successfully")
    }


// Buttons handling

    suspend fun addButton(request: AddButtonRequest, pipeline: PipelineContext<Unit, ApplicationCall>) {
        if (!SchemaValidator.isValid(GsonMapper.serialize(request), Schemas.ADD_BUTTON_REQUEST))
            reportError("Schema is not valid", pipeline)

        if (request.keyboard == request.newButton.keyboard)
            reportError("Schema is not valid", pipeline)

        if (getKeyboard(request.keyboard) == null)
            reportError("Keyboard '${request.keyboard}' doesn't exist", pipeline)

        addButton(request.keyboard, request.newButton)

        KeyboardsManager.reloadKeyboards()
        pipeline.call.respond(HttpStatusCode.OK, "Button added successfully")
    }

    suspend fun deleteButton(request: DeleteButtonRequest, pipeline: PipelineContext<Unit, ApplicationCall>) {
        if (!SchemaValidator.isValid(GsonMapper.serialize(request), Schemas.DELETE_BUTTON_REQUEST))
            reportError("Schema is not valid", pipeline)

        if (getKeyboard(request.keyboard) == null)
            reportError("Keyboard '${request.keyboard}' doesn't exist", pipeline)

        val button = getButton(request.keyboard, request.buttonText)
        if (button == null)
            reportError("Button '${request.buttonText}' doesn't exist", pipeline)

        if (button!!.type == "keyboard")
            deleteKeyboard(button.keyboard!!)
        deleteButton(request.keyboard, request.buttonText)

        // if button has keyboard type - detach keyboard
        // if  remove all nested keyboards
        KeyboardsManager.reloadKeyboards()
        pipeline.call.respond(HttpStatusCode.OK, "Button deleted successfully")
    }

// Service methods

    private suspend fun reportError(error: String, pipeline: PipelineContext<Unit, ApplicationCall>) {
        logger.error(error)
        pipeline.call.respond(HttpStatusCode.BadRequest, error)
    }

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

    private fun deleteButton(keyboard: String, buttonText: String) {
        MongoClient.update(
            mongoCollection,
            Keyboard::class.java,
            BasicDBObject("name", keyboard),
            BasicDBObject("\$pull", BasicDBObject("buttons", BasicDBObject("text", buttonText)))
        )
    }

    private fun getKeyboard(keyboardName: String): Keyboard? =
        KeyboardsManager.getKeyboard(keyboardName)

    private fun getButton(keyboardName: String, buttonText: String): Button? =
        getKeyboard(keyboardName)?.buttons?.firstOrNull { it.text == buttonText }

    private fun deleteKeyboard(keyboard: Keyboard, recursively: Boolean) {
        if (!recursively) {
            // TODO: detach nested keyboards and buttons
            MongoClient.delete(mongoCollection, BasicDBObject("name", keyboard.name))
        } else {
            keyboard.buttons
                .filter { it.type == "keyboard" }
                .forEach { deleteKeyboard(getKeyboard(it.keyboard!!)!!, true) }
        }
    }

    private fun deleteKeyboard(keyboardName: String) {
        MongoClient.delete(mongoCollection, BasicDBObject("name", keyboardName))
    }
}