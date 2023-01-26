package server.handlers

import database.mongo.MongoNullDataException
import io.ktor.http.*
import server.models.requests.auth.AuthRequest
import server.models.responses.Response
import server.validations.RequestValidationException

class AuthHandler : RequestHandler {

    fun authorize(request: AuthRequest) = try {
        request.validateSchema()
        request.validateData()
        request.relatedAction()
    } catch (e: RequestValidationException) {
        Response(HttpStatusCode.BadRequest, e.message!!)
    } catch (e: MongoNullDataException) {
        Response(HttpStatusCode.BadRequest, e.message!!)
    } catch (e: Exception) {
        // TODO: add exception log
        Response(HttpStatusCode.InternalServerError, commonError)
    }
}