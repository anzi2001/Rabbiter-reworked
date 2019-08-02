package com.example.kocja.rabbiter_online.broadcastrecievers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

open class ServiceStarterBroadCReciever : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {

    }

    companion object {
        fun startService(c: Context, notifyService: Intent) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                c.startForegroundService(notifyService)
            } else {
                c.startService(notifyService)
            }
        }
    }
}
