package com.example.kocja.rabbiter_online.broadcastrecievers

import android.content.Context
import android.content.Intent
import android.os.PowerManager

import com.example.kocja.rabbiter_online.services.ProcessService

class ProcessReciever : ServiceStarterBroadCReciever() {
    override fun onReceive(context: Context, intent: Intent) {
        val processEventIntent = Intent(context, ProcessService::class.java)
        startService(context, processEventIntent)
        if (ProcessService.wakeLock == null) {
            val manager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            ProcessService.wakeLock = manager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "rabbiter:processServiceWake")
        }
        if (ProcessService.wakeLock?.isHeld == false)
            ProcessService.wakeLock?.acquire(1000L * 60 * 5)
    }
}
