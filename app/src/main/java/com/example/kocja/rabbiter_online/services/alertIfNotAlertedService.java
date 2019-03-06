package com.example.kocja.rabbiter_online.services;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.example.kocja.rabbiter_online.managers.GsonManager;
import com.example.kocja.rabbiter_online.managers.HttpManager;
import com.example.kocja.rabbiter_online.databases.Events;

import androidx.annotation.Nullable;

/**
 * Created by kocja on 28/02/2018.
 */

public class alertIfNotAlertedService extends IntentService {
    public alertIfNotAlertedService(){
        super("This is alertIfNotAlertedService");
    }
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        HttpManager.INSTANCE.getRequest("seekNotAlertedEvents", response -> {
            for(Events event : GsonManager.INSTANCE.getGson().fromJson(response,Events[].class)){
                Log.v("Oops","This guy was not started");
                NotifyUser.schedule(this,event.getDateOfEventMilis(),event.getEventUUID().toString());
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
