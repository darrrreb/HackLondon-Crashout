package crashout

import io.ktor.http.HttpStatusCode
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.server.application.Application
import io.ktor.server.plugins.origin
import io.ktor.server.request.receive
import io.ktor.server.request.receiveMultipart
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import io.ktor.utils.io.jvm.javaio.toInputStream
import java.io.File
import kcl.seg.rtt.utils.aws.S3Service
import kotlin.io.path.Path
import kotlin.io.path.deleteIfExists
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.Json.Default.decodeFromString
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray

fun Application.configureRouting() {
    routing {
        configureMainRoute()
    }
}

fun Route.configureMainRoute() {
    get("/") {
        call.respondText("Hello, world!")
    }

    post("/prep/{name}/{sha}") {
        val name =
            call.parameters["name"] ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing cookbook name!")
        val sha = call.parameters["sha"] ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing SHA!")
        S3Service.createBucket("hl2025-cookbook-$name")

        val multipart = call.receiveMultipart()
        val uploadDir = File("uploads/$name")
        uploadDir.mkdirs()

        multipart.forEachPart { part ->
            if (part is PartData.FileItem) {
                val fileName = part.originalFileName ?: "unknown"
                val tempFile = kotlin.io.path.createTempFile(prefix = "upload_", suffix = fileName).toFile()

                val inputStream = part.provider().toInputStream()
                tempFile.outputStream().buffered().use {
                    inputStream.copyTo(it)
                }

                S3Service.uploadFile("hl2025-cookbook-$name", tempFile, "files/$fileName")
                tempFile.delete()
            }
            part.dispose()
        }

        createStep(sha, null, emptyList(), "Initial Step", "Initialise the Cookbook", name)
        call.respond(HttpStatusCode.OK, "Files uploaded successfully to $uploadDir")
    }

    post("/cook/{name}/{sha}/{parentSha}") {
        val name =
            call.parameters["name"] ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing cookbook name!")
        val sha = call.parameters["sha"] ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing SHA!")
        val parentSha = call.parameters["parentSha"]?.split(",") ?: emptyList()
        val diff = call.receive<ByteArray>()
        val diffFile = File.createTempFile("temp", "")
            diffFile.writeBytes(diff)
        S3Service.uploadFile("hl2025-cookbook-$name", diffFile, "diffs/$sha.diff")
        diffFile.delete()
        val shortMessage = AIService.shortMessage(String(diff))
        val longMessage = AIService.longMessage(String(diff))
        createStep(sha, parentSha, emptyList(),  shortMessage, longMessage, name)

        val parent = S3Service.getFile("hl2025-cookbook-$name", "steps/$parentSha.json")
        val parentString = String(parent)
        val decoded = Json.decodeFromString<JsonObject>(parentString)
        decoded.get("childrenSha")?.jsonArray?.plus(sha)
        val temp = File.createTempFile("temp", ".json")
        temp.writeBytes(Json.encodeToString<JsonObject>(decoded).toByteArray())
        S3Service.uploadFile("hl2025-cookbook-$name", temp, "steps/$parentSha.json")
        temp.delete()

        call.respond(HttpStatusCode.OK, "Step created successfully")
    }

    get("/api/steps/{repoName}"){
        val repoName = call.parameters["repoName"] ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing repo name!")
        val steps = S3Service.downloadDirectory("hl2025-cookbook-$repoName", "steps/")
        call.respond(HttpStatusCode.OK, steps.map { it.readBytes() })
    }

}

suspend fun createStep(sha: String, parentSha: List<String>?, childrenSha: List<String>, shortMessage: String, longMessage: String, bucketName: String): Step {
    val step = Step(sha = sha, parentSha = parentSha, childrenSha = childrenSha, shortMessage = shortMessage , longMessage = longMessage,)
    val temp = withContext(Dispatchers.IO) {
        File.createTempFile("step", ".json")
    }
    temp.writeText(Json.encodeToString(step))
    S3Service.uploadFile("hl2025-cookbook-$bucketName", temp, "steps/$sha.json")
    temp.delete()
    return step
}
