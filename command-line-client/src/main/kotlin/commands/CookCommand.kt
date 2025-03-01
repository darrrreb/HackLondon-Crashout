package commands

import FileScanner
import Hasher
import LocalRepository
import java.io.File
import kotlin.io.path.Path
import picocli.CommandLine.Command

@Command(name = "cook", description = ["Prepares files and sends them to the kitchen"], mixinStandardHelpOptions = true,)
class CookCommand : Runnable {
    override fun run() {
        LocalRepository.load()
        println("Cooking up a storm!")

        //LIST1: Old and deleted files
        //LIST2: New and modified files
        val files = FileScanner.getFiles()
        val ignoredFiles = FileScanner.getIgnoredFiles()
        //val previousState = GET FROM REMOTE
        val currentState = Hasher.hashAllFiles(files, ignoredFiles)
        val deletedFileNames = LocalRepository.getAllEntryNames().filter { name -> name !in currentState.map { it.first } }
        val newFiles: MutableList<File> = mutableListOf()
        val modifiedFiles: MutableList<File> = mutableListOf()

        for (pair in currentState) {
            val entry = LocalRepository.getEntryByName(pair.first)
            entry?.let {
                if (entry.hash != pair.second) {
                    println("File ${pair.first} has been modified")
                    LocalRepository.removeEntry(entry.hash)
                    LocalRepository.newEntry(pair.first, pair.second)
                    modifiedFiles.add(files.getByName(pair.first)!!)
                } else {
                    println("File ${pair.first} was not touched")
                }
            } ?: run {
                println("New file ${pair.first} found")
                newFiles.add(Path(System.getProperty("user.dir"), pair.first).toFile())
                LocalRepository.newEntry(pair.first, pair.second)
                newFiles.add(files.getByName(pair.first)!!)
            }
        }

        val list2 = newFiles + modifiedFiles
        val list1 = deletedFileNames //+ oldFiles

        //Generate diffs
        //Send diffs to remote
    }
}

fun Set<File>.getByName(name: String): File? {
    return this.find { it.name == name }
}