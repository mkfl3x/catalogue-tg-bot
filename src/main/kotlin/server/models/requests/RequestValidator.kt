package server.models.requests

import common.ReservedNames
import database.mongo.DataManager
import io.ktor.http.*
import org.bson.types.ObjectId
import server.models.Error
import server.models.Result
import server.models.objects.Location
import utils.SchemaValidator

object RequestValidator {

    fun validateSchema(request: String, schemaPath: String): Result? {
        SchemaValidator.validate(request, schemaPath).apply {
            return if (isSuccess.not()) {
                val error = this.toString().split("\n").first { it.startsWith("error:") }.replace("error: ", "")
                Result.error(Error.NOT_VALID_JSON_SCHEMA, error)
            } else null
        }
    }

    fun validateIds(vararg ids: String?): Result? {
        ids.filterNotNull().forEach {
            if (!ObjectId.isValid(it))
                return Result.error(Error.NOT_VALID_ID, it)
        }
        return null
    }

    fun validateReservedNames(vararg names: String?): Result? {
        names.forEach { name ->
            if (ReservedNames.values().any { it.text == name })
                return Result(HttpStatusCode.BadRequest, "'$name' is reserved and can't be used")
        }
        return null
    }

    fun validateLocation(location: Location): Result? {
        validateReservedNames(location.leadButtonText)?.let { return it }
        if (!DataManager.isKeyboardExist(location.hostKeyboard))
            return Result.error(Error.KEYBOARD_DOES_NOT_EXIST, location.hostKeyboard)
        if (DataManager.keyboardHasButton(location.hostKeyboard, location.leadButtonText))
            return Result.error(Error.BUTTON_ALREADY_EXISTS, location.leadButtonText, location.hostKeyboard)
        return null
    }

    fun validateKeyboardExistence(keyboardId: String) =
        if (DataManager.getKeyboard(keyboardId) == null)
            Result.error(Error.KEYBOARD_DOES_NOT_EXIST, keyboardId) else null

    fun validateButtonExistence(buttonId: String) =
        if (DataManager.getButton(buttonId) == null)
            Result.error(Error.BUTTON_DOES_NOT_EXIST, buttonId) else null

    fun validatePayloadExistence(payloadId: String) =
        if (DataManager.getPayload(payloadId) == null)
            Result.error(Error.PAYLOAD_DOES_NOT_EXISTS, payloadId) else null

    fun validateResourceExistence(type: String, id: String): Result? {
        val exists = when (type) {
            "keyboard" -> DataManager.getKeyboards().any { it.id == ObjectId(id) }
            "payload" -> DataManager.getPayloads().any { it.id == ObjectId(id) }
            else -> throw Exception("Unexpected resource type: \"$type\"")
        }
        return if (!exists)
            Result.error(Error.RESOURCE_DOES_NOT_EXISTS, id) else null
    }

    fun validateKeyboardAbsence(name: String) =
        if (DataManager.getKeyboards().any { it.name == name })
            Result.error(Error.KEYBOARD_ALREADY_EXISTS, name) else null

    fun validatePayloadAbsence(name: String) =
        if (DataManager.getPayloads().any { it.name == name })
            Result.error(Error.PAYLOAD_ALREADY_EXISTS, name) else null

    fun validateLoopLinking(link: String, keyboardId: String): Result? =
        if (link == keyboardId) Result.error(Error.LOOPED_BUTTON) else null

    fun tryDeleteMainKeyboard(keyboardId: String): Result? {
        DataManager.getKeyboard(keyboardId)?.let {
            if (it.name == ReservedNames.MAIN_KEYBOARD.text)
                return Result.error(Error.DELETE_MAIN_KEYBOARD)
        }
        return null
    }
}