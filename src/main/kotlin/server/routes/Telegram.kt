package server.routes

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import server.handlers.WebhookHandler
import utils.Properties

private val webhookHandler = WebhookHandler()

fun Route.telegramRoute() {

    post(Properties.get("bot.webhook.endpoint")) { // TODO: handle exception implicitly
        try {
            webhookHandler.handleUpdate(call.receive())
        } catch (e: Exception) {
            // TODO: print log
            e.printStackTrace()
        } finally {
            // in each case following response should be sent for complete interaction with telegram server
            call.respond(HttpStatusCode.OK, "ok")
        }
    }
}