import com.github.difflib.DiffUtils
import com.github.difflib.UnifiedDiffUtils
import crashout.Step
import kcl.seg.rtt.utils.aws.S3Service
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.BufferedWriter
import java.io.FileWriter

object GitDiff {
    fun generateDiffFile(oldList: List<File>, newList: List<File>, path: String) {
        val difference = generateDiff(oldList, newList)
        BufferedWriter(FileWriter(path + "diff")).use { writer ->
            for (i in difference.indices) {
                writer.write(difference[i].fileName)
                writer.write("======")
                writer.write(difference[i].flag.toString())
                if (difference[i].patch.isNotEmpty()) {
                    writer.write("======")
                    writer.write(difference[i].patch.replace("\n", "!====!"))
                }
                if (i != difference.size - 1) {
                    writer.write("=======")
                }
            }
        }
    }

    fun receiveStep() {
        // Step should be sent as diff, repoName, parentCommit, sha
        // Take parent commit and add this commit to its children list
        // generate the relevant ai thingies
        // create a step object
        // put it in the cuhloud
    }

    fun receivePull() {
        // Will receive: one SHA
        // Will return all files at this commit
        // Using getDiffs/Applyalldiffs
    }

    fun sendTreeToFrontend() {
        // Receives a repo name
        // Should return the full list of commits from here
        // Data expected by the front end: Steps(SHA, ShortMessage, [childrenSHAs])
    }

    fun receiveMergeRequestFromFrontEnd() {
        // Receives 2 commit SHAs
        // merges dem ones dere
        // returns a success or fail msg
    }

    suspend fun getDiffs(repoName: String,fromSha: String) : List<File>{
        var stepString = S3Service.getFile(repoName, "steps/" + fromSha + ".json")
        var step = Json.decodeFromString<Step>(String(stepString))
        var parentSha = step.parentSha
        val shlist = listOf(step.sha).toMutableList()
        while (parentSha != null) {
            shlist.add(parentSha[0])
            stepString = S3Service.getFile(repoName, "steps/" + parentSha[0] + ".json")
            step = Json.decodeFromString<Step>(String(stepString))
            parentSha = step.parentSha
        }
        shlist.reverse()
        val diffs = emptyList<File>() //Enzos method thingy to get em all
        var diffList = mutableListOf<File>()
        for (sha in shlist) {
            for (diff in diffs) {
                if (diff.name == sha) {
                    diffList.add(diff)
                }
            }
        }
        return diffList
    }

    suspend fun merge(repoName: String, sha1: String, sha2: String) {
        val listOfDiffs1 = getDiffs(repoName, sha1)
        val listOfDiffs2 = getDiffs(repoName, sha2)
        val longerList = maxOf(listOfDiffs1, listOfDiffs2, compareBy { it.size }).toMutableList()
        val shorterList = minOf(listOfDiffs1, listOfDiffs2, compareBy { it.size })
        for (diff in shorterList) {
            if (diff in longerList) {
                continue
            } else {
                longerList.add(diff)
            }
        }
        val str = applyAllDiffs(longerList, "tmp/")
        if (str == "") {
            applyAllDiffs(listOfDiffs1, "tmp2/")
            generateDiffFile(getFiles("tmp2"), getFiles("tmp"), "tmp/")
        } else {
            applyAllDiffs(listOfDiffs1, "tmp/" + str)
            applyAllDiffs(listOfDiffs2, "tmp2/" + str)
            // ASK CHATGPT TO TOUCH it AND put that in tmp
            val gptFile : File
            applyAllDiffs(listOfDiffs1, "tmp2/")
            generateDiffFile(getFiles("tmp2"), getFiles("tmp"), "tmp3/")
        }
        //Diff file in tmp3 goes into merge commit
    }

    fun getFiles(directoryPath: String): List<File> {
        val directory = File(directoryPath)
        if (directory.exists() && directory.isDirectory) {
            return directory.listFiles()?.filter { it.isFile }?.toList() ?: emptyList()
        }
        return emptyList()
    }

    fun applyAllDiffs(diffList: List<File>, directoryPath: String) : String {
        val directory = File(directoryPath)
        if (directory.exists() && directory.isDirectory) {
            directory.listFiles()?.forEach { file ->
                file.delete();
            }
        }

        for (diff in diffList) {
            var fileList = listOf<File>() //get all files in a place - perhaps clone initial files into a file to work with?
            val str = parseApplyDiff(fileList, directoryPath, diff)
            if (str == "") { return "" } else { return str }
        }
        return ""
    }

    fun parseApplyDiff(fileList: List<File>, path: String, diffFile: File) : String {
        val files = fileList
        val diffs = diffFile.readLines().joinToString(separator = " ").split("=======")
        for (diff in diffs) {
            val keys = diff.split("======")
            for (i in files.indices) {
                if (files[i].name == keys[0]) {
                    if (keys[1].toInt() == 0) {
                        try {
                            val fileString = (DiffUtils.patch(files[i].readLines(), UnifiedDiffUtils.parseUnifiedDiff(keys[2].replace("!====!", "\n").split("\n"))).joinToString("\n"))
                            File(path + keys[0]).writeText(fileString)
                        } catch (e : Exception) { return files[i].name }
                    }
                    else if (keys[1].toInt() == 1) {
                        files[i].delete()
                    }
                }
            }
            if (keys[1].toInt() == 2) {
                val fileString = (DiffUtils.patch(listOf<String>(), UnifiedDiffUtils.parseUnifiedDiff(keys[2].replace("!====!", "\n").split("\n"))).joinToString("\n"))
                File(path + keys[0]).writeText(fileString)
            }
        }
        return ""
    }

    data class Diff(
        val fileName: String,
        var flag: Int,
        var patch: String
    )

    fun generateDiff(originFiles: List<File>, newFiles: List<File>): List<Diff> {
        val result = mutableListOf<Diff>()

        for (file in originFiles) {
            val diff = Diff(file.name, 0, "")
            var patched = false
            for (file2 in newFiles) {
                if (file.name == file2.name) {
                    diff.patch = (UnifiedDiffUtils.generateUnifiedDiff(file.name, file2.name, file.readLines(), DiffUtils.diff(file.readLines(), file2.readLines()), 0).joinToString("\n"))
                    patched = true
                    break
                }
            }
            if (!patched) {
                diff.flag = 1
            }
            result.add(diff)
        }
        for (file in newFiles) {
            var added = false
            for (file2 in originFiles) {
                if (file.name == file2.name) {
                    added = true
                    break
                }
            }
            if (!added) {
                result.add(Diff(file.name, 2, UnifiedDiffUtils.generateUnifiedDiff(file.name, file.name, file.readLines(), DiffUtils.diff(listOf<String>(), file.readLines()), 0).joinToString("\n")))
            }
        }

        return result
    }
}