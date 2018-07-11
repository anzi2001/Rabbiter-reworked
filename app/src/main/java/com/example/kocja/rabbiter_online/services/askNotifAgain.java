package com.example.kocja.rabbiter_online.services;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.example.kocja.rabbiter_online.managers.GsonManager;
import com.example.kocja.rabbiter_online.managers.HttpManager;
import com.example.kocja.rabbiter_online.broadcastrecievers.NotifReciever;
import com.example.kocja.rabbiter_online.databases.Events;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


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
        HttpManager.postRequest("seekNotifUUID",intent.getSerializableExtra("eventUUID").toString(), (response,bytes) -> {
            NotificationManager notifManager =(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            Events events = GsonManager.getGson().fromJson(response,Events[].class)[0];
            notifManager.cancel(events.getId());

            //event has happened 3 times, and user clicked no for the 4th time, therefore has not happened
            if(events.getTimesNotified() > 2){
                Intent processNoEvent = new Intent(askNotifAgain.this,processEvents.class);
                processNoEvent.putExtra("processEventUUID",events.getEventUUID());
                processNoEvent.putExtra("happened",false);
                startService(processNoEvent);
            }
            //event has not happened 3 times, but user clicked no, try again in a day
            else {
                long dayMilis = 1000 * 60 *60 *24;
                events.setTimesNotified(events.getTimesNotified() + 1);
                events.setDateOfEventMilis(events.getDateOfEventMilis()+dayMilis);
                events.setDateOfEvent(new SimpleDateFormat("dd/MM/yyyy", Locale.UK).format(new Date(events.getDateOfEventMilis())));
                HttpManager.postRequest("updateEvents", GsonManager.getGson().toJson(events), (response1,bytes1) -> { });

                Intent alertIntent = new Intent(askNotifAgain.this, AlertEventService.class);
                alertIntent.putExtra("eventUUID",events.getEventUUID());
                PendingIntent alertPending = PendingIntent.getBroadcast(askNotifAgain.this, events.getId(), alertIntent, 0);
                AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                manager.set(AlarmManager.RTC_WAKEUP, events.getDateOfEventMilis(), alertPending);
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
