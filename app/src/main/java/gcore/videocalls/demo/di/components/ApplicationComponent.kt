package gcore.videocalls.demo.di.components

import android.content.Context
import dagger.Component
import gcore.videocalls.demo.call.RoomActivity
import gcore.videocalls.demo.di.modules.AppModule
import gcore.videocalls.demo.di.modules.FactoryModule
import javax.inject.Singleton


@Component(
    modules = [AppModule::class, FactoryModule::class]
)
@Singleton
interface ApplicationComponent {

    fun context(): Context

    fun inject(activity: RoomActivity)
}