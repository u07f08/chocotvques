package com.example.flowmahuang.chocotvques.module.network

import android.content.Context
import android.net.ConnectivityManager

/**
 * Created by flowmahuang on 2018/3/29.
 */
class CheckInternetIsConnectTool {
    companion object {
        fun isConnect(context: Context): Boolean {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkInfo = cm.activeNetworkInfo
            return networkInfo != null && networkInfo.isConnected
        }
    }
}