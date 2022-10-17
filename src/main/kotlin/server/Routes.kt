package server

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import server.handlers.ContentHandler
import server.handlers.WebhookHandler
import utils.Properties

object Routes {

    private val contentHandler = ContentHandler()
    private val webhookHandler = WebhookHandler()

    fun Route.keyboardRoutes(section: String) {
        get("$section/get") {
            val filter = call.request.queryParameters["filter"] ?: "all"
            val result = contentHandler.getKeyboards(filter)
            call.respond(result.responseCode, result.responseData)
        }
        post("$section/add") {
            val result = contentHandler.addKeyboard(call.receive())
            call.respond(result.responseCode, result.responseData)
        }
        put("$section/detach") {
            val result = contentHandler.detachKeyboard(call.receive())
            call.respond(result.responseCode, result.responseData)
        }
        delete("$section/delete") {
            val result = contentHandler.deleteKeyboard(call.receive())
            call.respond(result.responseCode, result.responseData)
        }
    }

    fun Route.buttonsRoute(section: String) {
        get("$section/get") {
            val filter = call.request.queryParameters["filter"] ?: "all"
            val result = contentHandler.getButtons(filter)
            call.respond(result.responseCode, result.responseData)
        }
        post("$section/add") {
            val result = contentHandler.addButton(call.receive())
            call.respond(result.responseCode, result.responseData)
        }
        delete("$section/delete") {
            val result = contentHandler.deleteButton(call.receive())
            call.respond(result.responseCode, result.responseData)
        }
        put("$section/link") {
            val result = contentHandler.linkButton(call.receive())
            call.respond(result.responseCode, result.responseData)
        }
    }

    fun Route.payloadsRoute(section: String) {
        get("$section/get") {
            val result = contentHandler.getPayloads()
            call.respond(result.responseCode, result.responseData)
        }
        post("$section/add") {
            val result = contentHandler.addPayload(call.receive())
            call.respond(result.responseCode, result.responseData)
        }
        delete("$section/delete") {
            val result = contentHandler.deletePayload(call.receive())
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
                e.printStackTrace()
            } finally {
                call.respond(HttpStatusCode.OK, "ok") // sent for complete interaction with telegram server
            }
        }
    }
}
