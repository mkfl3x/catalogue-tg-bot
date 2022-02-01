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
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import server.*
import utils.GsonMapper
import utils.Properties
import utils.SchemaValidator

class KeyboardsHandler {

    private val logger: Logger = LoggerFactory.getLogger(this.javaClass.name)
    private val mongoCollection = Properties.get("mongo.collection.keyboards")

    // Keyboards handling

    suspend fun getAllKeyboards(pipeline: PipelineContext<Unit, ApplicationCall>) {
        KeyboardsManager.getKeyboards()
        pipeline.call.respond(KeyboardsManager.getKeyboards())
    }

    suspend fun addKeyboard(request: AddKeyboardRequest, pipeline: PipelineContext<Unit, ApplicationCall>) {
        if (SchemaValidator.isValid(GsonMapper.serialize(request), Schemas.ADD_KEYBOARD_REQUEST))
            reportError("Schema is not valid", pipeline)

        if (getKeyboard(request.newKeyboard.hostKeyboard) == null)
            reportError("Host keyboard '${request.newKeyboard.hostKeyboard}' doesn't exist", pipeline)

        if (getButton(request.newKeyboard.hostKeyboard, request.newButton) != null)
            reportError("Button '${request.newButton}' already exists on host keyboard", pipeline)

        if (getKeyboard(request.newKeyboard.name) != null)
            reportError("Keyboard with name '${request.newKeyboard.name}' already exists", pipeline)

        addKeyboard(request.newKeyboard)
        addButton(
            request.newKeyboard.hostKeyboard,
            Button(request.newButton, "keyboard", keyboard = request.newKeyboard.name)
        )

        KeyboardsManager.reloadKeyboards()
        pipeline.call.respond(HttpStatusCode.OK, "Keyboard added successfully")
    }

    suspend fun deleteKeyboard(request: DeleteKeyboardRequest, pipeline: PipelineContext<Unit, ApplicationCall>) {
        if (SchemaValidator.isValid(GsonMapper.serialize(request), Schemas.DELETE_KEYBOARD_REQUEST))
            reportError("Schema is not valid", pipeline)

        if (getKeyboard(request.keyboard) == null) {
            reportError("'${request.keyboard}' keyboard doesn't exist", pipeline)
        } else {
            deleteKeyboard(request.keyboard)
            val hostKeyboard = getKeyboard(request.hostKeyboard)
            val linkedButton = hostKeyboard!!.buttons
                .filter { it.type == "keyboard" }
                .first { it.keyboard == request.keyboard }
            deleteButton(hostKeyboard.name, linkedButton.text)
        }

        KeyboardsManager.reloadKeyboards()
        pipeline.call.respond(HttpStatusCode.OK, "Keyboard deleted successfully")
    }

    // Buttons handling

    suspend fun addButton(request: AddButtonRequest, pipeline: PipelineContext<Unit, ApplicationCall>) {
        if (SchemaValidator.isValid(GsonMapper.serialize(request), Schemas.ADD_BUTTON_REQUEST))
            reportError("Schema is not valid", pipeline)

        if (getKeyboard(request.keyboard) == null)
            reportError("Keyboard '${request.keyboard}' doesn't exist", pipeline)

        addButton(request.keyboard, request.newButton)

        KeyboardsManager.reloadKeyboards()
        pipeline.call.respond(HttpStatusCode.OK, "Button added successfully")
    }

    suspend fun deleteButton(request: DeleteButtonRequest, pipeline: PipelineContext<Unit, ApplicationCall>) {
        if (SchemaValidator.isValid(GsonMapper.serialize(request), Schemas.DELETE_BUTTON_REQUEST))
            reportError("Schema is not valid", pipeline)

        if (getKeyboard(request.keyboard) == null)
            reportError("Keyboard '${request.keyboard}' doesn't exist", pipeline)

        val button = getButton(request.keyboard, request.buttonText)
        if (button == null)
            reportError("Button '${request.buttonText}' doesn't exist", pipeline)

        if (button!!.type == "keyboard")
            deleteKeyboard(button.keyboard!!)
        deleteButton(request.keyboard, request.buttonText)

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

    private fun deleteKeyboard(keyboardName: String) {
        MongoClient.delete(mongoCollection, BasicDBObject("name", keyboardName))
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
}