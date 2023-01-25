package server.handlers

import database.mongo.managers.DataManager
import io.ktor.http.*
import server.models.requests.Request
import server.models.responses.Response
import server.validations.RequestValidationException

class ContentHandler {

    // TODO: use logger
    // private val logger: Logger = LoggerFactory.getLogger(this.javaClass.name)

    fun handleRequest(request: Request) = try {
        request.validateSchema()
        request.validateData()
        request.relatedAction().apply {
            DataManager.reloadCollections()
            return this
        }
    } catch (e: RequestValidationException) {
        Response(HttpStatusCode.BadRequest, e.message ?: "Unknown problems with request validation")
    } catch (e: Exception) {
        Response(HttpStatusCode.InternalServerError, e.message ?: "Something went wrong")
    }

    fun getKeyboard(keyboardId: String) = DataManager.getKeyboard(keyboardId)?.let {
        Response(HttpStatusCode.OK, it.toJson())
    } ?: Response(HttpStatusCode.BadRequest, "Keyboard with ID \"$keyboardId\" not found")

    fun getKeyboards(filter: String): Response {
        val keyboards = when (filter) {
            "all" -> DataManager.getKeyboards()
            "detached" -> DataManager.getKeyboards().filter { it.leadButtons.isEmpty() }.toList()
            else -> return Response(HttpStatusCode.BadRequest, "Unknown \"filter\" parameter: \"$filter\"")
        }
        return Response(HttpStatusCode.OK, keyboards.map { it.toJson() }.toList())
    }

    fun getButton(buttonId: String) = DataManager.getButton(buttonId)?.let {
        Response(HttpStatusCode.OK, it.toJson())
    } ?: Response(HttpStatusCode.BadRequest, "Button with ID \"$buttonId\" not found")

    fun getButtons(filter: String): Response {
        val buttons = when (filter) {
            "all" -> DataManager.getButtons()
            else -> return Response(HttpStatusCode.BadRequest, "Unknown \"filter\" parameter: \"$filter\"")
        }
        return Response(HttpStatusCode.OK, buttons.map { it.toJson() }.toList())
    }

    fun getPayload(payloadId: String) = DataManager.getPayload(payloadId)?.let {
        Response(HttpStatusCode.OK, it.toJson())
    } ?: Response(HttpStatusCode.BadRequest, "Payload with ID \"$payloadId\" not found")

    fun getPayloads(): Response {
        return Response(HttpStatusCode.OK, DataManager.getPayloads().map { it.toJson() }.toList())
    }
}
