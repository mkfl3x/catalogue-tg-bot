package server.handlers

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.util.pipeline.*
import keyboards.KeyboardsManager
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import utils.Properties

abstract class BaseHandler {

    protected val logger: Logger = LoggerFactory.getLogger(this.javaClass.name)
    protected val mongoCollection = Properties.get("mongo.collection.keyboards")

    suspend fun doesKeyboardExists(name: String, pipeline: PipelineContext<Unit, ApplicationCall>): Boolean {
        return if (!KeyboardsManager.getAllKeyboardNames().contains(name)) {
            logger.info("Adding new button failed: Keyboard '$name' doesn't exist")
            pipeline.call.respond(HttpStatusCode.Conflict, "Keyboard '$name' doesn't exist")
            false
        } else true
    }
}