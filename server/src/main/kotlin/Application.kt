package crashout

import io.ktor.server.application.*
import java.security.MessageDigest
import java.time.Instant
import kotlinx.serialization.Serializable
import kotlin.collections.plus
import kotlinx.serialization.json.JsonObject

fun Application.module() {
    configureRouting()
    println(AIService.longMessage("Generate a sample response for this prompt."))
}

@Serializable
data class Step(
    val sha: String,
    val parentSha: List<String>,
    val childrenSha: List<String>,
    val shortMessage: String,
    val longMessage: String,
    val diffRef: String,
    val date: String = Instant.now().toString()
)

@OptIn(ExperimentalStdlibApi::class)
fun shaStep(diff: ByteArray, date: String) =
    MessageDigest.getInstance("SHA-256").digest(diff + date.encodeToByteArray()).toHexString()


object Constants {
    const val SERVE_ENDPOINT: String = "/serve"
}
fun processServe(data: JsonObject) {
    println("Processing serve request")
    println(data)
}
