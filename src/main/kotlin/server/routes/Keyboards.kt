package server.routes

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import server.Requests
import server.handlers.ContentHandler

fun Application.keyboards(section: String, handler: ContentHandler) {
    routing {
        get("$section/get") {
            val filter = call.request.queryParameters["filter"] ?: "all"
            handler.getKeyboards(filter).apply {
                call.respond(this.responseCode, this.responseData)
            }
        }
        post("$section/add") {
            handler.handleRequest(call.receive(), Requests.ADD_KEYBOARD_REQUEST).apply {
                call.respond(this.responseCode, this.responseData)
            }
        }
        put("$section/detach") {
            handler.handleRequest(call.receive(), Requests.DETACH_KEYBOARD_REQUEST).apply {
                call.respond(this.responseCode, this.responseData)
            }
        }
        delete("$section/delete") {
            handler.handleRequest(call.receive(), Requests.DELETE_KEYBOARD_REQUEST).apply {
                call.respond(this.responseCode, this.responseData)
            }
        }
    }
}
