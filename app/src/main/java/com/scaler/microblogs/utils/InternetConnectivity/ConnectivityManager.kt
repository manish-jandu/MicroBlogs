package com.scaler.microblogs.utils.InternetConnectivity


import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Author and idea from Mitch Tabian
 * https://github.com/mitchtabian/food2fork-compose/blob/master/app/src/main/java/com/codingwithmitch/food2forkcompose/presentation/util/ConnectivityManager.kt
 */

@Singleton
class ConnectivityManager
@Inject
constructor(
    application: Application,
) {

    private val connectionLiveData = ConnectionLiveData(application)

    private val _isNetworkAvailable = MutableLiveData<Boolean>()
    val isNetworkAvailable :LiveData<Boolean> = _isNetworkAvailable

    private val networkObserver = Observer<Boolean> { isConnected -> _isNetworkAvailable.value = isConnected }

    fun registerConnectionObserver() {
        connectionLiveData.observeForever(networkObserver)
    }

    fun unregisterConnectionObserver() {
        connectionLiveData.removeObserver(networkObserver)
    }

}