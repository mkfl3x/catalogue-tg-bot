package server

import io.ktor.serialization.gson.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
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
            this@embeddedServer.service("/service")
            this@embeddedServer.buttons("/buttons", contentHandler)
            this@embeddedServer.payloads("/payloads", contentHandler)
            this@embeddedServer.keyboards("/keyboards", contentHandler)
            this@embeddedServer.telegram(Properties.get("bot.webhook.endpoint"), webhookHandler)
        }
    }

    fun start() {
        server.start(true)
    }
}