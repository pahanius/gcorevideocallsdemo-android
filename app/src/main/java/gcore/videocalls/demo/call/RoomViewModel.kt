package gcore.videocalls.demo.call

import android.os.SystemClock
import androidx.databinding.ObservableField
import androidx.lifecycle.Observer
import androidx.lifecycle.MutableLiveData
import gcore.videocalls.demo.MainApp.Companion.sdkLogger
import kotlinx.coroutines.launch
import gcore.videocalls.demo.base.BaseViewModel
import gcore.videocalls.demo.base.SingleLiveEvent
import gcore.videocalls.demo.elapsedRealtimeDurationToTimerFormat
import gcore.videocalls.demo.utils.Stopwatch
import gcore.videocalls.meet.GCoreMeet
import gcore.videocalls.meet.logger.LLog
import gcore.videocalls.meet.model.Consumers
import gcore.videocalls.meet.model.LocalState
import gcore.videocalls.meet.model.Peers
import gcore.videocalls.meet.model.RoomInfo
import gcore.videocalls.meet.network.ConnectionState
import gcore.videocalls.meet.network.ConsumerKind
import gcore.videocalls.meet.network.WaitingState
import gcore.videocalls.meet.room.RequestPeerData
import gcore.videocalls.meet.room.RoomManager
import gcore.videocalls.meet.ui.view.me.ILocalVideoView
import gcore.videocalls.meet.ui.view.peer.PeerVideoRoomViewModel
import java.util.*

class RoomViewModel(private val roomManager: RoomManager) : BaseViewModel() {

    private val stopwatch = Stopwatch()

//    val textLog: ObservableField<String> = ObservableField()

    val audioOnly: ObservableField<Boolean> = ObservableField()
    val audioOnlyInProgress: ObservableField<Boolean> = ObservableField()
    val audioMuted: ObservableField<Boolean> = ObservableField()
    val restartIceInProgress: ObservableField<Boolean> = ObservableField()
    val displayPeerAvatar = MutableLiveData<Boolean>()

    val showControlButtons = MutableLiveData(true)

    val specialistName = MutableLiveData<String>()
    val duration = MutableLiveData<String>()
    val userName = MutableLiveData<String>()
    val roomId = MutableLiveData<String>()
    val isModer = MutableLiveData<Boolean>()

    val closeCall = SingleLiveEvent<Unit>()

    val startCall = SingleLiveEvent<Unit>()
    val closed = SingleLiveEvent<Unit>()

    val consultationDuration = MutableLiveData<Long>()

    lateinit var localVideo: ILocalVideoView

    private var peers: Peers? = null
    private var waitingPeers: Peers? = null
    private var isConnected = false
    private var updateTokenTimer: Timer? = null
    private var pingTimer: Timer? = null

    val waitingProgressVisible = SingleLiveEvent<Boolean>(false)
    private val waitingStateObserver = Observer<WaitingState> {
        if (it == WaitingState.IN_WAITING)
            waitingProgressVisible.postValue(true)
        else
            waitingProgressVisible.postValue(false)
    }

    val askUserConfirmMicDialogOpen = MutableLiveData<Boolean>()
    val askUserConfirmCamDialogOpen = MutableLiveData<Boolean>()

    val audioPermissionDialogOpen = MutableLiveData<Boolean>()
    val videoPermissionDialogOpen = MutableLiveData<Boolean>()

    val audioPermissionAfterAskDialogOpen = MutableLiveData<Boolean>()
    val videoPermissionAfterAskDialogOpen = MutableLiveData<Boolean>()

    private val askUserConfirmMicObserver = Observer<Unit?> {
        if (askUserConfirmMicDialogOpen.value != true)
            askUserConfirmMicDialogOpen.postValue(true)
    }

    private val askUserConfirmCamObserver = Observer<Unit?> {
        if (askUserConfirmCamDialogOpen.value != true)
            askUserConfirmCamDialogOpen.postValue(true)
    }

    private val acceptedAudioPermissionObserver = Observer<Unit?> {
        if (audioPermissionDialogOpen.value != true)
            audioPermissionDialogOpen.postValue(true)
    }
    private val acceptedVideoPermissionObserver = Observer<Unit?> {
        if (videoPermissionDialogOpen.value != true)
            videoPermissionDialogOpen.postValue(true)
    }

    private val acceptedAudioPermissionFromModeratorObserver = Observer<Unit?> {
        localVideo.enableMic()
//        if (audioPermissionAfterAskDialogOpen.value != true)
//            audioPermissionAfterAskDialogOpen.postValue(true)
    }
    private val acceptedVideoPermissionFromModeratorObserver = Observer<Unit?> {
        localVideo.enableCam()
//        if (videoPermissionAfterAskDialogOpen.value != true)
//            videoPermissionAfterAskDialogOpen.postValue(true)
    }

    val requestToModeratorDialog = SingleLiveEvent<RequestPeerData>()
    private val requestToModeratorObserver = Observer<RequestPeerData?> {
        it.let { requestToModeratorDialog.postValue(it) }
    }


    init {
        updateTokenTimer = Timer()
        userName.value = ""
    }

    fun init() {
        roomManager.roomProvider.waitingState.observeForever(waitingStateObserver)

        roomManager.roomProvider.closedByModerator.observeForever(closedByModeratorObserver)
        roomManager.askUserConfirmMic.observeForever(askUserConfirmMicObserver)
        roomManager.askUserConfirmCam.observeForever(askUserConfirmCamObserver)
        roomManager.roomProvider.acceptedAudioPermission.observeForever(
            acceptedAudioPermissionObserver
        )
        roomManager.roomProvider.acceptedVideoPermission.observeForever(
            acceptedVideoPermissionObserver
        )
        roomManager.roomProvider.acceptedAudioPermissionFromModerator.observeForever(
            acceptedAudioPermissionFromModeratorObserver
        )
        roomManager.roomProvider.acceptedVideoPermissionFromModerator.observeForever(
            acceptedVideoPermissionFromModeratorObserver
        )

        checkWaitingRoom.observeForever(checkWaitingRoomObserver)
        askModeratorToJoin.observeForever(askModeratorToJoinObserver)
        roomManager.roomProvider.requestToModerator.observeForever(requestToModeratorObserver)

        if (isConnected) {
            startCall.value = Unit
            return
        }
        viewModelScope.launch {
            showProgressBar()
            startStopwatch()
//            if (isModer.value == true) {
            startCall.value = Unit
            hideProgressBar()
//            } else {
//                checkWaitingRoom.value = roomManager.checkWaitingRoom()
//            }
        }
//        sdkLogger.log = {
//            textLog.set(textLog.get() + it)
//        }
    }

    private fun startStopwatch() {
        GCoreMeet.instance.starTime = SystemClock.elapsedRealtime()
        stopwatch.start(GCoreMeet.instance.starTime) {
            consultationDuration.value = it
            duration.value = it.elapsedRealtimeDurationToTimerFormat()
        }
    }

    fun hideShowButtons() {
        val isShowed = showControlButtons.value ?: true
        showControlButtons.value = !isShowed
    }

    fun restartIce() {
        roomManager.restartIce()
    }

    fun disableEnableCam() {
//        if (roomManager.roomProvider.camAllowed.value == true)
        localVideo.disableEnableCam()
//        else
//            requestCamToModeratorDialogOpen.postValue(true)
    }

    fun disableEnableMic() {
//        if (roomManager.roomProvider.micAllowed.value == true)
        localVideo.disableEnableMic()
//        else
//            requestMicToModeratorDialogOpen.postValue(true)
    }

    fun askModeratorEnableMic() {
        roomManager.askModeratorEnableMic()
    }

    fun askModeratorEnableCam() {
        roomManager.askModeratorEnableCam()
    }

    fun changeCamera() {
        localVideo.changeCam()
    }

    fun closeCall() {
        closeCall.value = Unit
    }

    private val peersObservable = Observer { peers: Peers? ->
        peers.let {
            this.peers = it
            handleDisplayPeerAvatar()
        }
    }

    private val waitingPeersObservable = Observer { peers: Peers? ->
        peers.let {
            this.waitingPeers = it
//            handleDisplayPeerAvatar()
        }
    }

    private val meObservable = Observer { localState: LocalState ->
        audioOnly.set(localState.isAudioOnly)
        audioOnlyInProgress.set(localState.isAudioOnlyInProgress)
        audioMuted.set(localState.isAudioMuted)
        restartIceInProgress.set(localState.isRestartIceInProgress)
//        roomManager.checkWaitingRoom()
    }

    private val connectedObservable = Observer { it: Boolean ->
        if (it && isConnected != it) {
//                roomManager.changeDisplayName(userName.value ?: "")
            userName.postValue(roomManager.displayName)
            roomId.postValue(roomManager.roomId)
            isModer.postValue(roomManager.isModer)
        }
        isConnected = it
        handleDisplayPeerAvatar()
    }

    private val roomInfoObservable = Observer { it: RoomInfo ->
        when (it.connectionState) {
            ConnectionState.CLOSED -> close()
            else -> {
                //do nothing
            }
        }
    }
    private val closedByModeratorObserver = Observer { _: Unit ->
        close()
    }

    fun connect() {

        roomManager.roomProvider.peers.removeObserver(peersObservable)
        roomManager.roomProvider.peers.observeForever(peersObservable)

        roomManager.roomProvider.waitingPeers.removeObserver(waitingPeersObservable)
        roomManager.roomProvider.waitingPeers.observeForever(waitingPeersObservable)

        roomManager.roomProvider.me.removeObserver(meObservable)
        roomManager.roomProvider.me.observeForever(meObservable)

        roomManager.roomProvider.connected.removeObserver(connectedObservable)
        roomManager.roomProvider.connected.observeForever(connectedObservable)

        roomManager.roomProvider.roomInfo.removeObserver(roomInfoObservable)
        roomManager.roomProvider.roomInfo.observeForever(roomInfoObservable)
    }

    private fun close() {
        updateTokenTimer?.cancel()
        pingTimer?.cancel()
        updateTokenTimer = null
        pingTimer = null
        GCoreMeet.instance.starTime = 0L
        closed.value = Unit

        roomManager.roomProvider.waitingState.removeObserver(waitingStateObserver)

        roomManager.roomProvider.closedByModerator.removeObserver(closedByModeratorObserver)
        roomManager.askUserConfirmMic.removeObserver(askUserConfirmMicObserver)
        roomManager.askUserConfirmCam.removeObserver(askUserConfirmCamObserver)
        roomManager.roomProvider.acceptedAudioPermission.removeObserver(
            acceptedAudioPermissionObserver
        )
        roomManager.roomProvider.acceptedVideoPermission.removeObserver(
            acceptedVideoPermissionObserver
        )
        roomManager.roomProvider.acceptedAudioPermissionFromModerator.removeObserver(
            acceptedAudioPermissionFromModeratorObserver
        )
        roomManager.roomProvider.acceptedVideoPermissionFromModerator.removeObserver(
            acceptedVideoPermissionFromModeratorObserver
        )

        checkWaitingRoom.removeObserver(checkWaitingRoomObserver)
        askModeratorToJoin.removeObserver(askModeratorToJoinObserver)

        roomManager.roomProvider.peers.removeObserver(peersObservable)
        roomManager.roomProvider.waitingPeers.removeObserver(waitingPeersObservable)
        roomManager.roomProvider.me.removeObserver(meObservable)
        roomManager.roomProvider.connected.removeObserver(connectedObservable)
        roomManager.roomProvider.roomInfo.removeObserver(roomInfoObservable)
        roomManager.roomProvider.requestToModerator.removeObserver(requestToModeratorObserver)
    }

    override fun onCleared() {
        updateTokenTimer?.cancel()
        pingTimer?.cancel()
        updateTokenTimer = null
        pingTimer = null
        super.onCleared()
    }

    private fun handleDisplayPeerAvatar() {
        if (peers?.allPeers?.firstOrNull()?.consumers?.values?.firstOrNull { it == ConsumerKind.VIDEO.value } == null) {
            displayPeerAvatar.value = true
            return
        } else displayPeerAvatar.value = isConnected == false
    }

    fun handleModerEvents(event: ModerOptionsEvent) {
        when (event) {
            is ModerOptionsEvent.PeerRemove -> {
//                roomManager.removeUserByModerator(event.peerId)
            }
            is ModerOptionsEvent.PeerMuteMic -> {
                roomManager.disableAudioByModerator(event.peerId)
            }
            is ModerOptionsEvent.PeerTurnOfWebCam -> {
                roomManager.disableVideoByModerator(event.peerId)
            }
            is ModerOptionsEvent.PeerTurnOfSharing -> {
                roomManager.disableShareByModerator(event.peerId)
            }
            is ModerOptionsEvent.PeerEnableMic -> {
                roomManager.enableAudioByModerator(event.peerId)
            }
            is ModerOptionsEvent.PeerEnableWebcam -> {
                roomManager.enableVideoByModerator(event.peerId)
            }
            is ModerOptionsEvent.PeerEnableSharing -> {
                roomManager.enableShareByModerator(event.peerId)
            }
        }
    }

    private val askModeratorToJoin = SingleLiveEvent<Boolean?>(null)
    private val askModeratorToJoinObserver = Observer<Boolean?> {
        when (it) {
            true -> {
                startCall.value = Unit
                hideProgressBar()
            }
            false -> {
                closed.value = Unit
                closeCall.value = Unit
            }
        }
    }

    private val checkWaitingRoom = SingleLiveEvent<Boolean?>(null)
    private val checkWaitingRoomObserver = Observer<Boolean?> {
        when (it) {
            null -> return@Observer
            true -> {
                askModeratorToJoin.value = roomManager.askModeratorToJoin()
            }
            false -> {
                startCall.value = Unit
                hideProgressBar()
            }
        }
    }

    fun showProgress() {
        showProgressBar()
    }

    fun hideProgress() {
        hideProgressBar()
    }

    fun acceptedPermission(peerData: RequestPeerData) {
        roomManager.acceptedPermission(peerData)
    }
//
//    fun checkWaitingRoom() {
//
//    }

}