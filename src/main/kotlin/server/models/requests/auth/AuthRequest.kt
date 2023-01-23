package server.models.requests.auth

import com.google.gson.annotations.SerializedName
import io.ktor.http.*
import security.JwtConfig
import server.models.requests.Request
import server.models.responses.Response
import server.validations.RequestValidator

data class AuthRequest(
    @SerializedName("username") val username: String,
    @SerializedName("password") val password: String
) : Request {

    override val schemaPath: String
        get() = "json-schemas/models/requests/auth_request.json"

    override fun validateData() {
        RequestValidator.validateUserExistence(username)
    }

    override fun relatedAction(): Response {
        val token = JwtConfig.generateToken(this)
        return Response(HttpStatusCode.OK, "JWT token: $token")
    }
}