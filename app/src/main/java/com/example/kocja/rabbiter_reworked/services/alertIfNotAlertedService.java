package com.example.kocja.rabbiter_reworked.services;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.kocja.rabbiter_reworked.GsonManager;
import com.example.kocja.rabbiter_reworked.SocketIOManager;
import com.example.kocja.rabbiter_reworked.broadcastrecievers.NotifReciever;
import com.example.kocja.rabbiter_reworked.databases.Events;
import com.google.gson.JsonObject;

/**
 * Created by kocja on 28/02/2018.
 */

public class alertIfNotAlertedService extends IntentService {
    public alertIfNotAlertedService(){
        super("This is alertIfNotAlertedService");
    }
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        SocketIOManager.getSocket().emit("seekNotAlertedEventsReq");
        SocketIOManager.getSocket().on("seekNotAlertedEventsRes", args -> {
            Intent startNotificationIntent = new Intent(this, NotifReciever.class);
            AlarmManager manager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
            for(Object eventObj : args){
                Events event = GsonManager.getGson().fromJson((JsonObject)eventObj,Events.class);
                Log.v("Oops","This guy was not started");
                startNotificationIntent.putExtra("eventUUID",event.eventUUID);
                PendingIntent startNotification = PendingIntent.getBroadcast(this,event.id,startNotificationIntent,PendingIntent.FLAG_UPDATE_CURRENT);
                //manager.set(AlarmManager.RTC_WAKEUP,event.dateOfEvent.getTime(),startNotification);
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
