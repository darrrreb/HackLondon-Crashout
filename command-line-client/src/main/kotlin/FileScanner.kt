import java.io.File
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.PathWalkOption
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.readBytes
import kotlin.io.path.walk

object FileScanner {
    private val dir: String = System.getProperty("user.dir")

     fun getFiles(): List<File> {
        return Path(dir)
            .listDirectoryEntries()
            .filter { path -> path.toFile().isFile }
            .map { path -> path.toFile() }
    }
}