package server.routes

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import server.handlers.ContentHandler
import server.models.requests.data.CreatePayloadRequest
import server.models.requests.data.DeletePayloadRequest
import server.models.requests.data.EditPayloadRequest

fun Application.payloads(section: String, handler: ContentHandler) {
    routing {
        authenticate {
            route("/get") {
                get {
                    handler.getPayloads().apply {
                        call.respond(this.httpCode, this.content)
                    }
                }
                get("/{payload_id}") {
                    handler.getPayload(call.parameters["payload_id"].toString()).apply {
                        call.respond(this.httpCode, this.content)
                    }
                }
            }
            post("$section/create") {
                handler.handleRequest(call.receive<CreatePayloadRequest>()).apply {
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