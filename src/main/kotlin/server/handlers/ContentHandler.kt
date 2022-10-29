package server.handlers

import bot.context.KeyboardStates
import com.mongodb.BasicDBObject
import database.mongo.DataManager
import database.mongo.MongoClient
import database.mongo.MongoCollections
import database.mongo.models.Button
import database.mongo.models.Keyboard
import database.mongo.models.Payload
import org.bson.types.ObjectId
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import server.models.Error
import server.models.Result
import server.models.requests.*
import utils.GsonMapper

class ContentHandler {

    private val logger: Logger = LoggerFactory.getLogger(this.javaClass.name)

    fun handleRequest(data: String, request: Requests): Result {
        RequestValidator.validateSchema(data, request.schemaPath)?.let { return it }
        (GsonMapper.deserialize(data, request.type)).apply {
            validateData()?.let { return it }
            when (this) {
                is AddKeyboardRequest -> addKeyboard(this)
                is AddButtonRequest -> addButton(this)
                is AddPayloadRequest -> addPayload(this)
                is DeleteKeyboardRequest -> deleteKeyboard(this)
                is DeleteButtonRequest -> deleteButton(this)
                is DeletePayloadRequest -> deletePayload(this)
                is LinkButtonRequest -> linkButton(this)
                is DetachKeyboardRequest -> detachKeyboard(this)
                else -> throw Exception("Unknown request type")
            }
            DataManager.reloadCollections()
            return Result.success(this.successMessage)
        }
    }

    fun getKeyboards(filter: String): Result {
        val keyboards = when (filter) {
            "all" -> DataManager.getKeyboards()
            "detached" -> DataManager.getKeyboards()
                .filter { it.leadButtons.isEmpty() }
                .toList()
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

    private fun addKeyboard(keyboard: AddKeyboardRequest) {
        // 1. Create keyboard
        val newKeyboardId = createKeyboard(Keyboard(ObjectId(), keyboard.name, emptyList(), emptyList()))
        // 3. Add leading keyboard button (if it's exist)
        keyboard.location?.let {
            val buttonId = createButton(Button(ObjectId(), it.leadButtonText, "keyboard", newKeyboardId))
            // 3.1 Add button on host keyboard
            addButtonToKeyboard(buttonId, ObjectId(it.hostKeyboard))
            // 3.2 Add leading button to new keyboard
            addButtonToKeyboard(buttonId, newKeyboardId, leadButtons = true)
        }
        // 4. Update keyboard's  content buttons
        keyboard.buttons.forEach { addButtonToKeyboard(ObjectId(it), newKeyboardId) }
    }

    private fun deleteKeyboard(request: DeleteKeyboardRequest) {
        deleteKeyboard(request.keyboardId, detachOnly = false)
    }

    private fun detachKeyboard(request: DetachKeyboardRequest) {
        deleteKeyboard(request.keyboardId, detachOnly = true)
    }

    private fun addButton(button: AddButtonRequest) {
        // 1. Create new detached button
        val newButton = createButton(Button(ObjectId(), button.text, button.type, ObjectId(button.link)))
        // 2. Put button to some keyboard is needed
        button.hostKeyboard?.let { addButtonToKeyboard(newButton, ObjectId(it)) }
        // 3. If button leads to keyboard
        if (button.type == "keyboard") {
            addButtonToKeyboard(newButton, ObjectId(button.link), leadButtons = true)
        }
    }

    private fun deleteButton(request: DeleteButtonRequest) {
        val button = DataManager.getButton(request.buttonId)!!
        // 1. Detach keyboard, if it's lead button
        DataManager.getKeyboards()
            .find { keyboard -> keyboard.leadButtons.contains(button.id) }
            ?.let { deleteButtonFromKeyboard(button.id, it.id, leadButtons = true) }
        // 2. Delete button from collection
        deleteButton(button.id)
        // 3. Delete button from all keyboards
        DataManager.getKeyboards()
            .filter { keyboard -> keyboard.buttons.contains(button.id) }
            .forEach { keyboard -> deleteButtonFromKeyboard(button.id, keyboard.id) }
    }

    private fun linkButton(request: LinkButtonRequest) {
        // 1. if it was keyboard - remove this button from lead_buttons
        DataManager.getButton(request.buttonId)?.let {
            if (it.type == "keyboard")
                DataManager.getKeyboards()
                    .filter { keyboard -> keyboard.leadButtons.contains(it.id) }
                    .forEach { keyboard -> deleteButtonFromKeyboard(it.id, keyboard.id, leadButtons = true) }
        }
        // 2. add relinked button to lead_buttons of leads keyboard
        if (request.type == "keyboard")
            addButtonToKeyboard(ObjectId(request.buttonId), ObjectId(request.link), leadButtons = true)
        // 3. update lead_buttons
        MongoClient.update(
            MongoCollections.BUTTONS.collectionName,
            Button::class.java,
            BasicDBObject("_id", ObjectId(request.buttonId)),
            BasicDBObject("\$set", BasicDBObject("link_to", ObjectId(request.link)))
        )
        MongoClient.update(
            MongoCollections.BUTTONS.collectionName,
            Button::class.java,
            BasicDBObject("_id", ObjectId(request.buttonId)),
            BasicDBObject("\$set", BasicDBObject("type", request.type))
        )
    }

    fun addPayload(payload: AddPayloadRequest) {
        // 1. Create payload
        val payloadId = addPayload(Payload(ObjectId(), payload.name, payload.type, payload.data))
        // 2. Add leading keyboard button (if it's exist)
        payload.location?.let {
            val buttonId = createButton(Button(ObjectId(), it.leadButtonText, "payload", payloadId))
            // 2.1 Add button to host keyboard
            addButtonToKeyboard(buttonId, ObjectId(it.hostKeyboard))
        }
    }

    private fun deletePayload(request: DeletePayloadRequest) {
        DataManager.getPayload(request.payloadId)?.let {
            // 1. Delete payload from collection
            deletePayload(it.id)

            // 2. Delete all button usages from keyboards
            DataManager.getButtons()
                .filter { button -> button.linkTo == it.id }
                .forEach { button ->
                    DataManager.getKeyboards()
                        .filter { keyboard -> keyboard.buttons.contains(button.id) }
                        .forEach { keyboard -> deleteButtonFromKeyboard(button.id, keyboard.id) }
                }

            // 3. Delete buttons, leads to payload
            DataManager.getButtons()
                .filter { button -> button.linkTo == it.id }
                .forEach { button -> deleteButton(button.id) }
        }
    }

    private fun createKeyboard(keyboard: Keyboard): ObjectId {
        MongoClient.create(MongoCollections.KEYBOARDS.collectionName, keyboard, Keyboard::class.java)
        return keyboard.id
    }

    private fun deleteKeyboard(keyboardId: ObjectId) {
        MongoClient.delete(MongoCollections.KEYBOARDS.collectionName, BasicDBObject("_id", keyboardId))
    }

    private fun createButton(button: Button): ObjectId {
        MongoClient.create(MongoCollections.BUTTONS.collectionName, button, Button::class.java)
        return button.id
    }

    private fun deleteButton(buttonId: ObjectId) {
        MongoClient.delete(MongoCollections.BUTTONS.collectionName, BasicDBObject("_id", buttonId))
    }

    private fun addButtonToKeyboard(button: ObjectId, keyboard: ObjectId, leadButtons: Boolean = false) {
        MongoClient.update(
            MongoCollections.KEYBOARDS.collectionName,
            Keyboard::class.java,
            BasicDBObject("_id", keyboard),
            BasicDBObject("\$push", BasicDBObject(if (leadButtons) "lead_buttons" else "buttons", button))
        )
    }

    private fun deleteButtonFromKeyboard(button: ObjectId, keyboard: ObjectId, leadButtons: Boolean = false) {
        MongoClient.update(
            MongoCollections.KEYBOARDS.collectionName,
            Keyboard::class.java,
            BasicDBObject("_id", keyboard),
            BasicDBObject("\$pull", BasicDBObject(if (leadButtons) "lead_buttons" else "buttons", button))
        )
    }

    private fun addPayload(payload: Payload): ObjectId {
        MongoClient.create(MongoCollections.PAYLOADS.collectionName, payload, Payload::class.java)
        return payload.id
    }

    private fun deletePayload(payloadId: ObjectId) {
        MongoClient.delete(MongoCollections.PAYLOADS.collectionName, BasicDBObject("_id", payloadId))
    }

    private fun deleteKeyboard(keyboardId: String, detachOnly: Boolean) {
        DataManager.getKeyboard(keyboardId)?.let {
            // 1. Drop keyboard from states
            KeyboardStates.deleteKeyboard(keyboardId)
            // 2. Delete lead buttons (if it's exist)
            it.leadButtons.forEach { leadButton ->
                // 2.1 Delete lead button from all keyboards which contains it
                DataManager.getKeyboards()
                    .filter { keyboard -> keyboard.buttons.contains(leadButton) }
                    .forEach { keyboard -> deleteButtonFromKeyboard(leadButton, keyboard.id) }
                // 2.2 Delete keyboard's lead button
                deleteButton(leadButton)
                // 2.3 Clear lead buttons
                deleteButtonFromKeyboard(leadButton, it.id, leadButtons = true)
            }
            // 3. Delete keyboard from collection
            if (detachOnly.not()) deleteKeyboard(it.id)
        }
    }
}
