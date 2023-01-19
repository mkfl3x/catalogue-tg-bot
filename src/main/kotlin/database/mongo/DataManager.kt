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

    fun reloadCollections(vararg collections: MongoCollections = MongoCollections.values()) {
        collections.forEach { MongoClient.readAllEntries(it.collectionName, it.type).toHashSet() }
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

