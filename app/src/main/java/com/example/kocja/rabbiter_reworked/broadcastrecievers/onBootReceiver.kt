package com.example.kocja.rabbiter_reworked.broadcastrecievers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.kocja.rabbiter_reworked.services.onBootService


/**
 * Created by kocja on 27/02/2018.
 */

class onBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val startOnBootService = Intent(context, onBootService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(startOnBootService)
        } else {
            context.startService(startOnBootService)
        }

    }
}
