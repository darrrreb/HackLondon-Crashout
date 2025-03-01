import java.io.File
import java.security.MessageDigest

object Hasher {
    const val method: String = "SHA-256"

    @OptIn(ExperimentalStdlibApi::class)
    fun hash(data: ByteArray): String {
        return MessageDigest.getInstance(method).digest(data).toHexString()
    }

   /**
    *  Hashes all files in a list and returns a list of pairs of file names and their respective hashes
    */
    fun hashAllFiles(files: List<File>, ignoredFiles: Set<String>): List<Pair<String, String>> {
        return files.filter { it.name !in ignoredFiles }.map { Pair(it.name, hash(it.readBytes())) }
    }
}