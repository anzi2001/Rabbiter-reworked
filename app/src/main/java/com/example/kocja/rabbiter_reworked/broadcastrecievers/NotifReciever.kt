package com.example.kocja.rabbiter_reworked.broadcastrecievers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.v4.app.NotificationCompat

import com.example.kocja.rabbiter_reworked.R
import com.example.kocja.rabbiter_reworked.activities.AddEntryActivity
import com.example.kocja.rabbiter_reworked.databases.Events
import com.example.kocja.rabbiter_reworked.databases.Events_Table
import com.example.kocja.rabbiter_reworked.services.AlertEventService
import com.example.kocja.rabbiter_reworked.services.askNotifAgain
import com.example.kocja.rabbiter_reworked.services.processEvents
import com.raizlabs.android.dbflow.sql.language.SQLite

import java.util.Random
import java.util.UUID

class NotifReciever : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val eventUUID = intent.getSerializableExtra("eventUUID") as UUID


        val noIntent = Intent(context, askNotifAgain::class.java)
        noIntent.putExtra("eventUUID", eventUUID)
        val noAction = PendingIntent.getService(context, Random().nextInt(), noIntent, 0)
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        SQLite.select()
                .from(Events::class.java)
                .where(Events_Table.eventUUID.eq(eventUUID))
                .and(Events_Table.notificationState.eq(Events.NOT_YET_ALERTED))
                .async()
                .querySingleResultCallback { _, events ->
                    if (events != null) {
                        val randomCode = Random().nextInt()
                        //events.id = randomCode;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            val chanel = NotificationChannel("NotifyEvent", "Event", NotificationManager.IMPORTANCE_DEFAULT)
                            notificationManager.createNotificationChannel(chanel)
                        }
                        val alertEvent = NotificationCompat.Builder(context, "NotifyEvent")
                        alertEvent.setSmallIcon(R.mipmap.dokoncana_ikona_zajec_round_lowres)
                        alertEvent.setContentTitle("Event!")
                        alertEvent.setContentText(events.eventString)

                        when {
                            events.typeOfEvent == 0 -> {
                                val yesIntent = Intent(context, AddEntryActivity::class.java)
                                yesIntent.putExtra("eventUUID", eventUUID)
                                yesIntent.putExtra("getMode", AlertEventService.ADD_BIRTH_FROM_SERVICE)
                                yesIntent.putExtra("happened", true)
                                val yesAction = PendingIntent.getActivity(context, randomCode, yesIntent, 0)

                                alertEvent.setOngoing(true)
                                alertEvent.priority = NotificationCompat.PRIORITY_DEFAULT
                                alertEvent.addAction(0, "Yes", yesAction)
                                alertEvent.addAction(0, "No", noAction)
                                //notificationManager.notify(events.id, alertEvent.build());

                            }
                            events.typeOfEvent == 1 -> {
                                val processEvents = Intent(context, com.example.kocja.rabbiter_reworked.services.processEvents::class.java)
                                processEvents.putExtra("happened", true)
                                processEvents.putExtra("processEventUUID", eventUUID)
                                val processEventsOnDelete = PendingIntent.getService(context, randomCode, processEvents, 0)

                                alertEvent.setDeleteIntent(processEventsOnDelete)
                                alertEvent.priority = NotificationCompat.PRIORITY_DEFAULT
                                //notificationManager.notify(events.id, alertEvent.build());


                            }
                            else -> {
                                val yesProcessEvent = Intent(context, processEvents::class.java)
                                yesProcessEvent.putExtra("processEventUUID", events.eventUUID)
                                yesProcessEvent.putExtra("happened", true)
                                val yesProcessPending = PendingIntent.getService(context, randomCode, yesProcessEvent, 0)

                                alertEvent.setOngoing(true)
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
}
