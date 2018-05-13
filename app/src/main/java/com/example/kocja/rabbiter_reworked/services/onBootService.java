package com.example.kocja.rabbiter_reworked.services;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import com.example.kocja.rabbiter_reworked.R;
import com.example.kocja.rabbiter_reworked.broadcastrecievers.NotifReciever;
import com.example.kocja.rabbiter_reworked.databases.Events;
import com.example.kocja.rabbiter_reworked.databases.Events_Table;
import com.raizlabs.android.dbflow.sql.language.SQLite;

/**
 * Created by kocja on 27/02/2018.
 */

public class onBootService extends IntentService {
    public onBootService(){
        super("This is onBootService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel chanel = new NotificationChannel("checkBoot","Boot", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(chanel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,"checkBoot")
                .setSmallIcon(R.mipmap.dokoncana_ikona_zajec_round_lowres)
                .setContentTitle("Configuring alarms")
                .setContentText("Configuring")
                .setTicker("tick");

        this.startForeground(1,builder.build());


        SQLite.select()
                .from(Events.class)
                .where(Events_Table.notificationState.eq(Events.NOT_YET_ALERTED))
                .async()
                .queryListResultCallback((transaction, tResult) -> {
                    AlarmManager alarmManager =(AlarmManager) getSystemService(Context.ALARM_SERVICE);
                    if(tResult != null){
                        Intent setNotification = new Intent(this,NotifReciever.class);
                        //Random randomGen = new Random();
                        for(Events event : tResult){
                            setNotification.putExtra("eventUUID",event.eventUUID);
                            PendingIntent setNotifIntent = PendingIntent.getBroadcast(this, event.id,setNotification,0);
                            alarmManager.set(AlarmManager.RTC_WAKEUP,event.dateOfEvent.getTime(),setNotifIntent);

                        }
                    }
                }).execute();

    }
}
