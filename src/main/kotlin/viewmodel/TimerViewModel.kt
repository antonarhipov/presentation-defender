package viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import model.TimerEntry
import service.TimerStorage
import kotlin.time.Duration

class TimerViewModel {
    private val storage = TimerStorage()

    var entries by mutableStateOf(listOf<TimerEntry>())
        private set

    var isEditMode by mutableStateOf(true)
        private set

    var totalTimeRemaining by mutableStateOf(Duration.ZERO)
        private set

    var savedLists by mutableStateOf<List<String>>(storage.listSavedFiles())
        private set

    private var currentListName by mutableStateOf<String?>(null)
    private var timerJob: Job? = null
    private val coroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    fun addEntry(description: String, minutes: Int, seconds: Int) {
        entries = entries + TimerEntry.fromMinutesAndSeconds(description, minutes, seconds)
        updateTotalTime()
    }

    fun updateEntry(index: Int, description: String, minutes: Int, seconds: Int) {
        if (index in entries.indices) {
            entries = entries.toMutableList().apply {
                this[index] = TimerEntry.fromMinutesAndSeconds(description, minutes, seconds)
            }
            updateTotalTime()
        }
    }

    fun removeEntry(index: Int) {
        if (index in entries.indices) {
            entries = entries.toMutableList().apply {
                removeAt(index)
            }
            updateTotalTime()
        }
    }

    fun toggleMode() {
        isEditMode = !isEditMode
        if (!isEditMode) {
            startTimer()
        } else {
            stopTimer()
        }
    }

    fun saveCurrentList(name: String) {
        storage.saveList(name, entries)
        currentListName = name
        savedLists = storage.listSavedFiles()
    }

    fun loadList(name: String) {
        stopTimer()
        storage.loadList(name)?.let { loadedEntries ->
            entries = loadedEntries
            currentListName = name
            updateTotalTime()
        }
    }

    fun deleteList(name: String) {
        storage.deleteList(name)
        if (currentListName == name) {
            entries = emptyList()
            currentListName = null
            updateTotalTime()
        }
        savedLists = storage.listSavedFiles()
    }

    fun clearCurrentList() {
        entries = emptyList()
        currentListName = null
        updateTotalTime()
    }

    private fun updateTotalTime() {
        totalTimeRemaining = entries.fold(Duration.ZERO) { acc, entry ->
            acc + entry.duration * (1 - entry.progress)
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        entries = entries.map { it.copy(progress = 0.0, isRunning = false) }

        timerJob = coroutineScope.launch {
            for (entryIndex in entries.indices) {
                val entry = entries[entryIndex]
                entries = entries.toMutableList().apply {
                    this[entryIndex] = entry.copy(isRunning = true)
                }

                val startTime = System.currentTimeMillis()
                val duration = entry.duration.inWholeMilliseconds

                while (true) {
                    val currentTime = System.currentTimeMillis()
                    val elapsed = currentTime - startTime
                    val progress = (elapsed.toFloat() / duration).coerceIn(0f, 1f)

                    entries = entries.toMutableList().apply {
                        this[entryIndex] = this[entryIndex].copy(progress = progress.toDouble())
                    }
                    updateTotalTime()

                    if (progress >= 1f) break
                    delay(16) // Approximately 60 FPS
                }

                entries = entries.toMutableList().apply {
                    this[entryIndex] = this[entryIndex].copy(isRunning = false)
                }
            }
            isEditMode = true
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
        entries = entries.map { it.copy(progress = 0.0, isRunning = false) }
        updateTotalTime()
    }
}