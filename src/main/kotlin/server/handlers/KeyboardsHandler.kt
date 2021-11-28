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

    // TODO:
    //  - update HTTP codes
    //  - implement more elegance way for return responds

    private val logger: Logger = LoggerFactory.getLogger(this.javaClass.name)
    private val mongoCollection = Properties.get("mongo.collection.keyboards")

    // Keyboards handling

    suspend fun addKeyboard(request: AddKeyboardRequest, pipeline: PipelineContext<Unit, ApplicationCall>) {
        if (!SchemaValidator.isValid(GsonMapper.serialize(request), RequestSchemas.ADD_KEYBOARD_REQUEST)) {
            val error = "Request has wrong model"
            logger.error(error)
            pipeline.call.respond(HttpStatusCode.NotAcceptable, error)
        }
        if (getKeyboard(request.parenKeyboard) == null) {
            val error =
                "Trying to add '${request.newKeyboard}' keyboard linked with '${request.newButton}' button to not existing '${request.parenKeyboard}' parent keyboard"
            logger.error(error)
            pipeline.call.respond(HttpStatusCode.Conflict, error)
        }
        if (getButton(request.parenKeyboard, request.newButton) != null) {
            val error =
                "Trying to add '${request.newKeyboard}' keyboard linked with already existing button '${request.newButton}' on '${request.parenKeyboard}' parent keyboard"
            logger.error(error)
            pipeline.call.respond(HttpStatusCode.Conflict, error)
        }
        if (getKeyboard(request.newKeyboard.name) != null) {
            val error =
                "Trying to add already existing '${request.newKeyboard}' keyboard linked with '${request.newButton}' button on '${request.parenKeyboard}' parent keyboard"
            logger.error(error)
            pipeline.call.respond(HttpStatusCode.Conflict, error)
        }
        addKeyboard(request.newKeyboard)
        addButton(request.parenKeyboard, Button(request.newButton, "keyboard", keyboard = request.newKeyboard.name))
        pipeline.call.respond(HttpStatusCode.OK, "Keyboard added successfully")
    }

    suspend fun deleteKeyboard(request: DeleteKeyboardRequest, pipeline: PipelineContext<Unit, ApplicationCall>) {
        if (!SchemaValidator.isValid(GsonMapper.serialize(request), RequestSchemas.DELETE_KEYBOARD_REQUEST)) {
            val error = "Request has wrong model"
            logger.error(error)
            pipeline.call.respond(HttpStatusCode.NotAcceptable, error)
        }
        val keyboard = getKeyboard(request.keyboard)
        if (keyboard == null) {
            val error = "'${request.keyboard}' keyboard doesn't exist"
            logger.error(error)
            pipeline.call.respond(HttpStatusCode.Conflict, error)
        } else {
            deleteKeyboard(keyboard.name)
            val parentKeyboard = getKeyboard(keyboard.parentKeyboard)
            val linkedButton = parentKeyboard!!.buttons
                .filter { it.type == "keyboard" }
                .first { it.keyboard == keyboard.name }
            deleteButton(parentKeyboard.name, linkedButton.text)
        }
        pipeline.call.respond(HttpStatusCode.OK, "Keyboard deleted successfully")
    }


    // Buttons handling

    suspend fun addButton(request: AddButtonRequest, pipeline: PipelineContext<Unit, ApplicationCall>) {
        if (!SchemaValidator.isValid(GsonMapper.serialize(request), RequestSchemas.ADD_BUTTON_REQUEST)) {
            val error = "Request has wrong model"
            logger.error(error)
            pipeline.call.respond(HttpStatusCode.NotAcceptable, error)
        }
        if (getKeyboard(request.keyboard) == null) {
            val error = "Trying to add '${request.newButton.text}' to not existing '${request.keyboard}' keyboard"
            logger.error(error)
            pipeline.call.respond(HttpStatusCode.Conflict, error)
        }
        addButton(request.keyboard, request.newButton)
        pipeline.call.respond(HttpStatusCode.OK, "Button added successfully")
    }

    suspend fun deleteButton(request: DeleteButtonRequest, pipeline: PipelineContext<Unit, ApplicationCall>) {
        if (!SchemaValidator.isValid(GsonMapper.serialize(request), RequestSchemas.DELETE_BUTTON_REQUEST)) {
            val error = "Request has wrong model"
            logger.error(error)
            pipeline.call.respond(HttpStatusCode.NotAcceptable, error)
        }
        if (getKeyboard(request.keyboard) == null) {
            val error =
                "Trying to delete '${request.buttonText}' button from not existing '${request.keyboard}' keyboard"
            logger.error(error)
            pipeline.call.respond(HttpStatusCode.Conflict, error)
        }
        val button = getButton(request.keyboard, request.buttonText)
        if (button != null) {
            if (button.type == "keyboard")
                deleteKeyboard(button.keyboard!!)
            deleteButton(request.keyboard, request.buttonText)
        } else {
            val error =
                "Trying to delete not existing '${request.buttonText}' button from '${request.keyboard}' keyboard"
            logger.error(error)
            pipeline.call.respond(HttpStatusCode.Conflict, error)
        }
        pipeline.call.respond(HttpStatusCode.OK, "Button deleted successfully")
    }


    // Service methods

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