package server.models.requests

import common.ReservedNames
import database.mongo.DataManager
import io.ktor.http.*
import org.bson.types.ObjectId
import server.models.Error
import server.models.Result
import server.models.objects.Location

object RequestValidator {

    fun validateIds(vararg ids: String?): Result? {
        ids.filterNotNull().forEach {
            if (!ObjectId.isValid(it))
                return Result.error(Error.NOT_VALID_ID_USED, it)
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
        if (!DataManager.isKeyboardExist(ObjectId(location.hostKeyboard)))
            return Result.error(Error.KEYBOARD_DOES_NOT_EXIST, location.hostKeyboard)
        if (DataManager.keyboardHasButton(ObjectId(location.hostKeyboard), location.leadButtonText))
            return Result.error(Error.BUTTON_ALREADY_EXISTS, location.leadButtonText, location.hostKeyboard)
        return null
    }

    fun validateResourceExistence(type: String, id: String): Result? {
        val exists = when (type) {
            "keyboard" -> DataManager.getKeyboards().any { it.id == ObjectId(id) }
            "payload" -> DataManager.getPayloads().any { it.id == ObjectId(id) }
            else -> throw Exception("Unexpected resource type: \"$type\"")
        }
        if (!exists)
            return Result.error(Error.RESOURCE_DOES_NOT_EXISTS, id)
        return null
    }

    fun validateKeyboardExistence(name: String) =
        if (DataManager.getKeyboards().any { it.name == name })
            Result.error(Error.KEYBOARD_ALREADY_EXISTS, name) else null

    fun validateLoopLinking(link: String, keyboardId: String): Result? =
        if (link == keyboardId) Result.error(Error.LOOPED_BUTTON) else null
}