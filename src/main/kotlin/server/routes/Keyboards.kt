package server.routes

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import server.handlers.ContentHandler
import server.models.requests.data.AddKeyboardRequest
import server.models.requests.data.DeleteKeyboardRequest
import server.models.requests.data.DetachKeyboardRequest

fun Application.keyboards(section: String, handler: ContentHandler) {
    routing {
        get("$section/get") {
            val filter = call.request.queryParameters["filter"] ?: "all"
            handler.getKeyboards(filter).apply {
                call.respond(this.httpCode, this.content)
            }
        }
        post("$section/add") {
            handler.handleRequest(call.receive<AddKeyboardRequest>()).apply {
                call.respond(this.httpCode, this.content)
            }
        }
        put("$section/detach") {
            handler.handleRequest(call.receive<DetachKeyboardRequest>()).apply {
                call.respond(this.httpCode, this.content)
            }
        }
        delete("$section/delete") {
            handler.handleRequest(call.receive<DeleteKeyboardRequest>()).apply {
                call.respond(this.httpCode, this.content)
            }
        }
    }
}
