package gcore.videocalls.demo.utils

import android.os.SystemClock
import kotlinx.coroutines.*

class Stopwatch {

    private val updateDuration = 1000L

    private val scope = CoroutineScope(Dispatchers.IO)

    private var startTime = 0L

    private var isStopped = false

    fun start(
        startTime: Long,
        onProcessing: (Long) -> Unit = { durationMs -> }
    ) {
        this.startTime = startTime
        scope.launch {
            while (!isStopped) {
                val currentTime = getCurrentValue()
                withContext(Dispatchers.Main) {
                    onProcessing(currentTime)
                }
                delay(updateDuration)
            }
        }
    }

    fun stop() {
        if (scope.isActive) {
            isStopped = true
            scope.cancel()
        }
    }

    fun getCurrentValue() = (SystemClock.elapsedRealtime() - startTime)
}