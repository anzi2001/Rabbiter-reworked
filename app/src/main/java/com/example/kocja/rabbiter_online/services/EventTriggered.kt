package com.example.kocja.rabbiter_online.services

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.example.kocja.rabbiter_online.R
import com.example.kocja.rabbiter_online.managers.WebService
import com.example.kocja.rabbiter_online.models.Events
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.*
import java.util.concurrent.TimeUnit

class EventTriggered(private val context : Context, workerParams : WorkerParameters) : Worker(context,workerParams),
    KoinComponent {
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val fetcher : WebService by inject()
    override fun doWork(): Result {
        val eventUUID = inputData.getString("eventUUID")
        CoroutineScope(Dispatchers.IO).launch{
            val event = fetcher.seekEventByUUID(eventUUID!!)
            val builder = NotificationCompat.Builder(context, "eventChannel")
            val randomGen = Random()
            val notificationCode = randomGen.nextInt()
            event.id = notificationCode

            builder.setContentTitle("Event!")
            builder.setContentText(event.eventString)
            builder.setSmallIcon(R.mipmap.dokoncana_ikona_zajec_round_lowres)

            if (event.typeOfEvent != Events.READY_MATING_EVENT) {
                builder.setOngoing(true)
                val processInput = Intent(context, ProcessService::class.java)
                processInput.putExtra("event", event)
                processInput.putExtra("eventHappened", true)
                builder.addAction(R.mipmap.dokoncana_ikona_zajec_round_lowres, "Yes", PendingIntent.getService(context, randomGen.nextInt(), processInput, 0))

                processInput.putExtra("eventHappened", false)
                builder.addAction(R.mipmap.dokoncana_ikona_zajec_round_lowres, "No", PendingIntent.getService(context, randomGen.nextInt(), processInput, 0))

            } else {
                val onDeleteIntent = Intent(context, ProcessService::class.java)
                builder.setDeleteIntent(PendingIntent.getService(context, randomGen.nextInt(), onDeleteIntent, 0))
            }
            notificationManager.notify(notificationCode, builder.build())
        }
        return Result.success()
    }
    companion object{
        fun scheduleWorkManager(c : Context, eventDate : Long,eventUUID : String){
            val currentDate = Date()
            val difference =
                    if(eventDate < currentDate.time) 0
                    else currentDate.time - eventDate

            val oneTimeWorkRequest = OneTimeWorkRequestBuilder<EventTriggered>()
                    .setInputData(workDataOf("eventUUID" to eventUUID))
                    .setInitialDelay(difference, TimeUnit.MILLISECONDS).build()
            WorkManager.getInstance(c).enqueue(oneTimeWorkRequest)
        }
        const val ADD_ENTRY_FROM_BIRTH = 3
    }
}