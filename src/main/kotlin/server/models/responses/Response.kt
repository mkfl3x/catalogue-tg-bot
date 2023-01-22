package server.models.responses

import io.ktor.http.*

data class Response(val httpCode: HttpStatusCode, val content: Any)