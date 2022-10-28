package server.routes

import io.ktor.application.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import server.handlers.ContentHandler
import server.models.requests.Requests

fun Application.buttons(section: String, handler: ContentHandler) {
    routing {
        get("$section/get") {
            val filter = call.request.queryParameters["filter"] ?: "all"
            val result = handler.getButtons(filter)
            call.respond(result.responseCode, result.responseData)
        }
        post("$section/add") {
            val result = handler.handleRequest(call.receive(), Requests.ADD_BUTTON_REQUEST)
            call.respond(result.responseCode, result.responseData)
        }
        delete("$section/delete") {
            val result = handler.handleRequest(call.receive(), Requests.DELETE_BUTTON_REQUEST)
            call.respond(result.responseCode, result.responseData)
        }
        put("$section/link") {
            val result = handler.handleRequest(call.receive(), Requests.LINK_BUTTON_REQUEST)
            call.respond(result.responseCode, result.responseData)
        }
    }
}