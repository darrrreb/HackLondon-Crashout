package helpers

import java.io.File
import kotlin.io.path.Path
import kotlin.io.path.createDirectory
import kotlin.io.path.createFile
import kotlin.io.path.deleteIfExists
import kotlin.io.path.exists
import kotlin.io.path.listDirectoryEntries

object FileHandler {
    private val dir: String = System.getProperty("user.dir")
    private val cloneDir: String = "$dir/.headchef/currState"

     fun getFiles(): Set<File>{
        return Path(dir)
            .listDirectoryEntries()
            .filter { path -> path.toFile().isFile }
            .map { path -> path.toFile() }.toSet()
    }

    fun getIgnoredFilesNames(): Set<String> {
        val ignoreFilePath = Path("$dir/.headchefignore")
        val ignoreFile = if (ignoreFilePath.exists()) { ignoreFilePath.toFile() } else { ignoreFilePath.createFile().toFile() }
        return ignoreFile.readLines()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .toSet()
    }

     fun cloneCurrentState(): Boolean {
        Path(cloneDir).deleteIfExists() //Clean up any existing files

        //Recreate the directory and file
        Path(cloneDir).createDirectory()
        val ignoredFileNames = getIgnoredFilesNames()
        val files = getFiles().filter { it.name !in ignoredFileNames }
        files.forEach {
            val newPath = Path(cloneDir, it.name)
            it.copyTo(newPath.toFile())
        }
        return true
    }

    fun getCurrentState(cloneDir: String): Set<File> {
        return Path(cloneDir)
            .listDirectoryEntries()
            .filter { maybeFile -> maybeFile.toFile().isFile }
            .map { maybeFile -> maybeFile.toFile() }.toSet()
    }
}