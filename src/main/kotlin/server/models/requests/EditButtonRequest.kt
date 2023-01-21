package server.models.requests

import com.google.gson.annotations.SerializedName
import database.mongo.DataManager
import org.bson.types.ObjectId
import server.Request
import server.RequestActions.editButton
import server.RequestValidator
import server.models.Error
import server.models.Result
import server.models.objects.Field

data class EditButtonRequest(
    @SerializedName("button_id") val buttonId: String,
    @SerializedName("fields") val fields: List<Field>
) : Request() {

    override fun validateData(): Result? {
        RequestValidator.validateIds(buttonId)?.let { return it }
        RequestValidator.validateButtonExistence(buttonId)
        fields.forEach { field ->
            RequestValidator.validateIsInList(field.name, listOf("text"))?.let { return it }
            RequestValidator.validateReservedNames(field.value)?.let { return it }
            when (field.name) {
                "text" -> {
                    // Check that button doesn't conflict with other buttons on it's keyboard
                    DataManager.getKeyboards().first { it.buttons.contains(ObjectId(buttonId)) }.apply {
                        if (this.buttons.map { DataManager.getButton(it.toHexString()) }.any { it!!.text == field.value })
                            return Result.error(Error.BUTTON_ALREADY_EXISTS, field.value, this.name)
                    }
                }
            }
        }
        return null
    }

    override fun relatedAction() = editButton(this)
}