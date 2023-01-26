package server.routes

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import server.handlers.ContentHandler
import server.models.requests.data.CreateButtonRequest
import server.models.requests.data.DeleteButtonRequest
import server.models.requests.data.EditButtonRequest
import server.models.requests.data.LinkButtonRequest

fun Application.buttons(section: String, handler: ContentHandler) {
    routing {
        authenticate {
            route("$section/get") {
                get {
                    handler.getButtons(call.request.queryParameters["filter"] ?: "all").apply {
                        call.respond(this.httpCode, this.content)
                    }
                }
                get("/{button_id}") {
                    handler.getButton(call.parameters["button_id"].toString()).apply {
                        call.respond(this.httpCode, this.content)
                    }
                }
            }
            post("$section/create") {
                handler.handleRequest(call.receive<CreateButtonRequest>()).apply {
                    call.respond(this.httpCode, this.content)
                }
            }
            delete("$section/delete") {
                handler.handleRequest(call.receive<DeleteButtonRequest>()).apply {
                    call.respond(this.httpCode, this.content)
                }
            }
            put("$section/edit") {
                handler.handleRequest(call.receive<EditButtonRequest>()).apply {
                    call.respond(this.httpCode, this.content)
                }
            }
            put("$section/link") {
                handler.handleRequest(call.receive<LinkButtonRequest>()).apply {
                    call.respond(this.httpCode, this.content)
                }
            }
        }
    }
}