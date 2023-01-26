package database.mongo.managers

import common.ReservedNames
import database.mongo.MongoCollections
import database.mongo.MongoNullDataException
import database.mongo.collections.MongoCollection
import database.mongo.models.MongoEntity
import database.mongo.models.data.Button
import database.mongo.models.data.Keyboard
import database.mongo.models.data.Payload

object DataManager {

    private val keyboards = MongoCollection(MongoCollections.KEYBOARDS, Keyboard::class.java)
    private val buttons = MongoCollection(MongoCollections.BUTTONS, Button::class.java)
    private val payloads = MongoCollection(MongoCollections.PAYLOADS, Payload::class.java)

    init {
        reloadCollections()
    }

    // TODO: refactor it
    fun reloadCollections() {
        reloadCollection(keyboards)
        reloadCollection(buttons)
        reloadCollection(payloads)
    }

    fun getKeyboards() = keyboards.entities

    fun getMainKeyboard() = keyboards.entities.find { it.name == ReservedNames.MAIN_KEYBOARD.text }
        ?: throw MongoNullDataException("Keyboard \"${ReservedNames.MAIN_KEYBOARD.text}\" not found")

    fun getKeyboard(keyboardId: String) = keyboards.entities.firstOrNull { it.id.toHexString() == keyboardId }
        ?: throw MongoNullDataException("Keyboard \"$keyboardId\" not found")

    fun getButtons() = buttons.entities

    fun getButton(buttonId: String) = buttons.entities.firstOrNull { it.id.toHexString() == buttonId }
        ?: throw MongoNullDataException("Button \"$buttonId\" not found")

    fun getPayloads() = payloads.entities

    fun getPayload(payloadId: String) = payloads.entities.firstOrNull { it.id.toHexString() == payloadId }
        ?: throw MongoNullDataException("Payload \"$payloadId\" not found")

    private fun <T : MongoEntity> reloadCollection(collection: MongoCollection<T>) {
        collection.reload()
    }
}

