import java.io.File
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.createDirectory
import kotlin.io.path.createFile
import kotlin.io.path.deleteIfExists
import kotlin.io.path.exists
import kotlin.io.path.listDirectoryEntries

object FileScanner {
    private val dir: String = System.getProperty("user.dir")
    private val cloneDir: String = "$dir/.headchef"

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

    private fun cloneCurrentState(path: String): Boolean {
        Path(path).deleteIfExists() //Clean up any existing files

        //Recreate the directory and file
        Path(path).createDirectory()
        val files = FileScanner.getFiles()
        files.forEach {
            val newPath = Path(path, it.name)
            it.copyTo(newPath.toFile())
        }
        return true
    }


    fun getClonedFiles(cloneDir: Path): Set<File> {
        return cloneDir
            .listDirectoryEntries()
            .filter { maybeFile -> maybeFile.toFile().isFile }
            .map { maybeFile -> maybeFile.toFile() }.toSet()
    }
}