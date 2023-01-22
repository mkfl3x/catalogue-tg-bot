package database.mongo.managers

import common.ReservedNames
import database.mongo.MongoClient
import database.mongo.MongoCollections
import database.mongo.models.data.Button
import database.mongo.models.data.Keyboard
import database.mongo.models.data.Payload

object DataManager {

    // TODO: handle exceptions

    private lateinit var keyboards: HashSet<Keyboard>
    private lateinit var buttons: HashSet<Button>
    private lateinit var payloads: HashSet<Payload>

    init {
        reloadCollections()
    }

    fun reloadCollections(vararg collections: MongoCollections = MongoCollections.values()) {
        collections.forEach {
            when (it) {
                MongoCollections.KEYBOARDS -> keyboards =
                    MongoClient.readAllEntries(it.collectionName, Keyboard::class.java).toHashSet()
                MongoCollections.BUTTONS -> buttons =
                    MongoClient.readAllEntries(it.collectionName, Button::class.java).toHashSet()
                MongoCollections.PAYLOADS -> payloads =
                    MongoClient.readAllEntries(it.collectionName, Payload::class.java).toHashSet()
            }
        }
    }

    fun getKeyboards() = keyboards

    // TODO: handle null
    fun getMainKeyboard() = keyboards.find { it.name == ReservedNames.MAIN_KEYBOARD.text }

    // TODO: handle null
    fun getKeyboard(keyboardId: String): Keyboard? = keyboards.firstOrNull { it.id.toHexString() == keyboardId }

    fun isKeyboardExist(keyboardId: String) = keyboards.any { it.id.toHexString() == keyboardId }

    fun keyboardHasButton(keyboardId: String, buttonText: String) =
        getKeyboard(keyboardId)!!.buttons
            .map { getButton(it.toHexString()) }
            .any { it!!.text == buttonText }

    fun getButtons() = buttons

    // TODO: handle null
    fun getButton(buttonId: String): Button? = buttons.firstOrNull { it.id.toHexString() == buttonId }

    fun getPayloads() = payloads

    // TODO: handle null
    fun getPayload(payloadId: String): Payload? = payloads.firstOrNull { it.id.toHexString() == payloadId }
}

