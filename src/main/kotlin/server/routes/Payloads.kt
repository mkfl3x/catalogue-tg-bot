package server.routes

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import server.handlers.ContentHandler
import server.models.requests.Requests

fun Application.payloads(section: String, handler: ContentHandler) {
    routing {
        get("$section/get") {
            val result = handler.getPayloads()
            call.respond(result.responseCode, result.responseData)
        }
        post("$section/add") {
            val result = handler.handleRequest(call.receive(), Requests.ADD_PAYLOAD_REQUEST)
            call.respond(result.responseCode, result.responseData)
        }
        delete("$section/delete") {
            val result = handler.handleRequest(call.receive(), Requests.DELETE_PAYLOAD_REQUEST)
            call.respond(result.responseCode, result.responseData)
        }
    }
}