package utils

import java.util.Properties

object Properties {

    private val properties = Properties()

    init {
        properties.load(Thread.currentThread().contextClassLoader.getResourceAsStream("config.properties"))
    }

    fun get(propertyName: String): String {
        val property = getDockerEnv(propertyName) ?: properties[propertyName]
        ?: throw Exception("$propertyName property doesn't exist")
        return property.toString().ifEmpty { throw Exception("$propertyName property is empty") }
    }

    private fun getDockerEnv(propertyName: String) =
        System.getenv(propertyName.replace(".", "_").uppercase()).let {
            if (!it.isNullOrEmpty()) it else null
        }
}