package com.example.kocja.rabbiter_online.services;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.example.kocja.rabbiter_online.GsonManager;
import com.example.kocja.rabbiter_online.HttpManager;
import com.example.kocja.rabbiter_online.broadcastrecievers.NotifReciever;
import com.example.kocja.rabbiter_online.databases.Events;

import java.util.UUID;


/**
 * If the user clicked that the event did not happen, try again in a day.
 */

public class askNotifAgain extends Service {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        HttpManager.postRequest("seekNotifUUID",intent.getSerializableExtra("eventUUID") .toString(), (response,bytes) -> {
            NotificationManager notifManager =(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            Events events = GsonManager.getGson().fromJson(response,Events[].class)[0];
            notifManager.cancel(events.id);

            if(events.timesNotified >2){
                Intent processNoEvent = new Intent(askNotifAgain.this,processEvents.class);
                processNoEvent.putExtra("processEventUUID",events.eventUUID);
                processNoEvent.putExtra("happened",false);
                startService(processNoEvent);
            }
            else {

                events.timesNotified++;
                //events.dateOfEvent = new Date(events.dateOfEvent.getTime() + (1000L *60*60));
                HttpManager.postRequest("updateEvents", GsonManager.getGson().toJson(events), (response1,bytes1) -> { });

                Intent alertIntent = new Intent(askNotifAgain.this, NotifReciever.class);
                alertIntent.putExtra("eventUUID",events.eventUUID);
                PendingIntent alertPending = PendingIntent.getBroadcast(askNotifAgain.this, events.id, alertIntent, 0);
                AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                //manager.set(AlarmManager.RTC_WAKEUP, events.dateOfEvent.getTime(), alertPending);
            }
        });
        /*
        SQLite.select()
                .from(Events.class)
                .where(Events_Table.eventUUID.eq())
                .async()
                .querySingleResultCallback((transaction, events) -> {

                }).execute();
         */
        return super.onStartCommand(intent, flags, startId);
    }

}
