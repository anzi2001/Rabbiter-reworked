package com.example.kocja.rabbiter_online.broadcastrecievers;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.example.kocja.rabbiter_online.managers.GsonManager;
import com.example.kocja.rabbiter_online.managers.HttpManager;
import com.example.kocja.rabbiter_online.R;
import com.example.kocja.rabbiter_online.activities.addEntryActivity;
import com.example.kocja.rabbiter_online.databases.Events;
import com.example.kocja.rabbiter_online.services.AlertEventService;
import com.example.kocja.rabbiter_online.services.askNotifAgain;
import com.example.kocja.rabbiter_online.services.processEvents;

import java.util.Random;
import java.util.UUID;



public class NotifReciever extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        UUID eventUUID =(UUID) intent.getSerializableExtra("eventUUID");


        Intent noIntent = new Intent(context,askNotifAgain.class);
        noIntent.putExtra("eventUUID",eventUUID);
        PendingIntent noAction = PendingIntent.getService(context, new Random().nextInt(),noIntent,0);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        HttpManager.postRequest("NotifBroadcast", GsonManager.getGson().toJson(eventUUID), (response,bytes) -> {
            Events events = GsonManager.getGson().fromJson(response,Events.class);
            if(events != null) {
                int randomCode = new Random().nextInt();
                //events.id = randomCode;
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                    NotificationChannel chanel = new NotificationChannel("NotifyEvent","Event",NotificationManager.IMPORTANCE_DEFAULT);
                    notificationManager.createNotificationChannel(chanel);
                }
                NotificationCompat.Builder alertEvent  = new NotificationCompat.Builder(context,"NotifyEvent");
                alertEvent.setSmallIcon(R.mipmap.dokoncana_ikona_zajec_round_lowres);
                alertEvent.setContentTitle("Event!");
                alertEvent.setContentText(events.getEventString());

                if (events.getTypeOfEvent() == 0) {
                    Intent yesIntent = new Intent(context, addEntryActivity.class);
                    yesIntent.putExtra("eventUUID", eventUUID);
                    yesIntent.putExtra("getMode", AlertEventService.ADD_BIRTH_FROM_SERVICE);
                    yesIntent.putExtra("happened", true);
                    PendingIntent yesAction = PendingIntent.getActivity(context, randomCode, yesIntent, 0);

                    alertEvent.setOngoing(true);
                    alertEvent.setPriority(NotificationCompat.PRIORITY_DEFAULT);
                    alertEvent.addAction(0, "Yes", yesAction);
                    alertEvent.addAction(0, "No", noAction);
                    //notificationManager.notify(events.id, alertEvent.build());

                } else if (events.getTypeOfEvent() == 1) {
                    Intent processEvents = new Intent(context, com.example.kocja.rabbiter_online.services.processEvents.class);
                    processEvents.putExtra("happened", true);
                    processEvents.putExtra("processEventUUID", eventUUID);
                    PendingIntent processEventsOnDelete = PendingIntent.getService(context, randomCode, processEvents, 0);

                    alertEvent.setDeleteIntent(processEventsOnDelete);
                    alertEvent.setPriority(NotificationCompat.PRIORITY_DEFAULT);
                    //notificationManager.notify(events.id, alertEvent.build());


                } else {
                    Intent yesProcessEvent = new Intent(context, processEvents.class);
                    yesProcessEvent.putExtra("processEventUUID", events.getEventUUID());
                    yesProcessEvent.putExtra("happened", true);
                    PendingIntent yesProcessPending = PendingIntent.getService(context, randomCode, yesProcessEvent, 0);

                    alertEvent.setOngoing(true);
                    alertEvent.setPriority(NotificationCompat.PRIORITY_DEFAULT);
                    alertEvent.addAction(0, "Yes", yesProcessPending);
                    alertEvent.addAction(0, "No", noAction);
                }
                notificationManager.notify(events.getId(), alertEvent.build());
                HttpManager.postRequest("updateEvents", GsonManager.getGson().toJson(events), (response1,bytes1) -> { });
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
