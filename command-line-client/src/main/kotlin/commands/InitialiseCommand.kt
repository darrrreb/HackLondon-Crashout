package commands

import FileScanner
import Hasher
import LocalRepository
import java.io.File
import kotlin.io.path.Path
import kotlin.io.path.createDirectory
import kotlin.io.path.createFile
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
        if (!ensureNotInitialised()) {
            return //Exit if already initialised
        }
        val files = FileScanner.getFiles()
        val ignoredFileNames = FileScanner.getIgnoredFilesNames()
        val fileChanges = getFilesToSend(files, ignoredFileNames)
        if (handleRemote(fileChanges).isSuccessful){
            handleLocal(fileChanges, ignoredFileNames)
            println("Repository initialised successfully!")
        } else {
            println("Failed to initialise repository")
        }
    }

    private fun handleLocal(newFiles: Set<File>, ignoredFileNames: Set<String>) {
        Path("$dir/.headchef").createDirectory()
        Path("$dir/.headchef", "repo.crashout").createFile()
        val entries = getLocalNameHashPairs(newFiles, ignoredFileNames)
        entries.forEach {LocalRepository.newEntry(it.first, it.second)}
        LocalRepository.writeToFile()
    }

    private fun handleRemote(fileChanges: Set<File>): okhttp3.Response {
        return client.newCall(
                buildRequest(
                    "http://localhost:8000/init",
                    buildMultipartBody(
                        MultipartBody.FORM,
                        fileChanges
                    )
                )
                ).execute()
    }

    private fun getLocalNameHashPairs(files: Set<File>, ignoredFiles: Set<String>): List<Pair<String, String>> = Hasher.hashAllFiles(files, ignoredFiles)

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

    private fun ensureNotInitialised(): Boolean {
        val entries = Path(dir).listDirectoryEntries().filter { !it.isDirectory() }
        val repoFile = entries.find { it.fileName.toString() == ".headchef" }

        return repoFile?.let {
            println("Repository already initialised!")
            false
        } ?: true
    }
}
