package utils

import java.io.FileInputStream
import java.util.Properties

object PropertiesManager {

    private val properties = Properties()

    init {
        properties.load(FileInputStream("src/main/resources/config.properties"))
    }

    fun get(propertyName: String): String {
        val property = properties[propertyName] ?: throw Exception("$propertyName property doesn't exist")
        return property.toString()
    }
}