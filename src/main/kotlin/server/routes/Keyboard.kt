package server.routes

import io.ktor.application.*
import io.ktor.request.*
import io.ktor.routing.*
import server.handlers.KeyboardsHandler

// TODO: move to common space [share with buttons route]
private val keyboardsHandler = KeyboardsHandler()

fun Route.keyboardRoutes(section: String) {

    get("/keyboards/getAll") {
        keyboardsHandler.getAllKeyboards(this)
    }

    post("/keyboards/add") {
        keyboardsHandler.addKeyboard(call.receive(), this)
    }

    post("/keyboards/detach") {
        keyboardsHandler.detachKeyboard(call.receive(), this)
    }

    post("/keyboards/link") {
        keyboardsHandler.linkKeyboard(call.receive(), this)
    }

    post("/keyboards/delete") {
        keyboardsHandler.deleteKeyboard(call.receive(), this)
    }
}