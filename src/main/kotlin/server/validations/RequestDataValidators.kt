package server.validations

import common.ReservedNames
import database.mongo.managers.DataManager
import database.mongo.managers.UsersManager
import org.bson.types.ObjectId
import server.models.objects.Location

object RequestDataValidators {

    fun validateIds(vararg ids: String?) {
        ids.filterNotNull().forEach {
            if (!ObjectId.isValid(it))
                throw RequestValidationException("ID '$it' format is not applicable")
        }
    }

    fun validateReservedNames(vararg names: String?) {
        names.forEach { name ->
            if (ReservedNames.values().any { it.text == name })
                throw RequestValidationException("'$name' is reserved and can't be used")
        }
    }

    fun validateValueInList(value: String, availableValues: List<String>) {
        if (value !in availableValues)
            throw RequestValidationException("'$value' is not allowed. Might be only [${availableValues.joinToString(", ")}]")
    }

    fun validateKeyboardExists(keyboardId: String) = DataManager.getKeyboard(keyboardId)

    fun validateKeyboardDoesNotExist(name: String) {
        if (DataManager.getKeyboards().any { it.name == name })
            throw RequestValidationException("Keyboard with the same name already exists")
    }

    fun validateMainKeyboardLinkingDetaching(keyboardName: String) {
        if (keyboardName == ReservedNames.MAIN_KEYBOARD.text)
            throw RequestValidationException("\"${ReservedNames.MAIN_KEYBOARD.text}\" can't be linked or detached")
    }

    fun validateButtonExists(buttonId: String) = DataManager.getButton(buttonId)

    fun validateButtonDoesNotExist(name: String) {
        if (DataManager.getButtons().any { it.text == name })
            throw RequestValidationException("Button with the same name already exists")
    }

    fun validatePayloadExists(payloadId: String) = DataManager.getPayload(payloadId)

    fun validatePayloadDoesNotExist(name: String) {
        if (DataManager.getPayloads().any { it.name == name })
            throw RequestValidationException("Payload with the same name already exists")
    }

    fun validateUserExists(username: String) = UsersManager.getUser(username)

    fun validateKeyboardDoesNotContainButton(keyboardId: String, buttonId: String) {
        if (ObjectId(buttonId) in DataManager.getKeyboard(keyboardId).buttons)
            throw RequestValidationException("Keyboard already contains this button")
    }

    fun validateLoopLinking(link: String, keyboardId: String) {
        if (link == keyboardId)
            throw RequestValidationException("Button can't leads to it's host keyboard")
    }

    fun validateLoopButton(keyboardId: String, buttonId: String) {
        if (DataManager.getKeyboard(keyboardId).leadButtons.contains(ObjectId(buttonId)))
            throw RequestValidationException("Button leading to keyboard can't be placed on it's one")
    }

    fun validateLocation(location: Location) {
        validateReservedNames(location.leadButtonText)
        validateKeyboardExists(location.hostKeyboard)
        validateButtonDoesNotExist(location.leadButtonText)
    }

    fun validateKeyboardDeletion(keyboardId: String) {
        DataManager.getKeyboard(keyboardId).apply {
            if (this.name == ReservedNames.MAIN_KEYBOARD.text)
                throw RequestValidationException("'${ReservedNames.MAIN_KEYBOARD.text}' can't be deleted")
        }
    }
    
    fun validateLinkingResourceExistence(type: String, id: String) {
        when (type) {
            "keyboard" -> validateKeyboardExists(id)
            "payload" -> validatePayloadExists(id)
            else -> throw Exception("Unexpected resource type: '$type'")
        }
    }
}