package com.example.kocja.rabbiter_reworked.services

import android.app.IntentService
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

import com.example.kocja.rabbiter_reworked.R
import com.example.kocja.rabbiter_reworked.activities.AddEntryActivity
import com.example.kocja.rabbiter_reworked.databases.Events
import com.example.kocja.rabbiter_reworked.databases.Events_Table
import com.example.kocja.rabbiter_reworked.databases.Events_Table.eventUUID
import com.raizlabs.android.dbflow.sql.language.SQLite


import java.util.Random
import java.util.UUID

/**
 * Service for notifying the user about an event that is going to happen,
 * or may have already happened
 */

class AlertEventService : IntentService("This is a AlertEventService") {
    override fun onHandleIntent(intent: Intent?) {
        val eventID = intent!!.getSerializableExtra("eventUUID") as UUID


        val noIntent = Intent(this, processEvents::class.java)
        noIntent.putExtra("processEventUUID", eventID)
        noIntent.putExtra("happened",false)
        val noAction = PendingIntent.getService(this, Random().nextInt(), noIntent, 0)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        SQLite.select()
                .from(Events::class.java)
                .where(Events_Table.eventUUID.eq(eventUUID))
                .and(Events_Table.notificationState.eq(Events.NOT_YET_ALERTED))
                .async()
                .querySingleResultCallback { _, events ->
                    if (events != null) {
                        val randomCode = Random().nextInt()
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            val chanel = NotificationChannel("NotifyEvent", "Event", NotificationManager.IMPORTANCE_DEFAULT)
                            notificationManager.createNotificationChannel(chanel)
                        }
                        val alertEvent = NotificationCompat.Builder(this, "NotifyEvent")
                        alertEvent.setSmallIcon(R.mipmap.dokoncana_ikona_zajec_round_lowres)
                        alertEvent.setContentTitle("Event!")
                        alertEvent.setContentText(events.eventString)

                        when(events.typeOfEvent) {
                            Events.BIRTH_EVENT -> {
                                val yesIntent = Intent(this, AddEntryActivity::class.java)
                                yesIntent.putExtra("eventUUID", eventID)
                                yesIntent.putExtra("getMode", ADD_BIRTH_FROM_SERVICE)
                                yesIntent.putExtra("happened", true)
                                val yesAction = PendingIntent.getActivity(this, randomCode, yesIntent, 0)



                                alertEvent.priority = NotificationCompat.PRIORITY_DEFAULT
                                alertEvent.addAction(0, "Yes", yesAction)
                                alertEvent.addAction(0, "No", noAction)

                            }
                            Events.READY_MATING_EVENT -> {
                                val processEvents = Intent(this, com.example.kocja.rabbiter_reworked.services.processEvents::class.java)
                                processEvents.putExtra("happened", true)
                                processEvents.putExtra("processEventUUID", eventID)
                                val processEventsOnDelete = PendingIntent.getService(this, randomCode, processEvents, 0)

                                alertEvent.setDeleteIntent(processEventsOnDelete)
                                alertEvent.priority = NotificationCompat.PRIORITY_DEFAULT

                            }
                            else -> {
                                val yesProcessEvent = Intent(this, processEvents::class.java)
                                yesProcessEvent.putExtra("processEventUUID", events.eventUUID)
                                yesProcessEvent.putExtra("happened", true)
                                val yesProcessPending = PendingIntent.getService(this, randomCode, yesProcessEvent, 0)

                                alertEvent.priority = NotificationCompat.PRIORITY_DEFAULT
                                alertEvent.addAction(0, "Yes", yesProcessPending)
                                alertEvent.addAction(0, "No", noAction)
                            }
                        }
                        notificationManager.notify(events.id, alertEvent.build())
                        events.update()
                    }
                }.execute()


    }
    companion object {
        const val ADD_BIRTH_FROM_SERVICE = 3
    }
}
