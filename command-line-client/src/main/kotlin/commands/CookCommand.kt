package commands

import helpers.FileHandler
import helpers.Hasher
import helpers.LocalRepository
import GitDiff.generateDiffFile
import helpers.JsonInteraction
import helpers.RemoteInteraction
import java.io.File
import kotlin.io.path.Path
import kotlin.io.path.deleteIfExists
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import picocli.CommandLine.Command
import picocli.CommandLine.call

@Command(name = "cook", description = ["Prepares files and sends them to the kitchen"], mixinStandardHelpOptions = true,)
class CookCommand : Runnable {
    override fun run() {
        LocalRepository.loadFromFile()
        println("Cooking up a storm!")

        val currentState = FileHandler.getCurrentState("${System.getProperty("user.dir")}/.headchef/currState")
        val ignoredFilesNames = FileHandler.getIgnoredFilesNames()
        val newStateFiles = FileHandler.getFiles()
        val newStateHashes = Hasher.hashAllFiles(newStateFiles, ignoredFilesNames)
        val deletedFileNames = LocalRepository.getAllEntryNames().filter { name -> name !in newStateHashes.map { it.first } }

        val list1: MutableList<File> = mutableListOf()
        val list2: MutableList<File> = mutableListOf()

        for (pair in newStateHashes) {
            val entry = LocalRepository.getEntryByName(pair.first)
            entry?.let {
                if (entry.hash != pair.second) {
                    LocalRepository.removeEntry(entry.hash)
                    list1.add(newStateFiles.getByName(pair.first)!!)
                }
            }
            LocalRepository.newEntry(pair.first, pair.second)
        }

        list2.addAll(newStateFiles.filter { it.name in deletedFileNames})
        list2.addAll(currentState)

        val sha = Hasher.hash(newStateHashes.joinToString { it.second }.encodeToByteArray())
        generateDiffFile(list1, list2, "${System.getProperty("user.dir")}/.headchef/tmp/", sha)

        val repoName = JsonInteraction.readJson("src/main/resources/config.json")!!["repoName"]!!.jsonPrimitive.content
        val parentSha = JsonInteraction.readJson("src/main/resources/config.json")!!["parentSha"]!!.jsonArray.map { it.jsonPrimitive.content }

        if(parentSha.contains(sha)) {
            Path("${System.getProperty("user.dir")}/.headchef/tmp/$sha").deleteIfExists() //Delete temp files
            return println("Already sent this step")
        }

        handleRemote(File("${System.getProperty("user.dir")}/.headchef/tmp/$sha"), repoName, sha, parentSha)
        JsonInteraction.writeJson("src/main/resources/config.json", Json.decodeFromString("{\"repoName\":\"$repoName\",\"parentSha\":[\"$sha\"]}"))
        Path("${System.getProperty("user.dir")}/.headchef/tmp/$sha").deleteIfExists() //Delete temp files
    }

    private fun handleRemote(diff: File, repoName: String, sha: String, parentSha: List<String>): okhttp3.Response {
        return RemoteInteraction.sendRequestToRemote(
            buildRequest(
                "http://localhost:8080/cook/$repoName/$sha/${parentSha.joinToString(",")}",
                buildMultipartBody(
                    MultipartBody.FORM,
                    setOf(diff)
                )
            )
        )
    }

    private fun buildMultipartBody(type: MediaType, filesToSend: Set<File>): MultipartBody {
        val multipartBuilder = MultipartBody.Builder()
            .setType(type)

        for (file in filesToSend) {
            multipartBuilder.addFormDataPart(
                name = "files",
                filename = file.name,
                body = file.asRequestBody("application/octet-stream".toMediaType())
            )
        }


        return multipartBuilder.build()
    }

    private fun buildRequest(url: String, body: RequestBody): Request {
        return Request.Builder()
            .url(url)
            .post(body)
            .build()
    }

}

fun Set<File>.getByName(name: String): File? {
    return this.find { it.name == name }
}

