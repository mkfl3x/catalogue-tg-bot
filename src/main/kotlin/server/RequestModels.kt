package server

import keyboards.Button
import keyboards.Keyboard

data class AddKeyboardRequest(
    val parenKeyboard: String,
    val newButton: String,
    val newKeyboard: Keyboard
)

data class DeleteKeyboardRequest(
    val keyboard: String
)

data class AddButtonRequest(
    val keyboard: String,
    val button: Button
)

data class DeleteButtonRequest(
    val keyboard: String,
    val buttonText: String
)
