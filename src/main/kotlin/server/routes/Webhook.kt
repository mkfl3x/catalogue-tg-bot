package server.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import server.handlers.WebhookHandler
import utils.Properties

fun Application.telegram(endpoint: String, handler: WebhookHandler) {
    routing {
        post(endpoint) {
            try {
                if (Properties.get("bot.webhook.secret") == call.request.headers["X-Telegram-Bot-Api-Secret-Token"])
                    handler.handleUpdate(call.receive())
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                call.respond(HttpStatusCode.OK, "ok") // sent for complete interaction with telegram server
            }
        }
    }
}