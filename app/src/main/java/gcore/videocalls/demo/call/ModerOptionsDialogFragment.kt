package gcore.videocalls.demo.call

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import gcore.videocalls.demo.R
import kotlinx.android.synthetic.main.moder_options.*
import java.util.*

sealed class ModerOptionsEvent {
    data class PeerRemove(val peerId: String) : ModerOptionsEvent()
    data class PeerMuteMic(val peerId: String) : ModerOptionsEvent()
    data class PeerTurnOfWebCam(val peerId: String) : ModerOptionsEvent()
    data class PeerTurnOfSharing(val peerId: String) : ModerOptionsEvent()
    data class PeerEnableMic(val peerId: String) : ModerOptionsEvent()
    data class PeerEnableWebcam(val peerId: String) : ModerOptionsEvent()
    data class PeerEnableSharing(val peerId: String) : ModerOptionsEvent()
}

class ModerOptionsDialogFragment(
    private val peerName: String,
    private val peerId: String,
    private val events: (ModerOptionsEvent) -> Unit
) : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Objects.requireNonNull(dialog)?.window!!.requestFeature(Window.FEATURE_NO_TITLE)
        return inflater.inflate(R.layout.moder_options, null, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(
            STYLE_NORMAL,
            android.R.style.Theme_Black_NoTitleBar_Fullscreen
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        peer_name.text = peerName

        peer_remove.setOnClickListener {
            events.invoke(ModerOptionsEvent.PeerRemove(peerId))
            dismiss()
        }
        peer_mute_mic.setOnClickListener {
            events.invoke(ModerOptionsEvent.PeerMuteMic(peerId))
            dismiss()
        }
        peer_turn_off_webcam.setOnClickListener {
            events.invoke(ModerOptionsEvent.PeerTurnOfWebCam(peerId))
            dismiss()
        }
        peer_turn_off_sharing.setOnClickListener {
            events.invoke(ModerOptionsEvent.PeerTurnOfSharing(peerId))
            dismiss()
        }
        peer_enable_mic.setOnClickListener {
            events.invoke(ModerOptionsEvent.PeerEnableMic(peerId))
            dismiss()
        }
        peer_enable_webcam.setOnClickListener {
            events.invoke(ModerOptionsEvent.PeerEnableWebcam(peerId))
            dismiss()
        }
        peer_enable_sharing.setOnClickListener {
            events.invoke(ModerOptionsEvent.PeerEnableSharing(peerId))
            dismiss()
        }
        btn_cancel.setOnClickListener {
            dismiss()
        }
    }
}
