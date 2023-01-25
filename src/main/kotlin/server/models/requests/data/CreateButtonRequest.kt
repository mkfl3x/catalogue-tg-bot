package server.models.requests.data

import com.google.gson.annotations.SerializedName
import server.RequestActions.createButton
import server.models.objects.Location
import server.models.requests.Request
import server.validations.RequestValidator

data class CreateButtonRequest(
    @SerializedName("text") val text: String,
    @SerializedName("type") val type: String,
    @SerializedName("link") val link: String,
    @SerializedName("host_keyboard") val hostKeyboard: String?
) : Request {

    override val schemaPath: String
        get() = "json-schemas/models/requests/objects/button.json"

    override fun validateData() {
        RequestValidator.validateIds(link, hostKeyboard)
        RequestValidator.validateReservedNames(text)
        RequestValidator.validateResourceExistence(type, link)
        hostKeyboard?.let { keyboard ->
            RequestValidator.validateLocation(Location(text, keyboard))
            if (type == "keyboard") RequestValidator.validateLoopLinking(link, keyboard)
        }
    }

    override fun relatedAction() = createButton(this)
}