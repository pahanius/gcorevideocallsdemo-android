package gcore.videocalls.demo.call

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
enum class CallCommands : Parcelable {
    MUTE_UNMUTE,
    DISABLE_ENABLE_CAM,
    CLOSE
}