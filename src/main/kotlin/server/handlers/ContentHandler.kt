package server.handlers

import database.mongo.DataManager
import server.RequestValidator
import server.Requests
import server.models.Error
import server.models.Result
import utils.GsonMapper

class ContentHandler {

    // TODO: use logger
    // private val logger: Logger = LoggerFactory.getLogger(this.javaClass.name)

    fun handleRequest(data: String, request: Requests): Result {
        RequestValidator.validateSchema(data, request.schemaPath)?.let { return it }
        (GsonMapper.deserialize(data, request.type)).apply {
            validateData()?.let { return it }
            val response = relatedAction()
            DataManager.reloadCollections()
            return Result.success(response.toJson())
        }
    }

    fun getKeyboards(filter: String): Result {
        val keyboards = when (filter) {
            "all" -> DataManager.getKeyboards()
            "detached" -> DataManager.getKeyboards().filter { it.leadButtons.isEmpty() }.toList()
            else -> return Result.error(Error.UNKNOWN_PARAMETER_VALUE, filter, "filter")
        }
        return Result.success(keyboards.map { it.toJson() }.toList())
    }

    fun getButtons(filter: String): Result {
        val buttons = when (filter) {
            "all" -> DataManager.getButtons()
            else -> return Result.error(Error.UNKNOWN_PARAMETER_VALUE, filter, "filter")
        }
        return Result.success(buttons.map { it.toJson() }.toList())
    }

    fun getPayloads(): Result {
        return Result.success(DataManager.getPayloads().map { it.toJson() }.toList())
    }
}
