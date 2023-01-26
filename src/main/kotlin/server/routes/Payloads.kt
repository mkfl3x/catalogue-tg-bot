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

fun Application.payloads(handler: ContentHandler) {
    routing {
        authenticate {
            route("/payloads") {
                get {
                    handler.getPayloads().apply {
                        call.respond(httpCode, content)
                    }
                }
                get("/{payload_id}") {
                    handler.getPayload(call.parameters["payload_id"].toString()).apply {
                        call.respond(httpCode, content)
                    }
                }
                post("/create") {
                    handler.handleRequest(call.receive<CreatePayloadRequest>()).apply {
                        call.respond(httpCode, content)
                    }
                }
                put("/edit") {
                    handler.handleRequest(call.receive<EditPayloadRequest>()).apply {
                        call.respond(httpCode, content)
                    }
                }
                delete("/delete") {
                    handler.handleRequest(call.receive<DeletePayloadRequest>()).apply {
                        call.respond(httpCode, content)
                    }
                }
            }
        }
    }
}