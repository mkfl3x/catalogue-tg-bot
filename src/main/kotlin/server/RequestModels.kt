package server

import database.Button
import database.Keyboard

data class AddKeyboardRequest(
    val keyboardName: String,
    val newButton: String,
    val keyboard: Keyboard
)

data class AddButtonRequest(
    val keyboard: String,
    val button: Button
)

data class DeleteButtonRequest(
    val keyboard: String,
    val buttonText: String
)
