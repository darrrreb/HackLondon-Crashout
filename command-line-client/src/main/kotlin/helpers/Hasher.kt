package helpers

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
    fun hashAllFiles(files: Set<File>, ignoredFiles: Set<String>): List<Pair<String, String>> {
        return files.filter { it.name !in ignoredFiles }.map { Pair(it.name, hash(it.readBytes())) }
    }

    fun sha(files: List<File>): String {
        val instance = MessageDigest.getInstance("SHA-256")
        files.forEach { file ->
            file.inputStream().use { inputStream ->
                val buffer = ByteArray(1024)
                var byteRead: Int
                while (inputStream.read(buffer).also { byteRead = it } != -1) {
                    instance.update(buffer, 0, byteRead)
                }
            }
        }
        val hashBytes = instance.digest()
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
}