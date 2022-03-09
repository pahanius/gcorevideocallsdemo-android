package gcore.videocalls.demo.di

import gcore.videocalls.demo.MainApp
import gcore.videocalls.demo.di.components.ApplicationComponent
import gcore.videocalls.demo.di.components.DaggerApplicationComponent
import gcore.videocalls.demo.di.modules.AppModule


class DaggerComponents {
    lateinit var appComponent: ApplicationComponent

    fun initAppComponent(application: MainApp) {
        appComponent = DaggerApplicationComponent.builder()
            .appModule(AppModule(application))
            .build()
    }
}