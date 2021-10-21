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
import server.handlers.WebhookHandler
import utils.Properties

class Server {

    private val handler = WebhookHandler()

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
            post(Properties.get("bot.webhook.endpoint")) {
                try {
                    handler.handleUpdate(call.receive())
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    call.respond(HttpStatusCode.OK, "ok")
                }
            }
            // TODO: add endpoint for insert keyboards
            // TODO: add endpoint for update keyboards
            // TODO: add endpoint for delete keyboards
        }
    }

    fun start() {
        server.start(true)
    }
}