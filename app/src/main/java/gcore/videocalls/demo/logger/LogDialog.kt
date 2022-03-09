package gcore.videocalls.demo.logger

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.Window
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import gcore.videocalls.demo.MainApp.Companion.sdkLogger
import gcore.videocalls.demo.R

class LogDialog {
    var dialog: Dialog? = null
    fun showDialog(activity: AppCompatActivity) {
        dialog = Dialog(activity)
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog?.setCancelable(false)
        dialog?.setCanceledOnTouchOutside(true)
        dialog?.setContentView(R.layout.log_dialog)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.window?.setLayout(MATCH_PARENT, MATCH_PARENT)

        dialog?.show()

        val logView = dialog?.findViewById(R.id.log_view) as TextView

        sdkLogger?.log?.observe(activity, Observer {
            logView.text = it
        })
    }

    fun close() {
        dialog?.cancel()
    }
}