package server

import database.Button

data class AddButtonRequest(
    val keyboard: String,
    val button: Button
)

data class DeleteButtonRequest(
    val keyboard: String,
    val buttonText: String
)
