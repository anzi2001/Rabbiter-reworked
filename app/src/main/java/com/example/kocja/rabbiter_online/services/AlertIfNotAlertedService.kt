package com.example.kocja.rabbiter_online.services

import android.app.IntentService
import android.content.Intent
import android.util.Log
import com.example.kocja.rabbiter_online.managers.DataFetcher
import org.koin.android.ext.android.inject


/**
 * Created by kocja on 28/02/2018.
 */

class AlertIfNotAlertedService : IntentService("This is AlertIfNotAlertedService") {
    private val fetcher : DataFetcher by inject()
    override fun onHandleIntent(intent: Intent?) {
        fetcher.findNotAlertedEvents {
            for (event in it) {
                Log.v("Oops", "This guy was not started")
                //NotifyUser.schedule(this, event.dateOfEventMilis, event)
                EventTriggered.scheduleWorkManager(this,event.dateOfEventMilis,event.eventUUID)
            }
        }
    }
}
