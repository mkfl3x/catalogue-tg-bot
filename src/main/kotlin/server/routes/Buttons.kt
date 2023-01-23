package server.routes

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import server.handlers.ContentHandler
import server.models.requests.data.AddButtonRequest
import server.models.requests.data.DeleteButtonRequest
import server.models.requests.data.EditButtonRequest
import server.models.requests.data.LinkButtonRequest

fun Application.buttons(section: String, handler: ContentHandler) {
    routing {
        authenticate {
            get("$section/get") {
                val filter = call.request.queryParameters["filter"] ?: "all"
                handler.getButtons(filter).apply {
                    call.respond(this.httpCode, this.content)
                }
            }
            post("$section/add") {
                handler.handleRequest(call.receive<AddButtonRequest>()).apply {
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