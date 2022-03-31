package gcore.videocalls.demo.call

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.media.AudioManager
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import gcore.videocalls.demo.MainApp
import gcore.videocalls.demo.MainApp.Companion.sdkLogger
import gcore.videocalls.demo.R
import gcore.videocalls.demo.databinding.ActivityRoomBinding
import gcore.videocalls.demo.isNetworkAvailable
import gcore.videocalls.demo.logger.LogDialog
import gcore.videocalls.meet.GCoreMeet
import gcore.videocalls.meet.room.RoomManager
import kotlinx.android.synthetic.main.activity_room.*
import javax.inject.Inject

import android.app.AlertDialog;
import android.content.DialogInterface;
import gcore.videocalls.meet.room.RequestType
import kotlinx.android.synthetic.main.activity_main.*


const val ENABLE_MIC = "ENABLE_MIC"
const val ENABLE_CAM = "ENABLE_CAM"
const val IS_MODER = "IS_MODER"

class HeadsetStateReceiver() : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val isConnected = intent.getIntExtra("state", 0) == 1
        Log.d("HeadsetStateReceiver", "HeadsetStateReceiver onReceive isConnected:$isConnected")
        Log.d(
            "HeadsetStateReceiver",
            "HeadsetStateReceiver onReceive state:${intent.getIntExtra("state", 0)}"
        )
        // Can also be 2 if headset is attached w/o mic.

        val audioManager =
            context.applicationContext?.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.isSpeakerphoneOn = !isConnected

        if (isConnected)
            audioManager.mode = AudioManager.MODE_IN_CALL
        else
            audioManager.mode = AudioManager.MODE_NORMAL
    }
}

class RoomActivity : AppCompatActivity() {

    private val intentFilter = IntentFilter(AudioManager.ACTION_HEADSET_PLUG)
    private var headsetStateReceiver: HeadsetStateReceiver? = null

    override fun onStart() {
        Log.d(TAG, "RoomActivity onStart")
        super.onStart()
        headsetStateReceiver = HeadsetStateReceiver()
        registerReceiver(headsetStateReceiver, intentFilter)
    }

    override fun onStop() {
        Log.d(TAG, "RoomActivity onStop")
        unregisterReceiver(headsetStateReceiver)
        val audioManager =
            applicationContext?.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.isSpeakerphoneOn = false
        audioManager.mode = AudioManager.MODE_NORMAL
        super.onStop()
    }

    companion object {
        private val TAG = RoomActivity::class.java.simpleName

        //TODO separate all permisions code in one enum
        private const val REQUEST_PERMISSIONS_CODE = 146
    }

    private val permissions = arrayOf(
        Manifest.permission.INTERNET,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.CAMERA,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    private var roomManager: RoomManager = GCoreMeet.instance.roomManager
    private lateinit var buttons: List<View>
    private lateinit var adapter: PeersRecyclerViewAdapter

    @Inject
    lateinit var factory: RoomViewModelFactory
    private val viewModel: RoomViewModel by viewModels { factory }
    private lateinit var dataBinding: ActivityRoomBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        MainApp.daggerComponents.appComponent.inject(this)
        super.onCreate(savedInstanceState)
        if (intent.getStringExtra("Command") == "CLOSE") {
            finish()
        }

        configDataBinding()
        checkPermission()

//        tb_connect.setOnCheckedChangeListener { compoundButton, b ->
//            if (b) {
//                val pref =
//                    applicationContext.getSharedPreferences("MyPref", 0) // 0 - for private mode
//                val editor: SharedPreferences.Editor = pref.edit()
//
//
//                GCoreMeet.instance.setRoomId(pref.getString("room_id", "serv0_test1")!!)
//                GCoreMeet.instance.roomManager.displayName = "sss"
//                GCoreMeet.instance.roomManager.isModer = true
//                viewModel.startCall.postValue(Unit)
//            } else {
//                GCoreMeet.instance.close()
//            }
//        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent ?: return
        if (intent.getStringExtra("Command") == "CLOSE") {
            finish()
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        configDataBinding()
    }

    private fun initViewModel() {
        viewModel.specialistName.value = "Remote user name"
        viewModel.closeCall.observe(this, Observer {
            startService(Intent(this, CallService::class.java).apply {
                putExtra(CALL_COMMAND, CallCommands.CLOSE as Parcelable)
            })
        })
        viewModel.closed.observe(this, Observer {
            if (!isNetworkAvailable()) {
                showNetworkErrorDialog()
            } else {
                finish()
            }
        })
        viewModel.startCall.observe(this, Observer {
            startCall()
        })
        viewModel.showControlButtons.observe(this, Observer { showControls ->
            buttons.forEach {
                it.animate().apply {
                    duration = FADE_DURATION
                    alphaBy(if (showControls) FADE_IN_ALPHA else FADE_OUT_ALPHA)
                    alpha(if (showControls) FADE_OUT_ALPHA else FADE_IN_ALPHA)
                }.start()
            }
        })

        viewModel.askUserConfirmMicDialogOpen.observe(this, Observer {
            if (!it) return@Observer

            val builder: AlertDialog.Builder = AlertDialog.Builder(this)
            builder.setTitle("Mics are currently disabled in this room")
            builder.setMessage("Ask moderator to enable mic?")
            builder.setPositiveButton(
                "Yes",
                DialogInterface.OnClickListener { _, _ ->
                    viewModel.askModeratorEnableMic()
                    viewModel.askUserConfirmMicDialogOpen.value = false
                })
            builder.setNegativeButton(
                "Cancel", DialogInterface.OnClickListener { _, _ ->
                    viewModel.askUserConfirmMicDialogOpen.value = false
                }
            )
            val dialog: AlertDialog = builder.create()
            dialog.show()
        })
        viewModel.askUserConfirmCamDialogOpen.observe(this, Observer {
            if (!it) return@Observer

            val builder: AlertDialog.Builder = AlertDialog.Builder(this)
            builder.setTitle("Cams are currently disabled in this room")
            builder.setMessage("Ask moderator to enable cam?")
            builder.setPositiveButton(
                "Yes",
                DialogInterface.OnClickListener { _, _ ->
                    viewModel.askModeratorEnableCam()
                    viewModel.askUserConfirmCamDialogOpen.value = false
                })
            builder.setNegativeButton(
                "Cancel", DialogInterface.OnClickListener { _, _ ->
                    viewModel.askUserConfirmCamDialogOpen.value = false
                }
            )
            val dialog: AlertDialog = builder.create()
            dialog.show()
        })

        viewModel.audioPermissionDialogOpen.observe(this, Observer {
            if (!it) return@Observer

            val builder: AlertDialog.Builder = AlertDialog.Builder(this)
            builder.setTitle("Request from moderator")
            builder.setMessage("Turn on the mic?")
            builder.setPositiveButton(
                "Yes",
                DialogInterface.OnClickListener { _, _ ->
//                    viewModel.disableEnableMic()
                    viewModel.localVideo.enableMic()
                    viewModel.audioPermissionDialogOpen.value = false
                })
            builder.setNegativeButton(
                "Cancel", DialogInterface.OnClickListener { _, _ ->
                    viewModel.audioPermissionDialogOpen.value = false
                }
            )
            val dialog: AlertDialog = builder.create()
            dialog.show()
        })
        viewModel.videoPermissionDialogOpen.observe(this, Observer {
            if (!it) return@Observer

            val builder: AlertDialog.Builder = AlertDialog.Builder(this)
            builder.setTitle("Request from moderator")
            builder.setMessage("Turn on the cam?")
            builder.setPositiveButton(
                "Yes",
                DialogInterface.OnClickListener { _, _ ->
//                    viewModel.disableEnableCam()
                    viewModel.localVideo.enableCam()
                    viewModel.videoPermissionDialogOpen.value = false
                })
            builder.setNegativeButton(
                "Cancel", DialogInterface.OnClickListener { _, _ ->
                    viewModel.videoPermissionDialogOpen.value = false
                }
            )
            val dialog: AlertDialog = builder.create()
            dialog.show()
        })

        viewModel.audioPermissionAfterAskDialogOpen.observe(this, Observer {
            if (!it) return@Observer

            val builder: AlertDialog.Builder = AlertDialog.Builder(this)
            builder.setTitle("The moderator confirmed the request")
            builder.setMessage("Turn on the mic?")
            builder.setPositiveButton(
                "Yes",
                DialogInterface.OnClickListener { _, _ ->
                    viewModel.localVideo.enableMic()
                    viewModel.audioPermissionAfterAskDialogOpen.value = false
                })
            builder.setNegativeButton(
                "Cancel", DialogInterface.OnClickListener { _, _ ->
                    viewModel.audioPermissionAfterAskDialogOpen.value = false
                }
            )
            val dialog: AlertDialog = builder.create()
            dialog.show()
        })
        viewModel.videoPermissionAfterAskDialogOpen.observe(this, Observer {
            if (!it) return@Observer

            val builder: AlertDialog.Builder = AlertDialog.Builder(this)
            builder.setTitle("The moderator confirmed the request")
            builder.setMessage("Turn on the cam?")
            builder.setPositiveButton(
                "Yes",
                DialogInterface.OnClickListener { _, _ ->
                    viewModel.localVideo.enableCam()
                    viewModel.videoPermissionAfterAskDialogOpen.value = false
                })
            builder.setNegativeButton(
                "Cancel", DialogInterface.OnClickListener { _, _ ->
                    viewModel.videoPermissionAfterAskDialogOpen.value = false
                }
            )
            val dialog: AlertDialog = builder.create()
            dialog.show()
        })

        viewModel.requestToModeratorDialog.observe(this, Observer { peerData ->
            if (peerData == null) return@Observer

            val builder: AlertDialog.Builder = AlertDialog.Builder(this)
            builder.setTitle("Hey!")
            builder.setMessage(
                "${peerData.userName} wants to enable " +
                        when (peerData.requestType) {
                            RequestType.AUDIO -> "mic"
                            RequestType.VIDEO -> "cam"
                            RequestType.SHARE -> "sharing"
                        }
            )
            builder.setPositiveButton(
                "Yes",
                DialogInterface.OnClickListener { _, _ ->
                    viewModel.acceptedPermission(peerData)
                })
            builder.setNegativeButton(
                "Cancel", DialogInterface.OnClickListener { _, _ ->
                }
            )
            val dialog: AlertDialog = builder.create()
            dialog.show()
        })

        viewModel.init()
        startService(Intent(this, CallService::class.java))
    }

    private fun configDataBinding() {
        dataBinding = DataBindingUtil.setContentView(this, R.layout.activity_room)
//        dataBinding.status.setShadow()
//        dataBinding.specialistName.setShadow()
        buttons = listOf(dataBinding.disableVideo, dataBinding.disableAudio, dataBinding.close)
        dataBinding.localVideo.connect()
        viewModel.localVideo = dataBinding.localVideo

        dataBinding.lifecycleOwner = this
        dataBinding.viewModel = viewModel

        dataBinding.logButton.setOnClickListener {
            LogDialog().showDialog(this)
        }
    }

    private fun startCall() {
        runOnUiThread {
            sdkLogger?.clearLog()
            sdkLogger?.initObserver()

            viewModel.showProgress()


            Log.d(TAG, "GCoreMeet.instance.roomManager ${GCoreMeet.instance.roomManager}")
            roomManager = GCoreMeet.instance.roomManager
            Log.d(TAG, "GCoreMeet.instance.roomManager ${GCoreMeet.instance.roomManager}")

            GCoreMeet.instance.startConnection(this)

//            if (waiting == true) {
//                val ask = roomManager.askModeratorToJoin()
//                if (ask == false) return@runOnUiThread
//            }

            viewModel.hideProgress()

            viewModel.connect()

            dataBinding.localVideo.connect()
            configPeer()

            roomManager.options.startWithCam = intent.getBooleanExtra(ENABLE_CAM, true)
            roomManager.options.startWithMic = intent.getBooleanExtra(ENABLE_MIC, true)

            if (roomManager.isClosed()) {
                roomManager.join()
            }
//
//            val waiting = roomManager.checkWaitingRoom()
//            val ask = roomManager.askModeratorToJoin()

        }
    }

    private fun configPeer() {
        val size = GCoreMeet.instance.getPeers().value?.allPeers?.size
        val spanCount = if (size ?: 0 > 1) 2 else 1

        val lm = GridLayoutManager(this, spanCount)
        remote_peers.layoutManager = lm
        adapter = PeersRecyclerViewAdapter(this, this) {
            ModerOptionsDialogFragment(it.displayName, it.id, viewModel::handleModerEvents).show(
                supportFragmentManager,
                "ModerOptionsDialogFragment"
            )
        }
        remote_peers.adapter = adapter
        GCoreMeet.instance.getPeers().observe(this, Observer {
            lm.spanCount = if (it?.allPeers?.size ?: 0 > 1) 2 else 1
            remote_peers.recycledViewPool.clear()
            adapter.notifyDataSetChanged()
        })
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_PERMISSIONS_CODE -> handlePermissionRequestResult(grantResults)
            else -> {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            }
        }
    }

    private fun handlePermissionRequestResult(grantResults: IntArray) {
        if (isPermissionGranted(grantResults)) {
            initViewModel()
            MainApp.networkIsAvailable.observe(this, Observer {
                if (!it) {
                    showNetworkErrorDialog()
                }
            })
        } else {
            finish()
        }
    }

    private fun isPermissionGranted(grantResults: IntArray) = grantResults.isNotEmpty()
            && grantResults.size == permissions.size
            && grantResults.all { it == PackageManager.PERMISSION_GRANTED }

    private fun checkPermission() {
        requestPermissions(permissions, REQUEST_PERMISSIONS_CODE)
    }

    private fun showNetworkErrorDialog() {
        //do nothing
    }

    override fun finish() {
        viewModel.closeCall()
        super.finish()
    }
}