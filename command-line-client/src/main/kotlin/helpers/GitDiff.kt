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

    fun parseApplyDiff(fileList: List<File>, path: String, diffPath: String) {
        val files = fileList
        val diffs = File(diffPath).readLines().joinToString(separator = " ").split("=======")
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