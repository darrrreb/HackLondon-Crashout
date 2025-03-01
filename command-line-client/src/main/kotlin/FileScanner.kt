import java.io.File
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.PathWalkOption
import kotlin.io.path.createFile
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.readBytes
import kotlin.io.path.walk

object FileScanner {
    private val dir: String = System.getProperty("user.dir")

     fun getFiles(): Set<File>{
        return Path(dir)
            .listDirectoryEntries()
            .filter { path -> path.toFile().isFile }
            .map { path -> path.toFile() }.toSet()
    }

    fun getIgnoredFiles(): Set<String> {
        val ignoreFilePath = Path("$dir/.headchefignore")
        val ignoreFile = if (ignoreFilePath.exists()) { ignoreFilePath.toFile() } else { ignoreFilePath.createFile().toFile() }
        return ignoreFile.readLines()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .toSet()
    }
}