package server

import io.ktor.serialization.gson.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import security.JwtConfig
import server.handlers.AuthHandler
import server.handlers.ContentHandler
import server.handlers.WebhookHandler
import server.routes.*
import utils.Properties

private val authHandler = AuthHandler()
private val webhookHandler = WebhookHandler()
private val contentHandler = ContentHandler()

class Server {

    private val server = embeddedServer(
        Netty,
        port = Properties.get("server.port").toInt(),
        module = Application::module
    )

    fun start() {
        server.start(true)
    }
}

private fun Application.module() {
    install(ContentNegotiation) {
        gson()
    }
    authentication {
        jwt {
            JwtConfig.configure(this)
        }
    }
    routing {
        this@module.service("/service")
        this@module.authorization(authHandler)
        this@module.buttons("/buttons", contentHandler)
        this@module.payloads("/payloads", contentHandler)
        this@module.keyboards("/keyboards", contentHandler)
        this@module.telegram(Properties.get("bot.webhook.endpoint"), webhookHandler)
    }
}