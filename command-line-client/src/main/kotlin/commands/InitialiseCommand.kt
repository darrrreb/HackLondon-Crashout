package commands

import FileScanner
import java.io.File
import kotlin.io.path.Path
import kotlin.io.path.createFile
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import kotlin.io.path.listDirectoryEntries
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

        if(!ensureNotInitialised()){
            return
        }

        val response = client.newCall(
            buildRequest("http://localhost:8000/init",
                buildMultipartBody(MultipartBody.FORM,
                    getFilesToSend()))).execute()
        if(!response.isSuccessful) {
            println("Failed to initialise repository!")
        }
        Path("$dir/.headchef").createFile()
        println("Repository initialised successfully!")
        }

    private fun buildMultipartBody(type: MediaType, filesToSend: List<File>): MultipartBody {
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

    private fun buildRequest(url: String, body: RequestBody): Request{
        return Request.Builder()
            .url(url)
            .post(body)
            .build()
    }

    private fun getFilesToSend(): List<File> {
        val ignoreFilePath = Path("$dir/.headchefignore")
        val ignoreFile = if (ignoreFilePath.exists()) { ignoreFilePath.toFile() } else { ignoreFilePath.createFile().toFile() }
        val ignoreSet = ignoreFile.readLines()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .toSet()

        return FileScanner.getFiles()
            .filter { file -> file.name !in ignoreSet }
    }

    private fun ensureNotInitialised(): Boolean {
        val entries = Path(dir).listDirectoryEntries().filter { !it.isDirectory() }
        val repoFile = entries.find { it.fileName.toString() == ".headchef" }

        return repoFile?.let {
            println("Repository already initialised!")
            false
        } ?: true
    }
}
