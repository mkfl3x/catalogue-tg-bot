package server

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.gson.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import server.handlers.ContentHandler
import server.handlers.WebhookHandler
import server.routes.*
import utils.Properties

class Server {

    private val webhookHandler = WebhookHandler()
    private val contentHandler = ContentHandler()

    private val server = embeddedServer(
        Netty,
        port = Properties.get("server.port").toInt()
    ) {
        install(ContentNegotiation) {
            gson()
        }
        routing {
            service("/service")
            buttons("/buttons", contentHandler)
            payloads("/payloads", contentHandler)
            keyboards("/keyboards", contentHandler)
            telegram(Properties.get("bot.webhook.endpoint"), webhookHandler)
        }
    }

    fun start() {
        server.start(true)
    }
}