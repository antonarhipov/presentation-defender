package service

import model.TimerEntry
import java.io.File

class TimerStorage(private val baseDir: File = File(System.getProperty("user.home"))) {
    private val storageDir = File(baseDir, "presentation-defender")

    init {
        storageDir.mkdirs()
    }

    fun saveList(name: String, entries: List<TimerEntry>) {
        val file = File(storageDir, "$name.txt")
        file.writeText(entries.joinToString("\n") { entry ->
            val minutes = entry.duration.inWholeMinutes
            val seconds = entry.duration.inWholeSeconds % 60
            "${entry.description}|$minutes|$seconds"
        })
    }

    fun loadList(name: String): List<TimerEntry>? {
        val file = File(storageDir, "$name.txt")
        if (!file.exists()) return null

        return file.readLines().map { line ->
            val (description, minutes, seconds) = line.split("|")
            TimerEntry.fromMinutesAndSeconds(
                description,
                minutes.toInt(),
                seconds.toInt()
            )
        }
    }

    fun listSavedFiles(): List<String> {
        return storageDir.listFiles { file ->
            file.isFile && file.extension == "txt"
        }?.map { it.nameWithoutExtension } ?: emptyList()
    }

    fun deleteList(name: String) {
        val file = File(storageDir, "$name.txt")
        if (file.exists()) {
            file.delete()
        }
    }
}
