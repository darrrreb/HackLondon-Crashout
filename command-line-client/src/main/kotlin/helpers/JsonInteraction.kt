package helpers

import java.io.File
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

object JsonInteraction {

    fun readJson(path: String): JsonObject? {
        val file = File(path)

        if (!file.isFile) {
            println("Not a file!")
            return null
        }

        val content = file.readText()
        return Json.parseToJsonElement(content).jsonObject
    }

    fun writeJson(path: String, json: JsonObject) {
        val file = File(path)
        file.writeText(json.toString())
    }
}