package server.routes

import io.ktor.application.*
import io.ktor.request.*
import io.ktor.routing.*
import server.handlers.KeyboardsHandler

// TODO: move to common space [share with keyboards route]
private val keyboardsHandler = KeyboardsHandler()

fun Route.buttonsRoute(section: String){

    post("/keyboards/buttons/add") {
        keyboardsHandler.addButton(call.receive(), this)
    }
    post("/keyboards/buttons/delete") {
        keyboardsHandler.deleteButton(call.receive(), this)
    }
}