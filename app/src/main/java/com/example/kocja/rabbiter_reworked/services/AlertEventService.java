package com.example.kocja.rabbiter_reworked.services;

import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import com.example.kocja.rabbiter_reworked.R;
import com.example.kocja.rabbiter_reworked.activities.addEntryActivity;
import com.example.kocja.rabbiter_reworked.databases.Events;
import com.example.kocja.rabbiter_reworked.databases.Events_Table;
import com.raizlabs.android.dbflow.sql.language.SQLite;

import java.util.Random;
import java.util.UUID;

/**
 * Service for notifying the user about an event that is going to happen,
 * or may have already happened
 */

public class AlertEventService extends IntentService {
    public static final int ADD_BIRTH_FROM_SERVICE =3;
    public AlertEventService(){
        super("This is a AlertEventService");
    }
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        UUID eventUUID =(UUID) intent.getSerializableExtra("eventUUID");


        Intent noIntent = new Intent(this,askNotifAgain.class);
        noIntent.putExtra("eventUUID",eventUUID);
        PendingIntent noAction = PendingIntent.getService(this, new Random().nextInt(),noIntent,0);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        SQLite.select()
                .from(Events.class)
                .where(Events_Table.eventUUID.eq(eventUUID))
                .and(Events_Table.notificationState.eq(Events.NOT_YET_ALERTED))
                .async()
                .querySingleResultCallback((transaction, events) -> {
            if(events != null) {
                int randomCode = new Random().nextInt();
                //events.id = randomCode;
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                    NotificationChannel chanel = new NotificationChannel("NotifyEvent","Event",NotificationManager.IMPORTANCE_DEFAULT);
                    notificationManager.createNotificationChannel(chanel);
                }
                if (events.typeOfEvent == 0) {
                    Intent yesIntent = new Intent(this, addEntryActivity.class);
                    yesIntent.putExtra("eventUUID", eventUUID);
                    yesIntent.putExtra("getMode", ADD_BIRTH_FROM_SERVICE);
                    yesIntent.putExtra("happened", true);
                    PendingIntent yesAction = PendingIntent.getActivity(this, randomCode, yesIntent, 0);
                    NotificationCompat.Builder alertEvent = new NotificationCompat.Builder(this, "NotifyEvent")
                            .setSmallIcon(R.mipmap.dokoncana_ikona_zajec)
                            .setContentTitle("Event!")
                            .setContentText(events.eventString)
                            .setOngoing(true)
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                            .addAction(0, "Yes", yesAction)
                            .addAction(0, "No", noAction);
                    notificationManager.notify(events.id, alertEvent.build());
                } else if (events.typeOfEvent == 1) {
                    Intent processEvents = new Intent(this, com.example.kocja.rabbiter_reworked.services.processEvents.class);
                    processEvents.putExtra("happened", true);
                    processEvents.putExtra("processEventUUID", eventUUID);
                    PendingIntent processEventsOnDelete = PendingIntent.getService(this, new Random().nextInt(), processEvents, 0);

                    NotificationCompat.Builder alertEvent2 = new NotificationCompat.Builder(this, "NotifyEvent")
                            .setSmallIcon(R.mipmap.dokoncana_ikona_zajec)
                            .setContentTitle("Event!")
                            .setContentText(events.eventString)
                            .setDeleteIntent(processEventsOnDelete)
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT);

                    notificationManager.notify(events.id, alertEvent2.build());


                } else {
                    Intent yesProcessEvent = new Intent(this, processEvents.class);
                    yesProcessEvent.putExtra("processEventUUID", events.eventUUID);
                    yesProcessEvent.putExtra("happened", true);
                    PendingIntent yesProcessPending = PendingIntent.getService(this, randomCode, yesProcessEvent, 0);
                    NotificationCompat.Builder alertEvent = new NotificationCompat.Builder(this, "NotifyEvent")
                            .setSmallIcon(R.mipmap.dokoncana_ikona_zajec)
                            .setContentTitle("Event!")
                            .setContentText(events.eventString)
                            .setOngoing(true)
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                            .addAction(0, "Yes", yesProcessPending)
                            .addAction(0, "No", noAction);
                    notificationManager.notify(events.id, alertEvent.build());
                }
                events.update();
            }
                }).execute();


    }
}
