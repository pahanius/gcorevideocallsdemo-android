package gcore.videocalls.demo.di.modules

import dagger.Module
import dagger.Provides
import gcore.videocalls.demo.call.RoomViewModelFactory
import gcore.videocalls.meet.GCoreMeet
import gcore.videocalls.meet.room.RoomManager

@Module
class FactoryModule {

    @Provides
    fun provideRoomManager() = GCoreMeet.instance.roomManager

    @Provides
    fun provideRoomViewModelFactory(roomManager: RoomManager) = RoomViewModelFactory(roomManager)
}