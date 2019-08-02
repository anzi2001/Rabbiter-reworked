package com.example.kocja.rabbiter_online.services

import android.app.IntentService
import android.content.Intent
import android.util.Log

import com.example.kocja.rabbiter_online.managers.GsonManager
import com.example.kocja.rabbiter_online.managers.HttpManager
import com.example.kocja.rabbiter_online.models.Events

/**
 * Created by kocja on 28/02/2018.
 */

class AlertIfNotAlertedService : IntentService("This is AlertIfNotAlertedService") {
    override fun onHandleIntent(intent: Intent?) {
        HttpManager.getRequest("seekNotAlertedEvents") { response ->
            for (event in GsonManager.gson.fromJson(response, Array<Events>::class.java)) {
                Log.v("Oops", "This guy was not started")
                NotifyUser.schedule(this, event.dateOfEventMilis, event.eventUUID!!.toString())
            }
        }
    }
}
