package server

import bot.context.KeyboardStates
import com.mongodb.BasicDBObject
import database.mongo.MongoClient
import database.mongo.MongoCollections
import database.mongo.managers.DataManager
import database.mongo.models.data.Button
import database.mongo.models.data.Keyboard
import database.mongo.models.data.Payload
import io.ktor.http.*
import org.bson.types.ObjectId
import server.models.requests.data.*
import server.models.responses.Response

object RequestActions {

    // TODO: add safety execution

    fun createKeyboard(request: CreateKeyboardRequest): Response {
        with(request) {
            // Create keyboard
            val keyboard = Keyboard(ObjectId(), name, emptyList(), emptyList())
            MongoClient.create(MongoCollections.KEYBOARDS.collectionName, keyboard, Keyboard::class.java)
            // Create button leads to new keyboard (if keyboard creating as not detached)
            location?.let {
                // Create button
                val button = Button(ObjectId(), it.leadButtonText, "keyboard", keyboard.id)
                MongoClient.create(MongoCollections.BUTTONS.collectionName, button, Button::class.java)
                // Add button on host keyboard (it leads to new keyboard)
                addButtonToKeyboard(button.id, ObjectId(it.hostKeyboard))
                // Add button to new keyboard lead_buttons
                addButtonToKeyboard(button.id, keyboard.id, leadButtons = true)
            }
            return Response(HttpStatusCode.OK, "Keyboard ${keyboard.id.toHexString()} added")
        }
    }

    fun createButton(request: CreateButtonRequest): Response {
        with(request) {
            // Create button
            val button = Button(ObjectId(), text, type, ObjectId(link))
            MongoClient.create(MongoCollections.BUTTONS.collectionName, button, Button::class.java)
            // Add button to host keyboard (if button creating as not detached)
            // TODO: maybe make sense set hostKeyboard as necessary
            hostKeyboard?.let { addButtonToKeyboard(button.id, ObjectId(hostKeyboard)) }
            // If button leads to keyboard then add button to host keyboard's lead_buttons
            if (button.type == "keyboard")
                addButtonToKeyboard(button.id, ObjectId(link), leadButtons = true)
            return Response(HttpStatusCode.OK, "Button ${button.id.toHexString()} added")

        }
    }

    fun editButton(request: EditButtonRequest): Response {
        with(request) {
            request.fields.forEach {
                MongoClient.update(
                    MongoCollections.BUTTONS.collectionName,
                    Button::class.java,
                    BasicDBObject("_id", ObjectId(buttonId)), // TODO: not explicitly that ObjectId required
                    BasicDBObject("\$set", BasicDBObject(it.name, it.value))
                )
            }
            return Response(HttpStatusCode.OK, request.buttonId)
        }
    }

    fun createPayload(request: CreatePayloadRequest): Response {
        with(request) {
            // Create payload
            val payload = Payload(ObjectId(), name, type, data)
            MongoClient.create(MongoCollections.PAYLOADS.collectionName, payload, Payload::class.java)
            // Add button leads to new payload (if payload creating as not detached)
            location?.let {
                // Create button
                val button = Button(ObjectId(), it.leadButtonText, "payload", payload.id)
                MongoClient.create(MongoCollections.BUTTONS.collectionName, button, Button::class.java)
                // Add button to host keyboard
                addButtonToKeyboard(button.id, ObjectId(it.hostKeyboard))
            }
            return Response(HttpStatusCode.OK, "Payload ${payload.id.toHexString()} added")
        }
    }

    fun editPayload(request: EditPayloadRequest): Response {
        with(request) {
            // Get payload
            val payload = DataManager.getPayload(payloadId)!!
            // Update fields
            request.fields.forEach {
                MongoClient.update(
                    MongoCollections.PAYLOADS.collectionName,
                    Button::class.java,
                    BasicDBObject("_id", payload.id), // TODO: not explicitly that ObjectId required
                    BasicDBObject("\$set", BasicDBObject(it.name, it.value))
                )
            }
            return Response(HttpStatusCode.OK, "Payload ${payload.id.toHexString()} edited")
        }
    }

    fun deleteKeyboard(keyboard: Keyboard, detachOnly: Boolean): Response {
        with(keyboard) {
            // Delete keyboard from states
            // TODO: add mechanism for update user's keyboards
            KeyboardStates.deleteKeyboard(id.toHexString())
            // Delete lead buttons (if keyboard is not detached)
            leadButtons.forEach { leadButton ->
                // Delete buttons leads to deleting keyboard from all keyboards
                DataManager.getKeyboards()
                    .filter { keyboard -> keyboard.buttons.contains(leadButton) }
                    .forEach { keyboard -> deleteButtonFromKeyboard(leadButton, keyboard.id) }
                // Delete button leads to deleting keyboard
                MongoClient.delete(MongoCollections.BUTTONS.collectionName, BasicDBObject("_id", leadButton))
                // Delete button from lead_buttons of deleting keyboard (if deleting keyboard is not detached)
                if (leadButtons.contains(leadButton))
                    deleteButtonFromKeyboard(leadButton, id, leadButtons = true)
            }
            // Delete keyboard from collection
            if (detachOnly.not()) {
                MongoClient.delete(MongoCollections.KEYBOARDS.collectionName, BasicDBObject("_id", id))
                return Response(HttpStatusCode.OK, "Keyboard ${id.toHexString()} deleted")
            }
            return Response(HttpStatusCode.OK, "Keyboard ${id.toHexString()} detached")
        }
    }

    fun updateKeyboardButton(request: UpdateKeyboardButtonRequest, action: String) = when (action) {
        "add" -> addKeyboardButton(request.keyboardId, request.buttonId)
        "delete" -> deleteKeyboardButton(request.keyboardId, request.buttonId)
        else -> throw Exception("Unknown \"$action\" parameter")
    }

    fun deleteButton(request: DeleteButtonRequest): Response {
        with(request) {
            // Get button from
            val button = DataManager.getButton(buttonId)!!
            // If button leads to keyboard then detach keyboard
            DataManager.getKeyboards()
                .find { keyboard -> keyboard.leadButtons.contains(button.id) }
                ?.let { deleteButtonFromKeyboard(button.id, it.id, leadButtons = true) }
            // Delete button
            // TODO: add mechanism for update user's keyboards
            MongoClient.delete(MongoCollections.BUTTONS.collectionName, BasicDBObject("_id", buttonId))
            // Delete button from all keyboards
            DataManager.getKeyboards()
                .filter { keyboard -> keyboard.buttons.contains(button.id) }
                .forEach { keyboard -> deleteButtonFromKeyboard(button.id, keyboard.id) }
            return Response(HttpStatusCode.OK, "Button ${button.id.toHexString()} deleted")
        }
    }

    fun deletePayload(request: DeletePayloadRequest): Response {
        with(request) {
            // Get payload
            val payload = DataManager.getPayload(payloadId)!!

            // Delete payload from collection
            MongoClient.delete(MongoCollections.PAYLOADS.collectionName, BasicDBObject("_id", payload.id))

            // Delete all buttons leads to deleting payload from keyboards
            DataManager.getButtons()
                .filter { button -> button.linkTo == payload.id }
                .forEach { button ->
                    // Delete buttons from keyboards
                    DataManager.getKeyboards()
                        .filter { keyboard -> keyboard.buttons.contains(button.id) }
                        .forEach { keyboard -> deleteButtonFromKeyboard(button.id, keyboard.id) }
                    // Delete buttons
                    MongoClient.delete(MongoCollections.BUTTONS.collectionName, BasicDBObject("_id", button.id))
                }
            return Response(HttpStatusCode.OK, "Payload ${payload.id.toHexString()} deleted")
        }
    }

    fun linkButton(request: LinkButtonRequest): Response {
        with(request) {
            // If button leading to keyboard then remove this button from lead_buttons
            val button = DataManager.getButton(request.buttonId)!!
            if (button.type == "keyboard")
                DataManager.getKeyboards()
                    .filter { keyboard -> keyboard.leadButtons.contains(button.id) }
                    .forEach { keyboard -> deleteButtonFromKeyboard(button.id, keyboard.id, leadButtons = true) }
            // If linking to keyboard then add button to lead_buttons of keyboard
            if (request.type == "keyboard")
                addButtonToKeyboard(ObjectId(request.buttonId), ObjectId(request.link), leadButtons = true)
            // Update button's link and link type
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
            return Response(HttpStatusCode.OK, "Button ${button.id.toHexString()} linked")
        }
    }

    private fun addKeyboardButton(keyboardId: String, buttonId: String): Response {
        addButtonToKeyboard(ObjectId(buttonId), ObjectId(keyboardId))
        return Response(HttpStatusCode.OK, "Button \"$buttonId\" added to keyboard \"$keyboardId\"")
    }

    private fun deleteKeyboardButton(keyboardId: String, buttonId: String): Response {
        deleteButtonFromKeyboard(ObjectId(buttonId), ObjectId(keyboardId))
        return Response(HttpStatusCode.OK, "Button \"$buttonId\" deleted from keyboard \"$keyboardId\"")
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
}