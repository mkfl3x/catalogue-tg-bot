package integrations

import io.ktor.http.*
import org.slf4j.LoggerFactory
import utils.GsonMapper
import utils.Properties

data class MlResponse(val msg: String)

object Ml {

    private val logger = LoggerFactory.getLogger(this.javaClass.name)

    fun getAnswer(question: String): String {
        val response = khttp.get(url = Properties.get("integrations.ml"), params = mapOf("msg" to question))
        return if (response.statusCode == HttpStatusCode.OK.value) {
            try {
                GsonMapper.deserialize(response.text, MlResponse::class.java).msg
            } catch (e: Exception) {
                logger.error(e.message)
                "Что-то пошло не так \uD83E\uDD72" // TODO: move to constant
            }
        } else {
            logger.error("response_code from Ml is ${response.statusCode}")
            "Что-то пошло не так \uD83E\uDD72" // TODO: move to constant
        }
    }
}