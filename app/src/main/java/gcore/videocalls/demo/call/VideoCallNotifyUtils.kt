package gcore.videocalls.demo.call

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Parcelable
import android.os.SystemClock
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import gcore.videocalls.demo.R
import gcore.videocalls.meet.GCoreMeet
import gcore.videocalls.meet.model.LocalState

private const val CHANNEL_ID = "ForegroundServiceChannel"
private const val CHANNEL_NAME = "Foreground Service Channel"

/**
 * the filed is used for update/remove notification
 */
private const val NOTIFICATION_ID = 13453

object VideoCallNotifyUtils {

    @JvmOverloads
    fun oreoNotificationStartService(
        context: Context,
        service: Service,
        text: String? = "",
        callParamsState: LocalState?
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(
                context,
                chanelName ="Video call"
            )
            val notificationBuilder: NotificationCompat.Builder = NotificationCompat.Builder(
                context,
                CHANNEL_ID
            )
            val notificationIntent = Intent(context, RoomActivity::class.java)
            val pendingMainIntent = PendingIntent.getActivity(
                context.applicationContext,
                0,
                notificationIntent,
                0
            )
            val notification: Notification = notificationBuilder
                .setCustomContentView(getCustomView(context, text, callParamsState))
                .setSmallIcon(android.R.drawable.sym_def_app_icon)
                .setAutoCancel(false)
                .setOnlyAlertOnce(true)
                .setChannelId(CHANNEL_ID)
                .setColor(context.getColor(R.color.black))
                .setContentIntent(pendingMainIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build()
            service.startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun getCustomView(context: Context, name: String?, callParamsState: LocalState?): RemoteViews {
        val collapsedView = RemoteViews(
            context.packageName,
            R.layout.notification_video_call
        )
        collapsedView.setTextViewText(R.id.title, name)
        collapsedView.setImageViewResource(R.id.video, if (callParamsState?.isAudioOnly == true) {
            R.drawable.ic_videocam_off_notification
        } else {
            R.drawable.ic_videocam_notification
        })
        collapsedView.setImageViewResource(R.id.audio, if (callParamsState?.isAudioMuted == true) {
            R.drawable.ic_mic_off_notification
        } else {
            R.drawable.ic_mic_notification
        })
        configCallChronometer(collapsedView)
        configButtons(
            context,
            collapsedView,
            CallCommands.DISABLE_ENABLE_CAM,
            R.id.video,
            VideoCallNotificationRequestCode.VIDEO
        )
        configButtons(
            context,
            collapsedView,
            CallCommands.CLOSE,
            R.id.closeCall,
            VideoCallNotificationRequestCode.END_CALL
        )
        configButtons(
            context,
            collapsedView,
            CallCommands.MUTE_UNMUTE,
            R.id.audio,
            VideoCallNotificationRequestCode.AUDIO
        )
        return collapsedView
    }

    private fun configCallChronometer(notificationView: RemoteViews) {
        notificationView.setChronometer(
            R.id.call_chronometer,
            SystemClock.elapsedRealtime() - GCoreMeet.instance.starTime,
            null,
            true
        )
    }

    private fun configButtons(
        context: Context,
        remoteViews: RemoteViews,
        command: CallCommands,
        idView: Int,
        requestCode: VideoCallNotificationRequestCode
    ) {
        var intent = Intent(context, CallService::class.java).apply {
            putExtra(CALL_COMMAND, command as Parcelable)
        }
        val pendingIntent =
            PendingIntent.getService(
                context,
                requestCode.code,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        remoteViews.setOnClickPendingIntent(idView, pendingIntent)
    }

    @JvmOverloads
    fun createNotificationChannel(
        context: Context,
        chanelId: String? = CHANNEL_ID,
        chanelName: String? = CHANNEL_NAME
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notifyManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (notifyManager.getNotificationChannel(chanelId) == null) {
                val mChannel = NotificationChannel(
                    chanelId,
                    chanelName,
                    NotificationManager.IMPORTANCE_HIGH
                )
                mChannel.enableVibration(false)
                notifyManager.createNotificationChannel(mChannel)
            }
        }
    }

    @JvmOverloads
    fun destroyChannel(
        context: Context,
        chanelId: String? = CHANNEL_ID
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notifyManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notifyManager.deleteNotificationChannel(chanelId)
        }
    }
}

private enum class VideoCallNotificationRequestCode(val code: Int) {
    VIDEO(1101),
    AUDIO(1102),
    END_CALL(1103)
}
