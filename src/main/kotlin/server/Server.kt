package server

import bot.Bot
import com.pengrad.telegrambot.model.Update
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.gson.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import utils.PropertiesManager

object Server {

    private val server = embeddedServer(
        Netty,
        port = PropertiesManager.get("server.port").toInt(),
        host = PropertiesManager.get("server.host")
    ) {
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
                call.respond(HttpStatusCode.OK, "ok")
            }
        }
    }

    fun start() {
        server.start(true)
    }
}