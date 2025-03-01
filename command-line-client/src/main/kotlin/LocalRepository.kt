import java.io.File
import kotlin.io.path.Path

class LocalRepository {
    val table: MutableList<Entry> = mutableListOf()

    fun newEntry(name: String, hash: String){
       table.add(Entry(name, hash))
    }

    fun getEntry(hash: String): Entry? {
        return table.find { it.hash == hash }
    }

    fun isValidEntry(hash: String): Boolean {
        return table.any { it.hash == hash }
    }

    fun removeEntry(hash: String) {
        table.removeIf { it.hash == hash }
    }

    fun writeToFile() {
        val file = File(Path(System.getProperty("user.dir"), ".headchef").toString())
        file.writeText(table.joinToString("\n") { "${it.name} ${it.hash}" })
    }

    fun load(){
        val file = File(Path(System.getProperty("user.dir"), ".headchef").toString())
        if (file.exists()) {
            table.addAll(file.readLines().map {
                val (name, hash) = it.split(" ")
                Entry(name, hash)
            })
        }
    }
}

data class Entry(
    val name: String,
    val hash: String
)