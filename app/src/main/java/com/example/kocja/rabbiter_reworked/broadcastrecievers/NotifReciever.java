package com.example.kocja.rabbiter_reworked.broadcastrecievers;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.example.kocja.rabbiter_reworked.R;
import com.example.kocja.rabbiter_reworked.activities.addEntryActivity;
import com.example.kocja.rabbiter_reworked.databases.Events;
import com.example.kocja.rabbiter_reworked.databases.Events_Table;
import com.example.kocja.rabbiter_reworked.services.AlertEventService;
import com.example.kocja.rabbiter_reworked.services.askNotifAgain;
import com.example.kocja.rabbiter_reworked.services.processEvents;
import com.raizlabs.android.dbflow.sql.language.SQLite;

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
                        NotificationCompat.Builder alertEvent  = new NotificationCompat.Builder(context,"NotifyEvent");
                        alertEvent.setSmallIcon(R.mipmap.dokoncana_ikona_zajec_round_lowres);
                        alertEvent.setContentTitle("Event!");
                        alertEvent.setContentText(events.eventString);

                        if (events.typeOfEvent == 0) {
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

                        } else if (events.typeOfEvent == 1) {
                            Intent processEvents = new Intent(context, com.example.kocja.rabbiter_reworked.services.processEvents.class);
                            processEvents.putExtra("happened", true);
                            processEvents.putExtra("processEventUUID", eventUUID);
                            PendingIntent processEventsOnDelete = PendingIntent.getService(context, randomCode, processEvents, 0);

                            alertEvent.setDeleteIntent(processEventsOnDelete);
                            alertEvent.setPriority(NotificationCompat.PRIORITY_DEFAULT);
                            //notificationManager.notify(events.id, alertEvent.build());


                        } else {
                            Intent yesProcessEvent = new Intent(context, processEvents.class);
                            yesProcessEvent.putExtra("processEventUUID", events.eventUUID);
                            yesProcessEvent.putExtra("happened", true);
                            PendingIntent yesProcessPending = PendingIntent.getService(context, randomCode, yesProcessEvent, 0);

                            alertEvent.setOngoing(true);
                            alertEvent.setPriority(NotificationCompat.PRIORITY_DEFAULT);
                            alertEvent.addAction(0, "Yes", yesProcessPending);
                            alertEvent.addAction(0, "No", noAction);
                        }
                        notificationManager.notify(events.id, alertEvent.build());
                        events.update();
                    }
                }).execute();


    }
}
