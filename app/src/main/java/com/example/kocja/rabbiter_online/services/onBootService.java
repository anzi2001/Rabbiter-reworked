package com.example.kocja.rabbiter_online.services;

import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.kocja.rabbiter_online.managers.GsonManager;
import com.example.kocja.rabbiter_online.managers.HttpManager;
import com.example.kocja.rabbiter_online.R;
import com.example.kocja.rabbiter_online.databases.Events;

/**
 * Created by kocja on 27/02/2018.
 */

public class onBootService extends IntentService {
    public onBootService(){
        super("This is onBootService");
    }

    public void onCreate(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel chanel = new NotificationChannel("checkBoot","Boot", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(chanel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,"checkBoot")
                .setSmallIcon(R.mipmap.dokoncana_ikona_zajec_round_lowres)
                .setContentTitle("Configuring alarms")
                .setContentText("Configuring");
        this.startForeground(1,builder.build());

        super.onCreate();
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        HttpManager.INSTANCE.getRequest("seekEventsNotAlerted", response -> {
            if(response != null){
                //Random randomGen = new Random();
                Events[] events = GsonManager.INSTANCE.getGson().fromJson(response,Events[].class);
                for(Events event : events){
                    NotifyUser.schedule(this,event.getDateOfEventMilis(),event.getEventUUID().toString());

                }
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
