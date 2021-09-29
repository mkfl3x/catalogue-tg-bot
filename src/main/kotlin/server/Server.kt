package server

import bot.Bot
import com.pengrad.telegrambot.model.Update
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.gson.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

object Server {

    // TODO: move 'port' and 'host' values to config file
    private val server = embeddedServer(Netty, port = 8080, host = "localhost") {
        install(ContentNegotiation) {
            gson()
        }
        routing {
            get("/ping") {
                call.respondText("I am fine")
            }
            post("/callback") {
                val update = call.receive<Update>()
                Bot.sendMessage(update.message().chat().id(), "echo: ${update.message().text()}")
            }
        }
    }

    fun start() {
        server.start(true)
    }
}