package com.example.kocja.rabbiter_reworked.services

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder

import com.example.kocja.rabbiter_reworked.broadcastrecievers.NotifReciever
import com.example.kocja.rabbiter_reworked.databases.Events
import com.example.kocja.rabbiter_reworked.databases.Events_Table
import com.raizlabs.android.dbflow.sql.language.SQLite

import java.util.Date
import java.util.UUID

/**
 * If the user clicked that the event did not happen, try again in a day.
 */

class askNotifAgain : Service() {

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        SQLite.select()
                .from(Events::class.java)
                .where(Events_Table.eventUUID.eq(intent.getSerializableExtra("eventUUID") as UUID))
                .async()
                .querySingleResultCallback { _, events ->
                    val notifManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    notifManager.cancel(events!!.id)

                    if (events.timesNotified > 2) {
                        val processNoEvent = Intent(this, processEvents::class.java)
                        processNoEvent.putExtra("processEventUUID", events.eventUUID)
                        processNoEvent.putExtra("happened", false)
                        startService(processNoEvent)
                    } else {

                        events.timesNotified++
                        events.dateOfEvent = Date(events.dateOfEvent!!.time + 1000L * 60 * 60)
                        events.update()

                        val alertIntent = Intent(this, NotifReciever::class.java)
                        alertIntent.putExtra("eventUUID", events.eventUUID)

                        val alertPending = PendingIntent.getBroadcast(this, events.id, alertIntent, 0)
                        val manager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
                        manager.set(AlarmManager.RTC_WAKEUP, events.dateOfEvent!!.time, alertPending)
                    }
                }.execute()
        return super.onStartCommand(intent, flags, startId)
    }

}
