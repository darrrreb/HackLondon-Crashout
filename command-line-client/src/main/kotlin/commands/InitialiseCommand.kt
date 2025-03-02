package commands

import FileHandler
import Hasher
import LocalRepository
import RemoteInteraction
import com.github.kinquirer.KInquirer
import com.github.kinquirer.components.promptConfirm
import java.io.File
import kotlin.io.path.Path
import kotlin.io.path.createDirectory
import kotlin.io.path.createFile
import kotlin.io.path.exists
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import picocli.CommandLine.Command

@Command(name = "prep", description = ["Initialise a new repository"])
class InitialiseCommand : Runnable {
    val client by lazy { OkHttpClient.Builder().build() }
    val dir = System.getProperty("user.dir")

    override fun run() {
        if (isInitialised()) {
            println("Repository already initialised!")
            return //Exit if already initialised
        }

        val ignoreFilePath = Path("$dir/.headchefignore")
        if (!ignoreFilePath.exists()) {
            val abort: Boolean = KInquirer.promptConfirm(message = "No .chefignorefile found? Continue with a blank one ?", default = false)
            if (abort) {
                return
            }
        }

        val files = FileHandler.getFiles()
        val ignoredFileNames = FileHandler.getIgnoredFilesNames()
        val fileChanges = getFilesToSend(files, ignoredFileNames)
        handleLocal(fileChanges, ignoredFileNames)
        if (handleRemote(fileChanges).isSuccessful) {

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

    private fun handleRemote(fileChanges: Set<File>): okhttp3.Response {
        return RemoteInteraction.sendRequestToRemote(buildRequest(
            "http://localhost:8000/init",
            buildMultipartBody(
                MultipartBody.FORM,
                fileChanges
            )
        ))
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
