import com.github.difflib.DiffUtils
import com.github.difflib.UnifiedDiffUtils
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

    fun receiveInit() {
        // Will receive: Lots of files, repoName
        // Make bucket
        // put stuff in bucket
        // success
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

    fun getDiffs(fromSha: String) {
        // Work back through the tree of steps to get list
        // Reverse Lists
        // Get the diffs of every step through time
    }

    fun merge() {
        // handles merging somehow I D E K how
    }

    fun applyAllDiffs(diffList: List<File>, path: String) {
        for (diff in diffList) {
            var fileList = listOf<File>() //get all files in a place - perhaps clone initial files into a file to work with?
            parseApplyDiff(fileList, path, diff)
        }
    }

    fun parseApplyDiff(fileList: List<File>, path: String, diffFile: File) {
        val files = fileList
        val diffs = diffFile.readLines().joinToString(separator = " ").split("=======")
        for (diff in diffs) {
            val keys = diff.split("======")
            for (i in files.indices) {
                if (files[i].name == keys[0]) {
                    if (keys[1].toInt() == 0) {
                        val fileString = (DiffUtils.patch(files[i].readLines(), UnifiedDiffUtils.parseUnifiedDiff(keys[2].replace("!====!", "\n").split("\n"))).joinToString("\n"))
                        File(path + keys[0]).writeText(fileString)
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