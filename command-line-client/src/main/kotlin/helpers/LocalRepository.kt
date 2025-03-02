package helpers

import java.io.File
import kotlin.io.path.Path

object LocalRepository {
    val table: MutableList<Entry> = mutableListOf()
    val dir: String = System.getProperty("user.dir")

    fun newEntry(name: String, hash: String){
       table.add(Entry(name, hash))
    }

    fun getEntryByName(name: String): Entry? {
        return table.find { it.name == name }
    }

    fun getAllEntryNames(): List<String> {
        return table.map { it.name }
    }

    fun removeEntry(hash: String) {
        table.removeIf { it.hash == hash }
    }

    fun writeToFile() {
        val file = File(Path("$dir/.headchef", "repo.crashout").toString())
        file.writeText(table.joinToString("\n") { "${it.name} ${it.hash}" })
    }

    fun loadFromFile(){
        val file = File(Path("$dir/.headchef", "repo.crashout").toString())
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