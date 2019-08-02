package com.example.kocja.rabbiter_online.broadcastrecievers

import android.content.Context
import android.content.Intent
import android.os.PowerManager

import com.example.kocja.rabbiter_online.services.NotifyUser


class AlarmReciever : ServiceStarterBroadCReciever() {
    override fun onReceive(context: Context, intent: Intent) {
        val notificationIntent = Intent(context, NotifyUser::class.java)
        notificationIntent.putExtra("eventUUID", intent.getStringExtra("eventUUID"))
        startService(context, notificationIntent)
        if (NotifyUser.wakeLock == null) {
            val manager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            NotifyUser.wakeLock = manager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "rabbiter:myWakeLock")
        }
        if (NotifyUser.wakeLock?.isHeld == false) {
            NotifyUser.wakeLock?.acquire(1000L * 60 * 5)
        }

    }


}
