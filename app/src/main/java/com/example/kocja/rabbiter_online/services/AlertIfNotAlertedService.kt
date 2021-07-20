package com.example.kocja.rabbiter_online.services

import android.app.IntentService
import android.content.Intent
import android.util.Log
import androidx.core.app.JobIntentService
import com.example.kocja.rabbiter_online.managers.WebService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject


/**
 * Created by kocja on 28/02/2018.
 */

class AlertIfNotAlertedService : IntentService("This is AlertIfNotAlertedService") {
    private val fetcher : WebService by inject()
    override fun onHandleIntent(intent: Intent?) {

        CoroutineScope(Dispatchers.IO).launch{
            val result = fetcher.findNotAlertedEvents()
            result.forEach{
                Log.v("Oops", "This guy was not started")
                //NotifyUser.schedule(this, event.dateOfEventMilis, event)
                EventTriggered.scheduleWorkManager(this@AlertIfNotAlertedService,it.dateOfEventMilis,it.eventUUID)
            }
        }
    }
}

