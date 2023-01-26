package server.routes

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import server.handlers.ContentHandler
import server.models.requests.data.*

fun Application.keyboards(handler: ContentHandler) {
    routing {
        authenticate {
            route("/keyboards") {
                get {
                    handler.getKeyboards(call.request.queryParameters["filter"] ?: "all").apply {
                        call.respond(httpCode, content)
                    }
                }
                get("/{keyboard_id}") {
                    handler.getKeyboard(call.parameters["keyboard_id"].toString()).apply {
                        call.respond(httpCode, content)
                    }
                }
                post("/create") {
                    handler.handleRequest(call.receive<CreateKeyboardRequest>()).apply {
                        call.respond(httpCode, content)
                    }
                }
                delete("/delete") {
                    handler.handleRequest(call.receive<DeleteKeyboardRequest>()).apply {
                        call.respond(httpCode, content)
                    }
                }
                put("/detach") {
                    handler.handleRequest(call.receive<DetachKeyboardRequest>()).apply {
                        call.respond(httpCode, content)
                    }
                }
                put("/rename") {
                    handler.handleRequest(call.receive<RenameKeyboardRequest>()).apply {
                        call.respond(httpCode, content)
                    }
                }
                put("/updateKeyboardButton") {
                    handler.handleRequest(call.receive<UpdateKeyboardButtonRequest>()).apply {
                        call.respond(httpCode, content)
                    }
                }
            }
        }
    }
}
