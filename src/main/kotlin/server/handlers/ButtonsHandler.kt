package server.handlers

import com.mongodb.BasicDBObject
import database.Keyboard
import database.MongoClient
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.util.pipeline.*
import server.AddButtonRequest
import server.DeleteButtonRequest

class ButtonsHandler : BaseHandler() {

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
}