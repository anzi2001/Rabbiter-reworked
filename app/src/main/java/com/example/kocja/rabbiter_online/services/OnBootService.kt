package com.example.kocja.rabbiter_online.services

import android.app.IntentService
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

import com.example.kocja.rabbiter_online.R
import com.example.kocja.rabbiter_online.managers.DataFetcher
import org.koin.android.ext.android.inject
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by kocja on 27/02/2018.
 */

class OnBootService : IntentService("This is OnBootService") {
    private val fetcher: DataFetcher by inject()
    private val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    override fun onCreate() {
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

        super.onCreate()
    }

    override fun onHandleIntent(intent: Intent?) {
        fetcher.findNotAlertedEvents { list ->
            list.forEach {
                EventTriggered.scheduleWorkManager(this,it.dateOfEventMilis,it.eventUUID)
                //NotifyUser.schedule(this, it.dateOfEventMilis, it)
            }

        }
    }
}
