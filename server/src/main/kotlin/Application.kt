package crashout

import aws.sdk.kotlin.services.sts.StsClient
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import java.io.File
import java.security.MessageDigest
import java.time.Instant
import kcl.seg.rtt.utils.aws.S3Manager
import kcl.seg.rtt.utils.aws.S3Service
import kotlinx.serialization.Serializable
import kotlin.collections.plus
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

fun Application.module() {
    install(CORS){
        anyMethod()
        anyHost()
        allowCredentials = true
    }
    install(ContentNegotiation) {
        json()
    }
    configureRouting()
    val json = readJsonFile("src/main/resources/s3config.json")
    S3Service.init(S3Manager(json["s3"]!!.jsonObject, StsClient {region = "eu-west-2"}))
}

fun readJsonFile(path: String): JsonObject {
    val file = File(path)
    val content: String = file.readText()
    return Json.parseToJsonElement(content).jsonObject
}

@Serializable
data class Step (
    val sha: String,
    val parentSha: List<String>?,
    val childrenSha: List<String>,
    val shortMessage: String,
    val longMessage: String,
    val date: String = Instant.now().toString()
)

@OptIn(ExperimentalStdlibApi::class)
fun shaStep(diff: ByteArray, date: String) =
    MessageDigest.getInstance("SHA-256").digest(diff + date.encodeToByteArray()).toHexString()


object Constants {
    const val PREP_ENDPOINT: String = "/prep/{name}/{sha}"
    const val COOK_ENDPOINT: String = "/cook/{name}/{sha}"

}
