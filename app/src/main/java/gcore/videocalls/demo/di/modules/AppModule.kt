package gcore.videocalls.demo.di.modules

import android.content.Context
import dagger.Module
import dagger.Provides
import gcore.videocalls.demo.MainApp
import javax.inject.Singleton

@Module
class AppModule(private val app: MainApp) {
    @Provides
    @Singleton
    fun provideApplication(): MainApp = app

    @Provides
    @Singleton
    fun provideContext(): Context = app
}