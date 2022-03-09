package gcore.videocalls.demo

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import gcore.videocalls.demo.logger.SDKLogger
import gcore.videocalls.demo.di.DaggerComponents
import gcore.videocalls.meet.GCoreMeet

class MainApp : Application() {
    companion object {
        val daggerComponents = DaggerComponents()
        var networkIsAvailable = MutableLiveData(false)
        var sdkLogger: SDKLogger? = null
    }

    override fun onCreate() {
        super.onCreate()
//        GCoreMeet.instance.init(this, sdkLogger, false)
        GCoreMeet.instance.init(this, null, true)
        // DataBindingUtil.setDefaultComponent(BindingComponent())
        daggerComponents.initAppComponent(this)
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.registerDefaultNetworkCallback(object :
            ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                networkIsAvailable.postValue(true)
            }

            override fun onUnavailable() {
                networkIsAvailable.postValue(false)
            }

            override fun onLost(network: Network) {
                networkIsAvailable.postValue(false)
            }
        })

        sdkLogger = SDKLogger()

        GCoreMeet.instance.webRtcHost = "webrtc1.youstreamer.com"
        GCoreMeet.instance.port = 443
        GCoreMeet.instance.clientHostName = "https://meet.gcorelabs.com"
        GCoreMeet.instance.hostName = "meet.gcorelabs.com"
    }
}