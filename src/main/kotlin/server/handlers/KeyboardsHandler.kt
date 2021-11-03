package server.handlers

import com.mongodb.BasicDBObject
import database.Keyboard
import database.MongoClient
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.util.pipeline.*
import keyboards.KeyboardsManager
import keyboards.Schemas
import server.AddKeyboardRequest
import utils.GsonMapper
import utils.SchemaValidator

class KeyboardsHandler : BaseHandler() {

    fun getAllKeyboards(): MutableList<Keyboard> {
        return KeyboardsManager.getAllKeyboards()
    }

    // TODO: add handling Mongo operations result
    // TODO: link with button also on another keyboard
    suspend fun addKeyboard(addKeyboardRequest: AddKeyboardRequest, pipeline: PipelineContext<Unit, ApplicationCall>) {
        if (!SchemaValidator.isValid(GsonMapper.serialize(addKeyboardRequest.keyboard), Schemas.KEYBOARD)) {
            pipeline.call.respond(HttpStatusCode.NotAcceptable, "Not valid keyboard model")
            return
        }
        val keyboard = addKeyboardRequest.keyboard
        if (KeyboardsManager.getAllKeyboardNames().contains(keyboard.name)) {
            logger.info("Already existing keyboard with '${keyboard.name}' name was tried to add")
            pipeline.call.respond(HttpStatusCode.Conflict, "Adding already existing keyboard")
            return
        }
        MongoClient.create(mongoCollection, keyboard, Keyboard::class.java)
    }

    // TODO: add handling Mongo operations result
    suspend fun deleteKeyboard(name: String, pipeline: PipelineContext<Unit, ApplicationCall>) {
        if (!doesKeyboardExists(name, pipeline)) return
        MongoClient.delete(mongoCollection, BasicDBObject("name", name))
    }
}