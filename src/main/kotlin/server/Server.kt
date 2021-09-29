package server

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

object Server {

    // TODO: move 'port' and 'host' values to config file
    private val server = embeddedServer(Netty, port = 8080, host = "localhost") {
        routing {
            get("/ping") {
                call.respondText("I am fine")
            }
            post("/callback") {
                // TODO
            }
        }
    }

    fun start() {
        server.start(true)
    }

    fun stop() {
        // TODO: move parameters to config file
        server.stop(3000, 500)
    }
}