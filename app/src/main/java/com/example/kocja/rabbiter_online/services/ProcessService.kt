package com.example.kocja.rabbiter_online.services

import android.app.IntentService
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import androidx.core.app.NotificationCompat

import com.example.kocja.rabbiter_online.R
import com.example.kocja.rabbiter_online.models.Events
import com.example.kocja.rabbiter_online.managers.GsonManager
import com.example.kocja.rabbiter_online.managers.HttpManager

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProcessService : IntentService("this is processEvents") {

    override fun onCreate() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("processChannel", "Proccessing", NotificationManager.IMPORTANCE_DEFAULT)
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
        val processNotification = NotificationCompat.Builder(this, "processChannel")
                .setContentTitle("Processing")
                .setContentText("this is processing")
                .setSmallIcon(R.mipmap.dokoncana_ikona_zajec_round_lowres)
        startForeground(5, processNotification.build())
        super.onCreate()
    }

    override fun onHandleIntent(intent: Intent?) {
        val eventHappened = intent!!.getBooleanExtra("eventHappened", false)
        val eventUUID = intent.getStringExtra("eventUUID")

        HttpManager.postRequest("seekSingleEntry", eventUUID) { response, _ ->
            var event = GsonManager.gson.fromJson(response, Events::class.java)

            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.UK)

            if (event.timesNotified > 3) {
                val todayDateString = dateFormat.format(Date()) + ":"
                event = setEventString(eventHappened, event, todayDateString)
            } else {
                event.timesNotified = event.timesNotified + 1
                event.dateOfEventMilis = event.dateOfEventMilis + day
                event.dateOfEvent = dateFormat.format(Date(event.dateOfEventMilis))

                NotifyUser.schedule(applicationContext, event.dateOfEventMilis, event.eventUUID!!.toString())
            }

            HttpManager.postRequest("updateEvent", GsonManager.gson.toJson(event)) { _, _ -> wakeLock!!.release() }
        }
    }

    private fun setEventString(eventHappened: Boolean, event: Events, formattedDate: String): Events {
        event.eventString = if (eventHappened) {
            event.notificationState = Events.EVENT_SUCCESSFUL
             when (event.typeOfEvent) {
                Events.BIRTH_EVENT -> formattedDate + event.name + "gave birth"
                Events.READY_MATING_EVENT -> formattedDate + event.name + "was ready for mating"
                Events.MOVE_GROUP_EVENT -> formattedDate + " the group " + event.name + " was moved into another cage"
                Events.SLAUGHTER_EVENT -> formattedDate + " the group " + event.name + " was slaughtered"
                else -> "Event type was not found"
            }
        } else {
            event.notificationState = Events.EVENT_FAILED
            when (event.typeOfEvent) {
                Events.BIRTH_EVENT ->  formattedDate + event.name + "didn't give birth"
                Events.READY_MATING_EVENT -> formattedDate + event.name + "was ready for mating"
                Events.MOVE_GROUP_EVENT -> formattedDate + "the group " + event.name + " wasn't moved into another cage"
                Events.SLAUGHTER_EVENT -> formattedDate + "the group " + event.name + " wasn't slaughtered"
                else -> "Event type was not found"
            }
        }
        return event
    }

    companion object {
        var wakeLock: PowerManager.WakeLock? = null
        const val day = 1000L * 60 * 60 * 24
    }
}
