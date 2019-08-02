package com.example.kocja.rabbiter_online.broadcastrecievers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.kocja.rabbiter_online.services.OnBootService


/**
 * Created by kocja on 27/02/2018.
 */

class OnBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val startOnBootService = Intent(context, OnBootService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(startOnBootService)
        } else {
            context.startService(startOnBootService)
        }

    }
}
