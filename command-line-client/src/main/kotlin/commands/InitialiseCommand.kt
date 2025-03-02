package commands

import helpers.FileHandler
import helpers.Hasher
import helpers.LocalRepository
import helpers.RemoteInteraction
import com.github.kinquirer.KInquirer
import com.github.kinquirer.components.promptConfirm
import helpers.JsonInteraction
import java.io.File
import java.security.MessageDigest
import kotlin.io.path.Path
import kotlin.io.path.createDirectory
import kotlin.io.path.createFile
import kotlin.io.path.exists
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.jsonObject
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import picocli.CommandLine
import picocli.CommandLine.Command

@Command(name = "prep", description = ["Initialise a new repository"])
class InitialiseCommand : Runnable {
    @CommandLine.Parameters(index = "0", paramLabel = "<name>", description = ["Name of a cookbook"])
    lateinit var repoName: String
    val dir = System.getProperty("user.dir")

    override fun run() {
        if (isInitialised()) {
            println("Repository already initialised!")
            return //Exit if already initialised
        }

        val ignoreFilePath = Path("$dir/.headchefignore")
        if (!ignoreFilePath.exists()) {
            val abort: Boolean = KInquirer.promptConfirm(
                message = "No .chefignorefile found? Continue with a blank one ?",
                default = false
            )
            if (abort) {
                return
            }
        }

        // Write the repo name to the config file. Since this is an initialisation, the parentSha is null.

        val files = FileHandler.getFiles()
        val ignoredFileNames = FileHandler.getIgnoredFilesNames()
        val fileChanges = getFilesToSend(files, ignoredFileNames)
        val sha = Hasher.sha(fileChanges.toList())
        if (handleRemote(fileChanges, sha).isSuccessful) {
            handleLocal(fileChanges, ignoredFileNames)
            JsonInteraction.writeJson(
                "src/main/resources/config.json",
                Json.parseToJsonElement("{ \"repoName\":\"$repoName\", \"parentSha\":\"$sha\"}").jsonObject
            )
            println("Repository initialised successfully!")
        } else {
            println("Failed to initialise repository")
        }
    }

    private fun handleLocal(newFiles: Set<File>, ignoredFileNames: Set<String>) {
        Path("$dir/.headchef").createDirectory()
        Path("$dir/.headchef", "repo.crashout").createFile()
        val entries = getLocalNameHashPairs(newFiles, ignoredFileNames)
        entries.forEach { LocalRepository.newEntry(it.first, it.second) }
        LocalRepository.writeToFile()
        FileHandler.cloneCurrentState()
    }

    private fun handleRemote(fileChanges: Set<File>, sha: String): okhttp3.Response {
        println(repoName)
        return RemoteInteraction.sendRequestToRemote(
            buildRequest(
                "http://localhost:8080/prep/$repoName/$sha",
                buildMultipartBody(
                    MultipartBody.FORM,
                    fileChanges
                )
            )
        )
    }



    private fun getLocalNameHashPairs(
        files: Set<File>,
        ignoredFiles: Set<String>
    ): List<Pair<String, String>> = Hasher.hashAllFiles(files, ignoredFiles)

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


    private fun getFilesToSend(files: Set<File>, ignoredFiles: Set<String>): Set<File> {
        return files
            .filter { file -> file.name !in ignoredFiles }.toSet()
    }

    private fun isInitialised(): Boolean = Path("$dir/.headchef").exists()
}
