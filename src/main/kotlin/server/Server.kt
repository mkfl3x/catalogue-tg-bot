package server

import com.mongodb.BasicDBObject
import database.Keyboard
import database.MongoClient
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.gson.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import keyboards.KeyboardsManager
import keyboards.Schemas
import server.handlers.WebhookHandler
import utils.GsonMapper
import utils.Properties
import utils.SchemaValidator

class Server {

    private val webhookHandler = WebhookHandler()

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
                val body = call.receive<String>()

                if (!SchemaValidator.isValid(body, Schemas.KEYBOARD)) {
                    call.respond(HttpStatusCode.NotAcceptable, "Not valid keyboard model")
                    return@post
                }

                val keyboard = GsonMapper.deserialize(body, Keyboard::class.java)
                if (KeyboardsManager.getAllKeyboards().contains(keyboard.name)) {
                    // TODO: add error log entry
                    call.respond(HttpStatusCode.Conflict, "Adding already existing keyboard")
                }

                MongoClient.create(Properties.get("mongo.collection.keyboards"), keyboard, Keyboard::class.java)

                call.respond(HttpStatusCode.OK, "ok")
            }
            post("/keyboards/update") {
                // TODO: implement it
                ///call.respond(HttpStatusCode.OK, "ok")
            }
            get("/keyboards/delete") {
                // TODO: add validation of keyboard name
                val keyboardName = call.request.queryParameters["keyboard_name"]
                MongoClient.delete(Properties.get("mongo.collection.keyboards"), BasicDBObject("name", keyboardName))
                // TODO: add delete result
                call.respond(HttpStatusCode.OK, "ok")
            }
        }
    }

    fun start() {
        server.start(true)
    }
}