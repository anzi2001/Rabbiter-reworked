package com.example.kocja.rabbiter_reworked.services

import android.app.IntentService
import android.app.NotificationManager
import android.content.Context
import android.content.Intent

import com.example.kocja.rabbiter_reworked.databases.Events
import com.example.kocja.rabbiter_reworked.databases.Events_Table
import com.raizlabs.android.dbflow.sql.language.SQLite

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Created by kocja on 27/02/2018.
 */

class processEvents : IntentService("This is processEvents") {
    override fun onHandleIntent(intent: Intent?) {
        val processEventUUID = intent?.getSerializableExtra("processEventUUID") as Int
        val happened = intent.getBooleanExtra("happened", false)
        val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.GERMANY)
        var groupName = intent.getStringExtra("groupName")
        var groupBirthDate = intent.getStringExtra("groupBirthDate")
        if(groupName == null){
            groupName = ""
        }

        SQLite.select()
                .from(Events::class.java)
                .where(Events_Table.eventUUID.eq(processEventUUID))
                .async()
                .querySingleResultCallback { _, events ->
                    if(groupBirthDate == null){
                        groupBirthDate = dateFormatter.format(events?.dateOfEvent)
                    }
                    val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    if (events?.typeOfEvent != Events.READY_MATING_EVENT) {
                        manager.cancel(events!!.id)
                    }
                    if (happened) {
                        val currentDate = Date()
                        events.notificationState = Events.EVENT_SUCCESSFUL
                        when (events.typeOfEvent) {
                            Events.BIRTH_EVENT -> events.eventString = groupBirthDate + ": " + events.name + " gave birth to group "+groupName
                            Events.MOVED_CAGE_EVENT -> events.eventString = dateFormatter.format(currentDate) + ": " + events.name + " was moved into another cage"
                            Events.SLAUGHTERED_EVENT -> events.eventString = dateFormatter.format(currentDate) + ": The group " + events.name + "was slaughtered"
                        }
                    } else {
                        //since we process a no event we can set the notificationState to EVENT_FAILED, so it counts
                        //as notified and doesn't annoy the user
                        events.notificationState = Events.EVENT_FAILED
                        when (events.typeOfEvent) {
                            Events.BIRTH_EVENT -> events.eventString = groupBirthDate + ": " + events.name + " did not give birth to group"+groupName
                            Events.MOVED_CAGE_EVENT -> events.eventString = dateFormatter.format(events.dateOfEvent) + ": " + events.name + " wasn't moved into another cage"
                            Events.SLAUGHTERED_EVENT -> events.eventString = dateFormatter.format(events.dateOfEvent) + ": The group " + events.name + "wasn't slaughtered"
                        }
                    }

                    events.update()
                }.execute()
    }
}
