package server

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import server.handlers.KeyboardsHandler
import server.handlers.WebhookHandler
import utils.Properties

object Routes {

    private val keyboardsHandler = KeyboardsHandler(Properties.get("mongo.collection.keyboards"))
    private val webhookHandler = WebhookHandler()

    fun Route.keyboardRoutes(section: String) {
        get("$section/get") {
            val filter = call.request.queryParameters["filter"] ?: "all"
            val result = keyboardsHandler.getKeyboards(filter)
            call.respond(result.responseCode, result.responseData)
        }
        post("$section/add") {
            val result = keyboardsHandler.addKeyboard(call.receive())
            call.respond(result.responseCode, result.responseData)
        }
        post("$section/detach") {
            val result = keyboardsHandler.detachKeyboard(call.receive())
            call.respond(result.responseCode, result.responseData)
        }
        post("$section/link") {
            val result = keyboardsHandler.linkKeyboard(call.receive())
            call.respond(result.responseCode, result.responseData)
        }
        post("$section/delete") {
            val result = keyboardsHandler.deleteKeyboard(call.receive())
            call.respond(result.responseCode, result.responseData)
        }
    }

    fun Route.buttonsRoute(section: String) {
        post("$section/add") {
            val result = keyboardsHandler.addButton(call.receive())
            call.respond(result.responseCode, result.responseData)
        }
        post("$section/delete") {
            val result = keyboardsHandler.deleteButton(call.receive())
            call.respond(result.responseCode, result.responseData)
        }
    }

    fun Route.serviceRoute(section: String) {
        get("$section/ping") {
            call.respond(HttpStatusCode.OK, "I am fine")
        }
    }

    fun Route.telegramRoute() {
        post(Properties.get("bot.webhook.endpoint")) {
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
}
