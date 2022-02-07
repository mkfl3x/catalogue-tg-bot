package server.routes

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*

fun Route.serviceRoute(section: String) {

    get("$section/ping") {
        call.respondText("I am fine")
    }
}