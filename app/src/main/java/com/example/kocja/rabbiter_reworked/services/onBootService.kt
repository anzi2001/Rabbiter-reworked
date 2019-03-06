package com.example.kocja.rabbiter_reworked.services

import android.app.AlarmManager
import android.app.IntentService
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.kocja.rabbiter_reworked.R
import com.example.kocja.rabbiter_reworked.broadcastrecievers.NotifReciever
import com.example.kocja.rabbiter_reworked.databases.Events
import com.example.kocja.rabbiter_reworked.databases.Events_Table
import com.raizlabs.android.dbflow.sql.language.SQLite

/**
 * Created by kocja on 27/02/2018.
 */

class onBootService : IntentService("This is onBootService") {

    override fun onHandleIntent(intent: Intent?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val chanel = NotificationChannel("checkBoot", "Boot", NotificationManager.IMPORTANCE_DEFAULT)
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(chanel)
        }

        val builder = NotificationCompat.Builder(this, "checkBoot")
                .setSmallIcon(R.mipmap.dokoncana_ikona_zajec_round_lowres)
                .setContentTitle("Configuring alarms")
                .setContentText("Configuring")

        this.startForeground(1, builder.build())


        SQLite.select()
                .from(Events::class.java)
                .where(Events_Table.notificationState.eq(Events.NOT_YET_ALERTED))
                .async()
                .queryListResultCallback { _, tResult ->
                    val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
                    val setNotification = Intent(this, NotifReciever::class.java)
                    for (event in tResult) {
                        setNotification.putExtra("eventUUID", event.eventUUID)
                        val setNotifIntent = PendingIntent.getBroadcast(this, event.id, setNotification, 0)
                        alarmManager.set(AlarmManager.RTC_WAKEUP, event.dateOfEvent!!.time, setNotifIntent)

                    }
                }.execute()

    }
}
