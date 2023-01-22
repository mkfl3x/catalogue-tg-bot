package server.validations

import common.ReservedNames
import database.mongo.managers.DataManager
import org.bson.types.ObjectId
import server.models.objects.Location

object RequestValidator {

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

    fun validateIsInList(value: String, availableValues: List<String>) {
        if (value !in availableValues)
            throw RequestValidationException("'$value' is not allowed. Might be only [${availableValues.joinToString(", ")}]")
    }

    fun validateLocation(location: Location) {
        validateReservedNames(location.leadButtonText)
        if (!DataManager.isKeyboardExist(location.hostKeyboard))
            throw RequestValidationException("Keyboard \"${location.hostKeyboard}\" doesn't exists")
        if (DataManager.keyboardHasButton(location.hostKeyboard, location.leadButtonText))
            throw RequestValidationException("Button \"${location.leadButtonText}\" already exists on \"${location.hostKeyboard}\" keyboard")
    }

    fun validateKeyboardExistence(keyboardId: String) {
        if (DataManager.getKeyboard(keyboardId) == null)
            throw RequestValidationException("Keyboard \"$keyboardId\" doesn't exists")
    }

    fun validateButtonExistence(buttonId: String) {
        if (DataManager.getButton(buttonId) == null)
            throw RequestValidationException("Button \"$buttonId\" doesn't exist")
    }

    fun validatePayloadExistence(payloadId: String) {
        if (DataManager.getPayload(payloadId) == null)
            throw RequestValidationException("Payload \"$payloadId\" doesn't exist")
    }

    fun validateResourceExistence(type: String, id: String) {
        when (type) {
            "keyboard" -> DataManager.getKeyboards().any { it.id == ObjectId(id) }
            "payload" -> DataManager.getPayloads().any { it.id == ObjectId(id) }
            else -> throw Exception("Unexpected resource type: \"$type\"")
        }.apply {
            if (!this)
                throw RequestValidationException("Keyboard/payload \"$id\" doesn't exist")
        }
    }

    fun validateKeyboardAbsence(name: String) {
        if (DataManager.getKeyboards().any { it.name == name })
            throw RequestValidationException("Keyboard \"$name\" already exists")
    }

    fun validatePayloadAbsence(name: String) {
        if (DataManager.getPayloads().any { it.name == name })
            throw RequestValidationException("Payload \"$name\" already exists")
    }

    fun validateLoopLinking(link: String, keyboardId: String) {
        if (link == keyboardId)
            throw RequestValidationException("Button can't leads to it's host keyboard")
    }

    fun validateKeyboardDeletion(keyboardId: String) {
        DataManager.getKeyboard(keyboardId)?.let {
            if (it.name == ReservedNames.MAIN_KEYBOARD.text)
                throw RequestValidationException("\"${ReservedNames.MAIN_KEYBOARD.text}\" can't be deleted")
        }
    }

    fun validateKeyboardsButtonConflicts(buttonId: String, buttonName: String) {
        DataManager.getKeyboards().first { it.buttons.contains(ObjectId(buttonId)) }.apply {
            if (this.buttons.map { DataManager.getButton(it.toHexString()) }.any { it!!.text == buttonName })
                throw RequestValidationException("Button \"${buttonName}\" already exists on \"${this.name}\" keyboard")
        }
    }
}