package server

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.gson.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import server.handlers.KeyboardsHandler
import server.handlers.WebhookHandler
import utils.Properties

class Server {

    private val webhookHandler = WebhookHandler()
    private val keyboardsHandler = KeyboardsHandler()

    private val server = embeddedServer(
        Netty,
        port = Properties.get("server.port").toInt(),
        host = Properties.get("server.host")
    ) {
        install(ContentNegotiation) {
            gson()
        }
        routing {
            get("/ping") {
                call.respondText("I am fine")
            }
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
            post("/keyboards/add") { // TODO: add new keyboard to some button
                keyboardsHandler.addKeyboard(call.receive(), this)
                call.respond(HttpStatusCode.OK, "ok")
            }
            post("/keyboards/update") {
                // TODO: implement it
                ///call.respond(HttpStatusCode.OK, "ok")
            }
            get("/keyboards/delete") {
                // TODO: handle parameter
                keyboardsHandler.deleteKeyboard(call.request.queryParameters["keyboard_name"]!!)
                call.respond(HttpStatusCode.OK, "ok")
            }
        }
    }

    fun start() {
        server.start(true)
    }
}