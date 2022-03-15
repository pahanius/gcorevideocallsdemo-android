package gcore.videocalls.demo

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import gcore.videocalls.demo.call.ENABLE_CAM
import gcore.videocalls.demo.call.ENABLE_MIC
import gcore.videocalls.demo.call.RoomActivity
import gcore.videocalls.meet.GCoreMeet
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_room.*


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        client_Host_Name.setText(GCoreMeet.instance.clientHostName)

        val pref = applicationContext.getSharedPreferences("MyPref", 0) // 0 - for private mode
        val editor: SharedPreferences.Editor = pref.edit()

        room_id.setText(pref.getString("room_id", "serv0_test1"))
        name.setText(pref.getString("user_name", "testN"))

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

            editor.putString("room_id", room_id.text.toString())
            editor.putString("user_name", name.text.toString())
            editor.apply()

//            bundle.putBoolean(IS_MODER, tb_moder.isChecked)

            val intent = Intent(this@MainActivity, RoomActivity::class.java)
            intent.putExtras(bundle)
            startActivity(intent)
        }
    }
}