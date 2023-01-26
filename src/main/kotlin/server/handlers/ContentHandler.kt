package server.handlers

import database.mongo.MongoNullDataException
import database.mongo.managers.DataManager
import io.ktor.http.*
import server.models.requests.Request
import server.models.responses.Response
import server.validations.RequestValidationException

class ContentHandler : RequestHandler {

    fun handleRequest(request: Request) = try {
        request.validateSchema()
        request.validateData()
        request.relatedAction().apply {
            DataManager.reloadCollections()
            return this
        }
    } catch (e: RequestValidationException) {
        Response(HttpStatusCode.BadRequest, e.message!!)
    } catch (e: MongoNullDataException) {
        Response(HttpStatusCode.BadRequest, e.message!!)
    } catch (e: Exception) {
        // TODO: add exception log
        Response(HttpStatusCode.InternalServerError, commonError)
    }

    fun getKeyboard(keyboardId: String) = try {
        Response(HttpStatusCode.OK, DataManager.getKeyboard(keyboardId).toJson())
    } catch (e: MongoNullDataException) {
        Response(HttpStatusCode.BadRequest, e.message!!)
    }

    fun getKeyboards(filter: String): Response {
        val keyboards = when (filter) {
            "all" -> DataManager.getKeyboards()
            "detached" -> DataManager.getKeyboards().filter { it.leadButtons.isEmpty() }.toList()
            else -> return Response(HttpStatusCode.BadRequest, "Unknown \"filter\" parameter: \"$filter\"")
        }
        return Response(HttpStatusCode.OK, keyboards.map { it.toJson() }.toList())
    }

    fun getButton(buttonId: String) = try {
        Response(HttpStatusCode.OK, DataManager.getButton(buttonId).toJson())
    } catch (e: MongoNullDataException) {
        Response(HttpStatusCode.BadRequest, e.message!!)
    }

    fun getButtons(filter: String): Response {
        val buttons = when (filter) {
            "all" -> DataManager.getButtons()
            else -> return Response(HttpStatusCode.BadRequest, "Unknown \"filter\" parameter: \"$filter\"")
        }
        return Response(HttpStatusCode.OK, buttons.map { it.toJson() }.toList())
    }

    fun getPayload(payloadId: String) = try {
        Response(HttpStatusCode.OK, DataManager.getPayload(payloadId).toJson())
    } catch (e: MongoNullDataException) {
        Response(HttpStatusCode.BadRequest, e.message!!)
    }

    fun getPayloads() = Response(HttpStatusCode.OK, DataManager.getPayloads().map { it.toJson() }.toList())
}
