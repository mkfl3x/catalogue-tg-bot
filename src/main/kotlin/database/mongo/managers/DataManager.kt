package database.mongo.managers

import common.ReservedNames
import database.mongo.MongoCollections
import database.mongo.collections.MongoCollection
import database.mongo.models.MongoEntity
import database.mongo.models.data.Button
import database.mongo.models.data.Keyboard
import database.mongo.models.data.Payload

object DataManager {

    // TODO: handle exceptions
    // TODO: think about collections storage approach

    private val keyboards = MongoCollection(MongoCollections.KEYBOARDS, Keyboard::class.java)
    private val buttons = MongoCollection(MongoCollections.BUTTONS, Button::class.java)
    private val payloads = MongoCollection(MongoCollections.PAYLOADS, Payload::class.java)

    init {
        reloadCollections()
    }

    fun reloadCollections() {
        reloadCollection(keyboards)
        reloadCollection(buttons)
        reloadCollection(payloads)
    }

    fun getKeyboards() = keyboards.entities

    // TODO: handle null
    fun getMainKeyboard() = keyboards.entities.find { it.name == ReservedNames.MAIN_KEYBOARD.text }

    // TODO: handle null
    fun getKeyboard(keyboardId: String): Keyboard? = keyboards.entities.firstOrNull { it.id.toHexString() == keyboardId }

    fun isKeyboardExist(keyboardId: String) = keyboards.entities.any { it.id.toHexString() == keyboardId }

    fun keyboardHasButton(keyboardId: String, buttonText: String) =
        getKeyboard(keyboardId)!!.buttons
            .map { getButton(it.toHexString()) }
            .any { it!!.text == buttonText }

    fun getButtons() = buttons.entities

    // TODO: handle null
    fun getButton(buttonId: String): Button? = buttons.entities.firstOrNull { it.id.toHexString() == buttonId }

    fun getPayloads() = payloads.entities

    // TODO: handle null
    fun getPayload(payloadId: String): Payload? = payloads.entities.firstOrNull { it.id.toHexString() == payloadId }

    private fun <T : MongoEntity> reloadCollection(collection: MongoCollection<T>) {
        collection.reload()
    }
}

