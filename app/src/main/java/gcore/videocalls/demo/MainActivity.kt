package gcore.videocalls.demo

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_room.*
import gcore.videocalls.demo.call.ENABLE_CAM
import gcore.videocalls.demo.call.ENABLE_MIC
import gcore.videocalls.demo.call.IS_MODER
import gcore.videocalls.demo.call.RoomActivity
import gcore.videocalls.meet.GCoreMeet

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        client_Host_Name.setText(GCoreMeet.instance.clientHostName)
        room_id.setText("serv0_test1")
        join.setOnClickListener {
            if (room_id.text.isNullOrEmpty()) {
                return@setOnClickListener
            }
            GCoreMeet.instance.setRoomId(room_id.text.toString())
            GCoreMeet.instance.clientHostName = client_Host_Name.text.toString()
            GCoreMeet.instance.roomManager.displayName = name.text.toString()
            GCoreMeet.instance.roomManager.isModer = tb_moder.isChecked
            val bundle = Bundle()
            bundle.putBoolean(ENABLE_MIC, tb_audio.isChecked)
            bundle.putBoolean(ENABLE_CAM, tb_video.isChecked)
//            bundle.putBoolean(IS_MODER, tb_moder.isChecked)

            val intent = Intent(this@MainActivity, RoomActivity::class.java)
            intent.putExtras(bundle)
            startActivity(intent)
        }
    }
}