package com.example.kocja.rabbiter_online.services

import android.app.IntentService
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

import com.example.kocja.rabbiter_online.R
import com.example.kocja.rabbiter_online.managers.WebService
import com.example.kocja.rabbiter_online.models.Events
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.core.KoinComponent
import org.koin.core.component.KoinComponent

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProcessService : IntentService("this is processEvents"), KoinComponent {
    private val fetcher : WebService by inject()
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
        var event = intent.getParcelableExtra<Events>("event")!!

        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.UK)

        if (event.timesNotified > 3) {
            val todayDateString = dateFormat.format(Date()) + ":"
            event = setEventString(eventHappened, event, todayDateString)
        } else {
            event.timesNotified = event.timesNotified + 1
            event.dateOfEventMilis = event.dateOfEventMilis + day
            event.dateOfEvent = dateFormat.format(Date(event.dateOfEventMilis))

            EventTriggered.scheduleWorkManager(this,event.dateOfEventMilis,event.eventUUID)
            //NotifyUser.schedule(applicationContext, event.dateOfEventMilis, event)
        }

        CoroutineScope(Dispatchers.IO).launch{
            fetcher.updateEvent(event)
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
                Events.BIRTH_EVENT -> formattedDate + event.name + "didn't give birth"
                Events.READY_MATING_EVENT -> formattedDate + event.name + "was ready for mating"
                Events.MOVE_GROUP_EVENT -> formattedDate + "the group " + event.name + " wasn't moved into another cage"
                Events.SLAUGHTER_EVENT -> formattedDate + "the group " + event.name + " wasn't slaughtered"
                else -> "Event type was not found"
            }
        }
        return event
    }

    companion object {
        const val day = 1000L * 60 * 60 * 24
    }
}
