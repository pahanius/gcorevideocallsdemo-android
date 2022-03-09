package gcore.videocalls.demo

import android.content.Context
import android.content.res.Configuration
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import gcore.videocalls.demo.call.TEXT_SHADOW_COLOR
import gcore.videocalls.demo.call.TEXT_SHADOW_DX
import gcore.videocalls.demo.call.TEXT_SHADOW_DY
import gcore.videocalls.demo.call.TEXT_SHADOW_RADIUS

fun Context.isNetworkAvailable(): Boolean {
    return MainApp.networkIsAvailable.value!!
}

fun Context.showToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}

fun Context.showToast(messageID: Int) {
    Toast.makeText(this, messageID, Toast.LENGTH_LONG).show()
}

fun Context.getCurrentLocale(): String = resources.configuration.locales[0].language

fun Long.elapsedRealtimeDurationToTimerFormat(): String {
    return (this / 1000).minutesToTimerFormat()
}

fun TextView.setShadow() {
    setShadowLayer(TEXT_SHADOW_RADIUS, TEXT_SHADOW_DX, TEXT_SHADOW_DY, TEXT_SHADOW_COLOR)
}

fun ConstraintLayout.LayoutParams.setOrientation(orientation: Int, width: Int, height: Int) {
    if (orientation == Configuration.ORIENTATION_PORTRAIT) {
        if (width > height) {
            setParamsForVerticalOrientation()
        } else {
            setParamsForHorizontalOrientation()
        }
    } else {
        if (height > width) {
            setParamsForVerticalOrientation()
        } else {
            setParamsForHorizontalOrientation()
        }
    }
}

private fun ConstraintLayout.LayoutParams.setParamsForVerticalOrientation() {
    this.width = ConstraintLayout.LayoutParams.MATCH_CONSTRAINT
    this.height = ConstraintLayout.LayoutParams.MATCH_PARENT
}

private fun ConstraintLayout.LayoutParams.setParamsForHorizontalOrientation() {
    this.width = ConstraintLayout.LayoutParams.MATCH_CONSTRAINT
    this.height = ConstraintLayout.LayoutParams.MATCH_PARENT
}

fun getConstraintRatioForVideo(width: Double, height: Double): String {
    return when (width / height) {
        16.0 / 9.0 -> "W,16:9"
        4.0 / 3.0 -> "W,4:3"
        3.0 / 4.0 -> "H,3:4"
        9.0 / 16.0 -> "H,9:16"
        else -> "W,16:9"
    }
}

fun Long.minutesToTimerFormat(): String {
    val hours = this / 3600
    val minutes = (this / 60) % 60
    val second = this % 60
    return if (hours == 0L) String.format(
        "%02d:%02d",
        minutes,
        second
    ) else String.format("%02d:%02d:%02d", hours, minutes, second)
}