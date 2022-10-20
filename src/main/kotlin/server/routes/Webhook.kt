package server.routes

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import server.handlers.WebhookHandler

fun Application.telegram(endpoint: String, handler: WebhookHandler) {

    routing {
        post(endpoint) {
            try {
                handler.handleUpdate(call.receive())
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                call.respond(HttpStatusCode.OK, "ok") // sent for complete interaction with telegram server
            }
        }
    }
}