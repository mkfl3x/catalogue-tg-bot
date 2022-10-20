package server.models.requests

import com.google.gson.annotations.SerializedName
import database.DataManager
import org.bson.types.ObjectId
import server.models.Error
import server.models.Result

data class LinkButtonRequest(
    @SerializedName("button_id") val buttonId: String,
    @SerializedName("type") val type: String,
    @SerializedName("link") val link: String
) : Request() {

    override val schema: Schemas
        get() = Schemas.LINK_KEYBOARD_REQUEST

    override val successMessage: String
        get() = "Button with ID \"$buttonId\" successfully linked to $type with ID \"$link\""

    override fun validateData(): Result? {
        DataManager.getButton(ObjectId(buttonId))?.let {
            val exists = when (type) {
                "keyboard" -> DataManager.getKeyboards().any { it.id == ObjectId(link) }
                "payload" -> DataManager.getPayloads().any { it.id == ObjectId(link) }
                else -> throw Exception("unknown button type")
            }
            if (exists.not())
                return Result.error(Error.LINK_OBJECT_DOES_NOT_EXISTS, link)
            if (type == "keyboard" && DataManager.getKeyboard(ObjectId(link))!!.leadButton != null)
                return Result.error(Error.KEYBOARD_ALREADY_LINKED, link)
        } ?: return Result.error(Error.BUTTON_DOES_NOT_EXIST, buttonId)
        return null
    }
}