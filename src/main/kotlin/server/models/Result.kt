package server.models

import io.ktor.http.*

data class Result(
    val responseCode: HttpStatusCode,
    val responseData: Any
)