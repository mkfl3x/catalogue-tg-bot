package database.mongo

import common.ReservedNames
import database.mongo.models.Button
import database.mongo.models.Keyboard
import database.mongo.models.Payload

object DataManager {

    private lateinit var keyboards: HashSet<Keyboard>
    private lateinit var buttons: HashSet<Button>
    private lateinit var payloads: HashSet<Payload>

    init {
        reloadCollections()
    }

    fun reloadCollections() {
        MongoCollections.values().forEach { reloadCollection(it) }
    }

    private fun reloadCollection(collection: MongoCollections) {
        when (collection) {
            MongoCollections.KEYBOARDS -> keyboards =
                MongoClient.readAllEntries(collection.collectionName, Keyboard::class.java).toHashSet()
            MongoCollections.BUTTONS -> buttons =
                MongoClient.readAllEntries(collection.collectionName, Button::class.java).toHashSet()
            MongoCollections.PAYLOADS -> payloads =
                MongoClient.readAllEntries(collection.collectionName, Payload::class.java).toHashSet()
        }
    }

    fun getKeyboards() = keyboards

    fun getMainKeyboard() = keyboards.find { it.name == ReservedNames.MAIN_KEYBOARD.text }

    fun getKeyboard(keyboardId: String): Keyboard? = keyboards.firstOrNull { it.id.toHexString() == keyboardId }

    fun isKeyboardExist(keyboardId: String) = keyboards.any { it.id.toHexString() == keyboardId }

    fun keyboardHasButton(keyboardId: String, buttonText: String) =
        getKeyboard(keyboardId)!!.buttons
            .map { getButton(it.toHexString()) }
            .any { it!!.text == buttonText }

    fun getButtons() = buttons

    fun getButton(buttonId: String): Button? = buttons.firstOrNull { it.id.toHexString() == buttonId }

    fun getPayloads() = payloads

    fun getPayload(payloadId: String): Payload? = payloads.firstOrNull { it.id.toHexString() == payloadId }
}

