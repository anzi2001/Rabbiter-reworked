package com.example.kocja.rabbiter_reworked.services

import android.app.AlarmManager
import android.app.IntentService
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log

import com.example.kocja.rabbiter_reworked.broadcastrecievers.NotifReciever
import com.example.kocja.rabbiter_reworked.databases.Events
import com.example.kocja.rabbiter_reworked.databases.Events_Table
import com.raizlabs.android.dbflow.sql.language.SQLite

/**
 * Created by kocja on 28/02/2018.
 */

class alertIfNotAlertedService : IntentService("This is alertIfNotAlertedService") {
    override fun onHandleIntent(intent: Intent?) {

        SQLite.select()
                .from(Events::class.java)
                .where(Events_Table.notificationState.eq(Events.NOT_YET_ALERTED))
                .async()
                .queryListResultCallback { _, tResult ->
                    val startNotificationIntent = Intent(this, NotifReciever::class.java)
                    val manager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
                    for (event in tResult) {
                        Log.v("Oops", "This guy was not started")
                        startNotificationIntent.putExtra("eventUUID", event.eventUUID)
                        val startNotification = PendingIntent.getBroadcast(this, event.id, startNotificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                        manager.set(AlarmManager.RTC_WAKEUP, event.dateOfEvent!!.time, startNotification)
                    }
                }.execute()
    }
}
