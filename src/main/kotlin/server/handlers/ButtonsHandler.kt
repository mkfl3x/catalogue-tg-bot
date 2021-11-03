package server.handlers

import com.mongodb.BasicDBObject
import database.Keyboard
import database.MongoClient
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.util.pipeline.*
import keyboards.KeyboardsManager
import org.slf4j.LoggerFactory
import server.AddButtonRequest
import server.DeleteButtonRequest
import utils.Properties

class ButtonsHandler {

    private val logger = LoggerFactory.getLogger(this.javaClass.name)
    private val mongoCollection = Properties.get("mongo.collection.keyboards")

    // TODO: add handling Mongo operations result
    // TODO: check button model by schema
    suspend fun addButton(request: AddButtonRequest, pipeline: PipelineContext<Unit, ApplicationCall>) {
        if (!doesKeyboardExists(request.keyboard, pipeline)) return
        MongoClient.update(
            mongoCollection,
            Keyboard::class.java,
            BasicDBObject("name", request.keyboard),
            BasicDBObject("\$push", BasicDBObject("buttons", request.button))
        )
        pipeline.call.respond(
            HttpStatusCode.OK,
            "Button '${request.button.text}' successfully added to '${request.keyboard}' keyboard"
        )
    }

    // TODO: add handling Mongo operations result
    // TODO: delete linked keyboard also
    // TODO: check button model by schema
    suspend fun deleteButton(request: DeleteButtonRequest, pipeline: PipelineContext<Unit, ApplicationCall>) {
        if (!doesKeyboardExists(request.keyboard, pipeline)) return
        MongoClient.update(
            mongoCollection,
            Keyboard::class.java,
            BasicDBObject("name", request.keyboard),
            BasicDBObject("\$pull", BasicDBObject("buttons", BasicDBObject("text", request.buttonText)))
        )
        pipeline.call.respond(
            HttpStatusCode.OK,
            "Button '${request.buttonText}' successfully removed from '${request.keyboard}' keyboard"
        )
    }

    private suspend fun doesKeyboardExists(name: String, pipeline: PipelineContext<Unit, ApplicationCall>): Boolean {
        return if (!KeyboardsManager.getAllKeyboardNames().contains(name)) {
            logger.info("Adding new button failed: Keyboard '$name' doesn't exist")
            pipeline.call.respond(HttpStatusCode.Conflict, "Keyboard '$name' doesn't exist")
            false
        } else true
    }
}