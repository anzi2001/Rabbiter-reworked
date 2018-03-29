package com.example.kocja.rabbiter_reworked.services;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.kocja.rabbiter_reworked.databases.Events;
import com.example.kocja.rabbiter_reworked.databases.Events_Table;
import com.raizlabs.android.dbflow.sql.language.SQLite;

/**
 * Created by kocja on 28/02/2018.
 */

public class alertIfNotAlertedService extends IntentService {
    public alertIfNotAlertedService(){
        super("This is alertIfNotAlertedService");
    }
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        SQLite.select()
                .from(Events.class)
                .where(Events_Table.notificationState.eq(Events.NOT_YET_ALERTED))
                .async()
                .queryListResultCallback((transaction, tResult) -> {
                    Intent startNotificationIntent = new Intent(this,AlertEventService.class);
                    AlarmManager manager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
                    for(Events event : tResult){
                            Log.v("Oops","This guy was not started");
                            startNotificationIntent.putExtra("eventUUID",event.eventUUID);
                            PendingIntent startNotification = PendingIntent.getService(this,event.id,startNotificationIntent,PendingIntent.FLAG_CANCEL_CURRENT);
                            manager.set(AlarmManager.RTC_WAKEUP,event.dateOfEvent.getTime(),startNotification);
                    }
                }).execute();
    }
}
