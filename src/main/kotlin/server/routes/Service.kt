package server.routes

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*

fun Application.service(section: String) {

    routing {
        get("$section/ping") {
            call.respond(HttpStatusCode.OK, "I am fine")
        }
    }
}