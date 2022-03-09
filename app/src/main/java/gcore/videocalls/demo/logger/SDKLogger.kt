package gcore.videocalls.demo.logger

import androidx.lifecycle.MutableLiveData
import gcore.videocalls.meet.GCoreMeet
import gcore.videocalls.meet.logger.Logger
import java.text.SimpleDateFormat

class SDKLogger {
    private val format = SimpleDateFormat.getTimeInstance()

    fun initObserver() {
        GCoreMeet.instance.roomManager.roomProvider.notify.observeForever {
            log.postValue(log.value?.append("${format.format(System.currentTimeMillis())} ${it.text}\n"))
        }
    }

    var log = MutableLiveData(StringBuilder(""))
        private set

    fun clearLog() {
        log.postValue(log.value?.clear())
    }
}