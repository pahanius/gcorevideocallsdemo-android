package gcore.videocalls.demo.call

import android.app.Service
import android.content.Intent
import android.os.IBinder
import gcore.videocalls.demo.BuildConfig
import gcore.videocalls.meet.GCoreMeet
import gcore.videocalls.meet.model.LocalState


const val CALL_COMMAND = "${BuildConfig.APPLICATION_ID}.CALL_COMMAND"

class CallService : Service() {


    override fun onCreate() {
        super.onCreate()
        VideoCallNotifyUtils.oreoNotificationStartService(
            this,
            this,
            "Remote User Name",
            GCoreMeet.instance.roomManager.roomProvider.me.value
        )
        observeCallParams()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) {
            return super.onStartCommand(intent, flags, startId)
        }
        val command = intent.getParcelableExtra(CALL_COMMAND) as? CallCommands
            ?: return super.onStartCommand(intent, flags, startId)

        when (command) {
            CallCommands.CLOSE -> {
                GCoreMeet.instance.starTime = 0L
                GCoreMeet.instance.close()
                startActivity(Intent(this, RoomActivity::class.java).apply {
                    putExtra("Command", "CLOSE")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                })
                stopSelf()
            }
            CallCommands.DISABLE_ENABLE_CAM -> {
                GCoreMeet.instance.roomManager.disableEnableCam()
            }
            CallCommands.MUTE_UNMUTE -> {
                GCoreMeet.instance.roomManager.disableEnableMic()
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        GCoreMeet.instance.roomManager.destroyRoom()
    }

    private fun observeCallParams() {
        GCoreMeet.instance.roomManager.roomProvider.me.observeForever { localState: LocalState ->
            VideoCallNotifyUtils.oreoNotificationStartService(
                this,
                this,
                "Remote User Name",
                localState
            )
        }
    }
}