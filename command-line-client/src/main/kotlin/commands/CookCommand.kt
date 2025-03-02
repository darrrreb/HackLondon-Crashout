package commands

import FileHandler
import Hasher
import LocalRepository
import GitDiff.generateDiffFile
import java.io.File
import picocli.CommandLine.Command

@Command(name = "cook", description = ["Prepares files and sends them to the kitchen"], mixinStandardHelpOptions = true,)
class CookCommand : Runnable {
    override fun run() {
        LocalRepository.loadFromFile()
        println("Cooking up a storm!")

        val currentState = FileHandler.getCurrentState("${System.getProperty("user.dir")}/.headchef/currState")
        val ignoredFilesNames = FileHandler.getIgnoredFilesNames()
        val newStateFiles = FileHandler.getFiles()
        val newStateHashes = Hasher.hashAllFiles(newStateFiles, ignoredFilesNames)
        val deletedFileNames = LocalRepository.getAllEntryNames().filter { name -> name !in newStateHashes.map { it.first } }

        val list1: MutableList<File> = mutableListOf()
        val list2: MutableList<File> = mutableListOf()

        for (pair in newStateHashes) {
            val entry = LocalRepository.getEntryByName(pair.first)
            entry?.let {
                if (entry.hash != pair.second) {
                    LocalRepository.removeEntry(entry.hash)
                }
            }
            LocalRepository.newEntry(pair.first, pair.second)
            list1.add(newStateFiles.getByName(pair.first)!!)
        }

        list2.addAll(newStateFiles.filter { it.name in deletedFileNames})
        list2.addAll(currentState)

        generateDiffFile(list1, list2, "${System.getProperty("user.dir")}/.headchef/tmp/")
    }
}

fun Set<File>.getByName(name: String): File? {
    return this.find { it.name == name }
}