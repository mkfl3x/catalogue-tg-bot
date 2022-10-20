package server.routes

import io.ktor.application.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import server.handlers.ContentHandler

fun Application.keyboards(section: String, handler: ContentHandler) {
    routing {
        get("$section/get") {
            val filter = call.request.queryParameters["filter"] ?: "all"
            val result = handler.getKeyboards(filter)
            call.respond(result.responseCode, result.responseData)
        }
        post("$section/add") {
            val result = handler.addKeyboard(call.receive())
            call.respond(result.responseCode, result.responseData)
        }
        put("$section/detach") {
            val result = handler.detachKeyboard(call.receive())
            call.respond(result.responseCode, result.responseData)
        }
        delete("$section/delete") {
            val result = handler.deleteKeyboard(call.receive())
            call.respond(result.responseCode, result.responseData)
        }
    }
}
