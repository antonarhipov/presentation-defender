package model

import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

data class TimerEntry(
    val description: String,
    val duration: Duration,
    var progress: Double = 0.0,
    var isRunning: Boolean = false
) {
    fun getRemainingTimeFormatted(): String {
        val remaining = duration * (1 - progress)
        val minutes = remaining.inWholeMinutes
        val seconds = remaining.inWholeSeconds % 60
        return "%d:%02d".format(minutes, seconds)
    }

    companion object {
        fun fromMinutesAndSeconds(description: String, minutes: Int, seconds: Int): TimerEntry {
            return TimerEntry(
                description = description,
                duration = minutes.minutes + seconds.seconds
            )
        }
    }
}
