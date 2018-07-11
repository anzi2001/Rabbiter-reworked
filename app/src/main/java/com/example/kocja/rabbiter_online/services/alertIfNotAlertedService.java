package com.example.kocja.rabbiter_online.services;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.kocja.rabbiter_online.managers.GsonManager;
import com.example.kocja.rabbiter_online.managers.HttpManager;
import com.example.kocja.rabbiter_online.broadcastrecievers.NotifReciever;
import com.example.kocja.rabbiter_online.databases.Events;

/**
 * Created by kocja on 28/02/2018.
 */

public class alertIfNotAlertedService extends IntentService {
    public alertIfNotAlertedService(){
        super("This is alertIfNotAlertedService");
    }
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        HttpManager.getRequest("seekNotAlertedEvents", response -> {
            Intent startNotificationIntent = new Intent(alertIfNotAlertedService.this, NotifReciever.class);
            AlarmManager manager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
            for(Events event : GsonManager.getGson().fromJson(response,Events[].class)){
                Log.v("Oops","This guy was not started");
                startNotificationIntent.putExtra("eventUUID",event.getEventUUID());
                PendingIntent startNotification = PendingIntent.getBroadcast(alertIfNotAlertedService.this,event.getId(),startNotificationIntent,PendingIntent.FLAG_UPDATE_CURRENT);
                manager.set(AlarmManager.RTC_WAKEUP,event.getDateOfEventMilis(),startNotification);
            }
        });
        /*
        SQLite.select()
                .from(Events.class)
                .where(Events_Table.notificationState.eq(Events.NOT_YET_ALERTED))
                .async()
                .queryListResultCallback((transaction, tResult) -> {
                }).execute();
          */
    }
}
