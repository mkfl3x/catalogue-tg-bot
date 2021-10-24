package utils

import com.google.gson.Gson

object GsonMapper {

    private val gson = Gson()

    fun <T> deserialize(json: String, type: Class<T>): T = gson.fromJson(json, type)

    fun serialize(data: Any): String = gson.toJson(data)
}