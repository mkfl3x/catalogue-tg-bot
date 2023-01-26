package server.models.requests.data

import com.google.gson.annotations.SerializedName
import server.RequestActions.createButton
import server.models.objects.Location
import server.models.requests.Request
import server.validations.RequestDataValidators

data class CreateButtonRequest(
    @SerializedName("text") val text: String,
    @SerializedName("type") val type: String,
    @SerializedName("link") val link: String,
    @SerializedName("host_keyboard") val hostKeyboard: String?
) : Request {

    override val schemaPath: String
        get() = "json-schemas/models/requests/objects/button.json"

    override fun validateData() {
        RequestDataValidators.validateIds(link, hostKeyboard)
        RequestDataValidators.validateReservedNames(text)
        RequestDataValidators.validateLinkingResourceExistence(type, link)
        hostKeyboard?.apply {
            RequestDataValidators.validateLocation(Location(text, this))
            if (type == "keyboard") RequestDataValidators.validateLoopLinking(link, this)
        }
    }

    override fun relatedAction() = createButton(this)
}