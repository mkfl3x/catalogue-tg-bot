package server.routes

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import server.handlers.ContentHandler
import server.models.requests.data.CreateButtonRequest
import server.models.requests.data.DeleteButtonRequest
import server.models.requests.data.LinkButtonRequest
import server.models.requests.data.RenameButtonRequest

fun Application.buttons(handler: ContentHandler) {
    routing {
        authenticate {
            route("/buttons") {
                get {
                    handler.getButtons(call.request.queryParameters["filter"] ?: "all").apply {
                        call.respond(httpCode, content)
                    }
                }
                get("/{button_id}") {
                    handler.getButton(call.parameters["button_id"].toString()).apply {
                        call.respond(httpCode, content)
                    }
                }
                post("/create") {
                    handler.handleRequest(call.receive<CreateButtonRequest>()).apply {
                        call.respond(httpCode, content)
                    }
                }
                delete("/delete") {
                    handler.handleRequest(call.receive<DeleteButtonRequest>()).apply {
                        call.respond(httpCode, content)
                    }
                }
                put("/rename") {
                    handler.handleRequest(call.receive<RenameButtonRequest>()).apply {
                        call.respond(httpCode, content)
                    }
                }
                put("/link") {
                    handler.handleRequest(call.receive<LinkButtonRequest>()).apply {
                        call.respond(httpCode, content)
                    }
                }
            }
        }
    }
}