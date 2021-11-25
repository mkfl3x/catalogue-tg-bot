package utils

import java.util.Properties

object Properties {

    private val properties = Properties()

    init {
        properties.load(Thread.currentThread().contextClassLoader.getResourceAsStream("config.properties"))
    }

    fun get(propertyName: String): String {
        val property = properties[propertyName] ?: throw Exception("$propertyName property doesn't exist")
        return property.toString()
    }
}