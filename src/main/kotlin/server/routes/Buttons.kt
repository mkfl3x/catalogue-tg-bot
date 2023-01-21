package server.routes

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import server.Requests
import server.handlers.ContentHandler

fun Application.buttons(section: String, handler: ContentHandler) {
    routing {
        get("$section/get") {
            val filter = call.request.queryParameters["filter"] ?: "all"
            handler.getButtons(filter).apply {
                call.respond(this.responseCode, this.responseData)
            }
        }
        post("$section/add") {
            handler.handleRequest(call.receive(), Requests.ADD_BUTTON_REQUEST).apply {
                call.respond(this.responseCode, this.responseData)
            }
        }
        delete("$section/delete") {
            handler.handleRequest(call.receive(), Requests.DELETE_BUTTON_REQUEST).apply {
                call.respond(this.responseCode, this.responseData)
            }
        }
        put("$section/edit") {
            handler.handleRequest(call.receive(), Requests.EDIT_BUTTON_REQUEST).apply {
                call.respond(this.responseCode, this.responseData)
            }
        }
        put("$section/link") {
            handler.handleRequest(call.receive(), Requests.LINK_BUTTON_REQUEST).apply {
                call.respond(this.responseCode, this.responseData)
            }
        }
    }
}