package server.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.service(section: String) {
    routing {
        authenticate {
            get("$section/ping") {
                call.respond(HttpStatusCode.OK, "I am fine")
            }
        }
    }
}