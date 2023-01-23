package server.routes

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import server.handlers.ContentHandler
import server.models.requests.data.AddPayloadRequest
import server.models.requests.data.DeletePayloadRequest
import server.models.requests.data.EditPayloadRequest

fun Application.payloads(section: String, handler: ContentHandler) {
    routing {
        authenticate {
            get("$section/get") {
                handler.getPayloads().apply {
                    call.respond(this.httpCode, this.content)
                }
            }
            post("$section/add") {
                handler.handleRequest(call.receive<AddPayloadRequest>()).apply {
                    call.respond(this.httpCode, this.content)
                }
            }
            put("$section/edit") {
                handler.handleRequest(call.receive<EditPayloadRequest>()).apply {
                    call.respond(this.httpCode, this.content)
                }
            }
            delete("$section/delete") {
                handler.handleRequest(call.receive<DeletePayloadRequest>()).apply {
                    call.respond(this.httpCode, this.content)
                }
            }
        }
    }
}