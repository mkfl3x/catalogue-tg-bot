package server.routes

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import server.handlers.AuthHandler
import server.models.requests.auth.AuthRequest

fun Application.authorization(handler: AuthHandler) {
    routing {
        post("/auth") {
            handler.authorize(call.receive<AuthRequest>()).apply {
                call.respond(this.httpCode, this.content)
            }
        }
    }
}