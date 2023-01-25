package server.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import server.handlers.ContentHandler
import server.models.requests.data.CreateKeyboardRequest
import server.models.requests.data.DeleteKeyboardRequest
import server.models.requests.data.DetachKeyboardRequest
import server.models.requests.data.UpdateKeyboardButtonRequest

fun Application.keyboards(section: String, handler: ContentHandler) {
    routing {
        authenticate {
            get("$section/get/{keyboard_id}") {
                handler.getKeyboard(call.parameters["keyboard_id"].toString()).apply {
                    call.respond(this.httpCode, this.content)
                }
            }
            get("$section/get") {
                handler.getKeyboards(call.request.queryParameters["filter"] ?: "all").apply {
                    call.respond(this.httpCode, this.content)
                }
            }
            post("$section/create") {
                handler.handleRequest(call.receive<CreateKeyboardRequest>()).apply {
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
            put("$section/rename") {
                // TODO
                call.respond(HttpStatusCode.NotImplemented, "API method under construction")
            }
            put("$section/updateKeyboardButton") {
                handler.handleRequest(call.receive<UpdateKeyboardButtonRequest>()).apply {
                    call.respond(this.httpCode, this.content)
                }
            }
        }
    }
}
