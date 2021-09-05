package com.scaler.microblogs.utils.InternetConnectivity

import android.util.Log
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket

/**
 * Send a ping to googles primary DNS.
 * If successful, that means we have internet.
 *
 *
 * Inspired by:
 * https://github.com/AlexSheva-mason/Rick-Morty-Database/blob/master/app/src/main/java/com/shevaalex/android/rickmortydatabase/utils/networking/ConnectionLiveData.kt
 */
object DoesNetworkHaveInternet {

    // Make sure to execute this on a background thread.//socketFactory: SocketFactory
    fun execute(): Boolean {
        return try {
            Log.d(TAG, "PINGING google.")
            val socket = Socket()
            socket.connect(InetSocketAddress("8.8.8.8", 53),3000)
            socket.close()
            Log.d(TAG, "PING success.")
            true
        } catch (e: IOException) {
            Log.e(TAG, "No internet connection. ${e}")
            false
        }
    }
}