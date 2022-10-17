package server

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.gson.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import server.Routes.buttonsRoute
import server.Routes.keyboardRoutes
import server.Routes.payloadsRoute
import server.Routes.serviceRoute
import server.Routes.telegramRoute
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
            payloadsRoute("/payloads")
            telegramRoute()
        }
    }

    fun start() {
        server.start(true)
    }
}