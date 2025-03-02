package commands

import helpers.FileHandler
import helpers.Hasher
import helpers.LocalRepository
import GitDiff.generateDiffFile
import helpers.JsonInteraction
import helpers.RemoteInteraction
import java.io.File
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.Path
import kotlin.io.path.createFile
import kotlin.io.path.deleteIfExists
import kotlin.io.path.deleteRecursively
import kotlin.io.path.exists
import kotlinx.coroutines.runBlocking
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

@Command(
    name = "cook",
    description = ["Prepares files and sends them to the kitchen"],
    mixinStandardHelpOptions = true,
)
class CookCommand : Runnable {
    @OptIn(ExperimentalPathApi::class)
    override fun run() {
        LocalRepository.loadFromFile()

        val oldEntryNames = LocalRepository.getAllEntryNames()
        val ignoredFilesNames = FileHandler.getIgnoredFilesNames()
        val currentState = FileHandler.getCurrentState("${System.getProperty("user.dir")}/.headchef/currState")
        Path("${System.getProperty("user.dir")}/.headchef/repo.crashout").deleteIfExists()
       Path("${System.getProperty("user.dir")}/.headchef/repo.crashout").createFile()

        println("Cooking up a storm!")
       // println( File("${System.getProperty("user.dir")}/.headchef/repo.crashout").readText())

        val newStateFiles = FileHandler.getFiles()
        val newStateHashes = Hasher.hashAllFiles(newStateFiles, ignoredFilesNames)

        println(oldEntryNames)
        println(ignoredFilesNames)
        println(currentState)
        println(newStateHashes)
        println(newStateFiles)
        println("=====================================")


        val list1: MutableList<File> = mutableListOf()
        val list2: MutableList<File> = mutableListOf()

        for (pair in newStateHashes) {
            val entry = LocalRepository.getEntryByName(pair.first)
            entry?.let {
                if (entry.hash != pair.second) {
                    list1.add(newStateFiles.getByName(pair.first)!!)
                    list2.add(currentState.getByName(pair.first)!!)
                    LocalRepository.removeEntry(pair.first)
                    LocalRepository.newEntry(pair.first, pair.second)
                }
            } ?: run {LocalRepository.newEntry(pair.first, pair.second)
                list1.add(newStateFiles.getByName(pair.first)!!)}


            print("T")
        }

        println(oldEntryNames)
        println(newStateHashes)
        println("=====================================")
        oldEntryNames.filter { it !in newStateHashes.map { it.first } }.onEach { name ->
            currentState.getByName(name)
                ?.let {
                    print("a")
                    list2.add(it)
                    LocalRepository.removeEntry(it.name)
                }
        }

        list1.forEach { println(it.name) }
        println("SKIBDIIIIII")
        list2.forEach { println(it.name) }


        val sha = Hasher.hash(newStateHashes.joinToString { it.second }.encodeToByteArray())
        generateDiffFile(list2, list1, "${System.getProperty("user.dir")}/.headchef/tmp/", sha)

        val repoName = JsonInteraction.readJson("src/main/resources/config.json")!!["repoName"]!!.jsonPrimitive.content
        val parentSha =
            JsonInteraction.readJson("src/main/resources/config.json")!!["parentSha"]!!.jsonArray.map { it.jsonPrimitive.content }


        LocalRepository.writeToFile()

        if (parentSha.contains(sha)) {
            Path("${System.getProperty("user.dir")}/.headchef/tmp/$sha").deleteIfExists() //Delete temp files
            return println("Already sent this step")
        }

        handleRemote(File("${System.getProperty("user.dir")}/.headchef/tmp/$sha"), repoName, sha, parentSha)
        JsonInteraction.writeJson(
            "src/main/resources/config.json",
            Json.decodeFromString("{\"repoName\":\"$repoName\",\"parentSha\":[\"$sha\"]}")
        )
        Path("${System.getProperty("user.dir")}/.headchef/tmp/$sha").deleteIfExists() //Delete temp files
        Path("${System.getProperty("user.dir")}/.headchef/currState").deleteRecursively() //Delete temp files
        FileHandler.cloneCurrentState()
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

