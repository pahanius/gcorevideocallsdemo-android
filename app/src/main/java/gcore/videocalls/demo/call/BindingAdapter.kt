package gcore.videocalls.demo.call

import android.util.Log
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import gcore.videocalls.demo.R

@BindingAdapter("binding:mic_state")
fun micState(view: ImageView, isMic: Boolean) {
    Log.d("BindingAdapter", "micState $isMic")
    if (isMic)
        view.setImageResource(R.drawable.ic_mic_notification)
    else
        view.setImageResource(R.drawable.ic_mic_off_notification)
}

@BindingAdapter("binding:video_state")
fun videoState(view: ImageView, isVideo: Boolean) {
    Log.d("BindingAdapter", "videoState $isVideo")
    if (isVideo)
        view.setImageResource(R.drawable.ic_videocam_notification)
    else
        view.setImageResource(R.drawable.ic_videocam_off_notification)
}
