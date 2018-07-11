package com.example.kocja.rabbiter_online.services;

import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import com.example.kocja.rabbiter_online.managers.GsonManager;
import com.example.kocja.rabbiter_online.managers.HttpManager;
import com.example.kocja.rabbiter_online.R;
import com.example.kocja.rabbiter_online.activities.addEntryActivity;
import com.example.kocja.rabbiter_online.databases.Events;

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

        HttpManager.postRequest("findNotAlertedEvent", GsonManager.getGson().toJson(eventUUID), (response,bytes) -> {
            if(response != null) {
                Events events = GsonManager.getGson().fromJson(response,Events[].class)[0];
                int randomCode = new Random().nextInt();
                //events.id = randomCode;
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                    NotificationChannel chanel = new NotificationChannel("NotifyEvent","Event",NotificationManager.IMPORTANCE_DEFAULT);
                    notificationManager.createNotificationChannel(chanel);
                }

                NotificationCompat.Builder alertEvent  = new NotificationCompat.Builder(this,"NotifyEvent");
                alertEvent.setSmallIcon(R.mipmap.dokoncana_ikona_zajec_round_lowres);
                alertEvent.setContentTitle("Event!");
                alertEvent.setContentText(events.getEventString());

                if (events.getTypeOfEvent() == Events.BIRTH_EVENT) {
                    Intent yesIntent = new Intent(this, addEntryActivity.class)
                            .putExtra("eventUUID", eventUUID)
                            .putExtra("getMode", ADD_BIRTH_FROM_SERVICE)
                            .putExtra("happened", true);
                    PendingIntent yesAction = PendingIntent.getActivity(this, randomCode, yesIntent, 0);

                    alertEvent.setOngoing(true);
                    alertEvent.setPriority(NotificationCompat.PRIORITY_DEFAULT);
                    alertEvent.addAction(0, "Yes", yesAction);
                    alertEvent.addAction(0, "No", noAction);
                    //notificationManager.notify(events.id, alertEvent.build());
                }
                else if (events.getTypeOfEvent() == Events.READY_MATING_EVENT) {
                    Intent processEvents = new Intent(this, processEvents.class)
                            .putExtra("happened", true)
                            .putExtra("processEventUUID", eventUUID);
                    PendingIntent processEventsOnDelete = PendingIntent.getService(this, randomCode, processEvents, 0);

                    alertEvent.setDeleteIntent(processEventsOnDelete);
                    alertEvent.setPriority(NotificationCompat.PRIORITY_DEFAULT);
                    //notificationManager.notify(events.id, alertEvent.build());
                }
                else {
                    Intent yesProcessEvent = new Intent(this, processEvents.class)
                            .putExtra("processEventUUID", events.getEventUUID())
                            .putExtra("happened", true);
                    PendingIntent yesProcessPending = PendingIntent.getService(this, randomCode, yesProcessEvent, 0);

                    alertEvent.setOngoing(true);
                    alertEvent.setPriority(NotificationCompat.PRIORITY_DEFAULT);
                    alertEvent.addAction(0, "Yes", yesProcessPending);
                    alertEvent.addAction(0, "No", noAction);
                }
                notificationManager.notify(events.getId(), alertEvent.build());
                HttpManager.postRequest("updateEvents", GsonManager.getGson().toJson(events), (response1, bytes1) -> {

                });
            }
        });
        /*
        SQLite.select()
                .from(Events.class)
                .where(Events_Table.eventUUID.eq(eventUUID))
                .and(Events_Table.notificationState.eq(Events.NOT_YET_ALERTED))
                .async()
                .querySingleResultCallback((transaction, events) -> {

                }).execute();
        */

    }
}
