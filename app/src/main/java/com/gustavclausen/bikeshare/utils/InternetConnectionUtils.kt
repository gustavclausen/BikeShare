package com.gustavclausen.bikeshare.utils

import android.content.Context
import android.net.ConnectivityManager

class InternetConnectionUtils {

    companion object {
        fun isConnected(context: Context): Boolean {
            val connectionManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetworkInfo = connectionManager.activeNetworkInfo

            return activeNetworkInfo != null
        }
    }
}