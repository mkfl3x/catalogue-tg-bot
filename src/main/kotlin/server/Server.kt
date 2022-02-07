package server

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.gson.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import utils.Properties

class Server {

    private val server = embeddedServer(
        Netty,
        port = Properties.get("server.port").toInt()
    ) {
        install(ContentNegotiation) {
            gson()
        }
        routing {
            serviceRoute("/service")
            keyboardRoutes("/keyboards")
            buttonsRoute("/buttons")
            telegramRoute()
        }
    }

    fun start() {
        server.start(true)
    }
}