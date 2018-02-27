package com.example.kocja.rabbiter_reworked.services;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;

import com.example.kocja.rabbiter_reworked.databases.Events;
import com.example.kocja.rabbiter_reworked.databases.Events_Table;
import com.raizlabs.android.dbflow.sql.language.SQLite;

import java.util.Random;

/**
 * Created by kocja on 27/02/2018.
 */

public class onBootService extends IntentService {
    public onBootService(){
        super("This is onBootService");
    }
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        SQLite.select()
                .from(Events.class)
                .where(Events_Table.timesNotified.lessThan(3))
                .and(Events_Table.yesClicked.eq(false))
                .async()
                .queryListResultCallback((transaction, tResult) -> {
                    AlarmManager notifyManager =(AlarmManager) getSystemService(Context.ALARM_SERVICE);
                    if(tResult != null){
                        for(Events event : tResult){
                            Intent setNotifcation = new Intent(this,AlertEventService.class);
                            setNotifcation.putExtra("eventUUID",event.eventUUID);
                            setNotifcation.putExtra("firstParent",event.eventUUID);
                            PendingIntent setNotifIntent = PendingIntent.getBroadcast(this, new Random().nextInt(),setNotifcation,0);
                            notifyManager.set(AlarmManager.RTC_WAKEUP,event.dateOfEvent.getTime(),setNotifIntent);

                        }
                    }
                }).execute();
    }
}
