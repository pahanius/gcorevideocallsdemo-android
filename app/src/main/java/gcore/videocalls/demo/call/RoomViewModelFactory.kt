package gcore.videocalls.demo.call

import gcore.videocalls.demo.base.BaseViewModelFactory
import gcore.videocalls.meet.GCoreMeet
import gcore.videocalls.meet.room.RoomManager

class RoomViewModelFactory(private val roomManager: RoomManager) : BaseViewModelFactory<RoomViewModel>(RoomViewModel::class.java) {

    override fun createViewModel(): RoomViewModel =
        RoomViewModel(GCoreMeet.instance.roomManager)
}