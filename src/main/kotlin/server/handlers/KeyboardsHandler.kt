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
import utils.GsonMapper
import utils.Properties
import utils.SchemaValidator

class KeyboardsHandler {

    // TODO: link new keyboard to some button
    suspend fun addKeyboard(keyboardJson: String, pipeline: PipelineContext<Unit, ApplicationCall>) {
        if (!SchemaValidator.isValid(keyboardJson, Schemas.KEYBOARD)) {
            pipeline.call.respond(HttpStatusCode.NotAcceptable, "Not valid keyboard model")
            return
        }
        val pojoKeyboard = GsonMapper.deserialize(keyboardJson, Keyboard::class.java)
        if (KeyboardsManager.getAllKeyboards().contains(pojoKeyboard.name)) {
            // TODO: add error log entry
            pipeline.call.respond(HttpStatusCode.Conflict, "Adding already existing keyboard")
            return
        }
        MongoClient.create(Properties.get("mongo.collection.keyboards"), pojoKeyboard, Keyboard::class.java)
    }

    // TODO: add analyzing of delete result
    fun deleteKeyboard(name: String) {
        MongoClient.delete(Properties.get("mongo.collection.keyboards"), BasicDBObject("name", name))
    }
}