package com.example.kocja.rabbiter_reworked.services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

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
    private static final int START_YES_ACTION =4;
    private static final int START_NO_ACTION = 5;
    public static final int ADD_BIRTH_FROM_SERVICE =3;
    public AlertEventService(){
        super("This is a AlertEventService");
    }
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        UUID eventUUID =(UUID) intent.getSerializableExtra("eventUUID");

        Intent noIntent = new Intent(this,askNotifAgain.class);
        noIntent.putExtra("eventUUID",eventUUID);
        PendingIntent noAction = PendingIntent.getBroadcast(this,START_NO_ACTION,noIntent,0);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        SQLite.select()
                .from(Events.class)
                .where(Events_Table.eventUUID.eq(eventUUID))
                .and(Events_Table.timesNotified.lessThan(3))
                .and(Events_Table.yesClicked.eq(false))
                .async()
                .querySingleResultCallback((transaction, events) -> {
                    if(events != null && events.secondParent != null){

                        Intent yesIntent = new Intent(this,addEntryActivity.class);
                        yesIntent.putExtra("eventUUID",eventUUID);
                        yesIntent.putExtra("getMode", ADD_BIRTH_FROM_SERVICE);
                        yesIntent.putExtra("happened",true);
                        PendingIntent yesAction = PendingIntent.getBroadcast(this,START_YES_ACTION,yesIntent,0);

                        NotificationCompat.Builder alertEvent = new NotificationCompat.Builder(this,"id")
                                .setContentTitle("Event!")
                                .setContentText(events.eventString)
                                .setOngoing(true)
                                .addAction(0,"Yes",yesAction)
                                .addAction(0,"No",noAction);
                        events.id = new Random().nextInt();
                        notificationManager.notify(events.id,alertEvent.build());
                    }
                    else if(events != null && events.typeOfEvent == 1){

                        NotificationCompat.Builder alertEvent = new NotificationCompat.Builder(this,"id")
                                .setContentTitle("Event!")
                                .setContentText(events.eventString);
                        events.id = new Random().nextInt();

                        Intent processEvents = new Intent(this, com.example.kocja.rabbiter_reworked.services.processEvents.class);
                        processEvents.putExtra("processEventUUID",eventUUID);
                        startService(processEvents);

                        notificationManager.notify(events.id,alertEvent.build());
                    }
                    else if(events != null){

                        Intent yesProcessEvent = new Intent(this,processEvents.class);
                        yesProcessEvent.putExtra("processEventUUID",events.eventUUID);
                        yesProcessEvent.putExtra("happened",true);
                        PendingIntent yesProcessPending = PendingIntent.getBroadcast(this,START_YES_ACTION,yesProcessEvent,0);

                        NotificationCompat.Builder alertEvent = new NotificationCompat.Builder(this,"id")
                                .setContentTitle("Event!")
                                .setContentText(events.eventString)
                                .setOngoing(true)
                                .addAction(0,"Yes",yesProcessPending)
                                .addAction(0,"No",noAction);
                        events.id = new Random().nextInt();
                        notificationManager.notify(events.id,alertEvent.build());
                    }
                    events.update();
                }).execute();


    }
}
