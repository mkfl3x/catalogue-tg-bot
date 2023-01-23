package server.handlers

import io.ktor.http.*
import server.models.requests.auth.AuthRequest
import server.models.responses.Response
import server.validations.RequestValidationException

class AuthHandler {

    fun authorize(request: AuthRequest) = try {
        request.validateSchema()
        request.validateData()
        request.relatedAction()
    } catch (e: RequestValidationException) {
        Response(HttpStatusCode.BadRequest, e.message ?: "Unknown problems with request validation")
    } catch (e: Exception) {
        Response(HttpStatusCode.InternalServerError, e.message ?: "Something went wrong")
    }
}