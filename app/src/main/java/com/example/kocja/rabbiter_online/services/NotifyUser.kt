package com.example.kocja.rabbiter_online.services

import android.app.AlarmManager
import android.app.IntentService
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import androidx.core.app.NotificationCompat

import com.example.kocja.rabbiter_online.R
import com.example.kocja.rabbiter_online.broadcastrecievers.AlarmReciever
import com.example.kocja.rabbiter_online.broadcastrecievers.ProcessReciever
import com.example.kocja.rabbiter_online.models.Events
import com.example.kocja.rabbiter_online.managers.GsonManager
import com.example.kocja.rabbiter_online.managers.HttpManager

import java.util.Random

class NotifyUser : IntentService("NotifyUser") {
    private val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager


    override fun onCreate() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("eventChannel", "eventChannel", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }
        val notification = NotificationCompat.Builder(this, "eventChannel")
                .setContentTitle("processing Event")
                .setSmallIcon(R.mipmap.dokoncana_ikona_zajec_round_lowres)
                .setContentText("content")
        startForeground(Random().nextInt(), notification.build())
        super.onCreate()
    }

    override fun onHandleIntent(intent: Intent?) {
        val eventUUID = intent!!.getStringExtra("eventUUID")


        val builder = NotificationCompat.Builder(this, "eventChannel")
        HttpManager.postRequest("findNotAlertedEvent", eventUUID) { response, _ ->
            val event = GsonManager.gson.fromJson(response, Events::class.java)

            val randomGen = Random()
            val notificationCode = randomGen.nextInt()
            event.id = notificationCode

            builder.setContentTitle("Event!")
            builder.setContentText(event.eventString)
            builder.setSmallIcon(R.mipmap.dokoncana_ikona_zajec_round_lowres)

            if (event.typeOfEvent != Events.READY_MATING_EVENT) {
                builder.setOngoing(true)
                val processYesInput = Intent(applicationContext, ProcessReciever::class.java)
                processYesInput.putExtra("eventUUID", event.eventUUID)
                processYesInput.putExtra("eventHappened", true)
                builder.addAction(R.mipmap.dokoncana_ikona_zajec_round_lowres, "Yes", PendingIntent.getBroadcast(this, randomGen.nextInt(), processYesInput, 0))

                val processNoInput = Intent(applicationContext, ProcessReciever::class.java)
                processNoInput.putExtra("eventUUID", event.eventUUID)
                processNoInput.putExtra("eventHappened", false)

                builder.addAction(R.mipmap.dokoncana_ikona_zajec_round_lowres, "No", PendingIntent.getBroadcast(this, randomGen.nextInt(), processNoInput, 0))
            } else {
                val onDeleteIntent = Intent(applicationContext, ProcessReciever::class.java)
                builder.setDeleteIntent(PendingIntent.getBroadcast(this, randomGen.nextInt(), onDeleteIntent, 0))
            }
            notificationManager.notify(notificationCode, builder.build())

            if (wakeLock!!.isHeld) {
                wakeLock!!.release()

            }
            wakeLock = null

        }
    }

    companion object {
        const val ADD_ENTRY_FROM_BIRTH = 3
        var wakeLock: PowerManager.WakeLock? = null
        fun schedule(context: Context, time: Long, uuid: String) {
            val startSchedule = Intent(context, AlarmReciever::class.java)
            startSchedule.putExtra("eventUUID", uuid)
            val startSchedulePending = PendingIntent.getBroadcast(context, Random().nextInt(), startSchedule, 0)

            val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            manager.set(AlarmManager.RTC_WAKEUP, time, startSchedulePending)


        }
    }

}
