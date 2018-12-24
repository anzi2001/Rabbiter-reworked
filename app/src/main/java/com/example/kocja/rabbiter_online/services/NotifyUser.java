package com.example.kocja.rabbiter_online.services;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import com.example.kocja.rabbiter_online.R;
import com.example.kocja.rabbiter_online.broadcastrecievers.AlarmReciever;
import com.example.kocja.rabbiter_online.broadcastrecievers.ProcessReciever;
import com.example.kocja.rabbiter_online.databases.Events;
import com.example.kocja.rabbiter_online.managers.GsonManager;
import com.example.kocja.rabbiter_online.managers.HttpManager;

import java.util.Random;

public class NotifyUser extends IntentService {
    public static final int ADD_ENTRY_FROM_BIRTH = 3;
    public static PowerManager.WakeLock wakeLock;
    private NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
    public NotifyUser() {
        super("NotifyUser");
    }


    @Override
    public void onCreate() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("eventChannel", "eventChannel", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }
        NotificationCompat.Builder notification =new NotificationCompat.Builder(this,"eventChannel")
                .setContentTitle("processing Event")
                .setSmallIcon(R.mipmap.dokoncana_ikona_zajec_round_lowres)
                .setContentText("content");
        startForeground(new Random().nextInt(),notification.build());
        super.onCreate();
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        String eventUUID = intent.getStringExtra("eventUUID");


        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,"eventChannel");
        HttpManager.postRequest("findNotAlertedEvent", eventUUID, (response, bytes) -> {
            Events event = GsonManager.getGson().fromJson(response,Events[].class)[0];

            Random randomGen = new Random();
            int notificationCode = randomGen.nextInt();
            event.setId(notificationCode);

            builder.setContentTitle("Event!");
            builder.setContentText(event.getEventString());
            builder.setSmallIcon(R.mipmap.dokoncana_ikona_zajec_round_lowres);

            if(event.getTypeOfEvent() != Events.READY_MATING_EVENT){
                builder.setOngoing(true);
                Intent processYesInput = new Intent(getApplicationContext(),ProcessReciever.class);
                processYesInput.putExtra("eventUUID",event.getEventUUID());
                processYesInput.putExtra("eventHappened",true);
                builder.addAction(R.mipmap.dokoncana_ikona_zajec_round_lowres,"Yes",PendingIntent.getBroadcast(this,randomGen.nextInt(),processYesInput,0));

                Intent processNoInput = new Intent(getApplicationContext(),ProcessReciever.class);
                processNoInput.putExtra("eventUUID",event.getEventUUID());
                processNoInput.putExtra("eventHappened",false);
                builder.addAction(R.mipmap.dokoncana_ikona_zajec_round_lowres,"No",PendingIntent.getBroadcast(this,randomGen.nextInt(),processNoInput,0));
            }
            else{
                Intent onDeleteIntent = new Intent(getApplicationContext(),ProcessReciever.class);
                builder.setDeleteIntent(PendingIntent.getBroadcast(this,randomGen.nextInt(),onDeleteIntent,0));
            }
            notificationManager.notify(notificationCode,builder.build());

            if(wakeLock.isHeld()){
                wakeLock.release();

            }
            wakeLock = null;

        });
    }
    public static void schedule(Context context,long time,String uuid){
        Intent startSchedule = new Intent(context,AlarmReciever.class);
        startSchedule.putExtra("eventUUID",uuid);
        PendingIntent startSchedulePending = PendingIntent.getBroadcast(context,new Random().nextInt(),startSchedule,0);

        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        manager.set(AlarmManager.RTC_WAKEUP,time,startSchedulePending);


    }

}
