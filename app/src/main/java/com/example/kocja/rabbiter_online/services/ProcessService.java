package com.example.kocja.rabbiter_online.services;

import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.PowerManager;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.kocja.rabbiter_online.R;
import com.example.kocja.rabbiter_online.databases.Events;
import com.example.kocja.rabbiter_online.managers.GsonManager;
import com.example.kocja.rabbiter_online.managers.HttpManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ProcessService extends IntentService {
    public static PowerManager.WakeLock wakeLock;
    public ProcessService(){
        super("this is processEvents");
    }

    @Override
    public void onCreate() {
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.O){
            NotificationChannel channel =new NotificationChannel("processChannel","Proccessing", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            manager.createNotificationChannel(channel);
        }
        NotificationCompat.Builder processNotification = new NotificationCompat.Builder(this,"processChannel")
                .setContentTitle("Processing")
                .setContentText("this is processing")
                .setSmallIcon(R.mipmap.dokoncana_ikona_zajec_round_lowres);
        startForeground(5,processNotification.build());
        super.onCreate();
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        boolean eventHappened = intent.getBooleanExtra("eventHappened",false);
        String eventUUID = intent.getStringExtra("eventUUID");

        HttpManager.INSTANCE.postRequest("seekSingleEntry", eventUUID, (response, bytes) -> {
            Events event = GsonManager.INSTANCE.getGson().fromJson(response,Events.class);

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.UK);

            if(event.getTimesNotified() > 3){
                Date todaysDate = new Date();
                String todayDateString = dateFormat.format(todaysDate) + ":";

                event = setEventString(eventHappened,event,todayDateString);
            }
            else{
                long day = 1000L*60*60*24;
                event.setTimesNotified(event.getTimesNotified()+1);
                event.setDateOfEventMilis(event.getDateOfEventMilis()+day);
                event.setDateOfEvent(dateFormat.format(new Date(event.getDateOfEventMilis())));

                NotifyUser.schedule(getApplicationContext(),event.getDateOfEventMilis(),event.getEventUUID().toString());
            }

            HttpManager.INSTANCE.postRequest("updateEvent", GsonManager.INSTANCE.getGson().toJson(event), (response1, bytes1) -> {
                wakeLock.release();
            });
        });
    }
    public Events setEventString(boolean eventHappened,Events event,String formattedDate){
        if(eventHappened){
            event.setNotificationState(Events.EVENT_SUCCESSFUL);
            switch(event.getTypeOfEvent()){
                case Events.BIRTH_EVENT:
                    event.setEventString(formattedDate + event.getName() + "gave birth");
                    break;
                case Events.READY_MATING_EVENT:
                    event.setEventString(formattedDate + event.getName() + "was ready for mating");
                    break;
                case Events.MOVE_GROUP_EVENT:
                    event.setEventString(formattedDate + " the group "+event.getName()+" was moved into another cage");
                    break;
                case Events.SLAUGHTER_EVENT:
                    event.setEventString(formattedDate + " the group "+ event.getName()+" was slaughtered");
                    break;
            }
        }
        else{
            event.setNotificationState(Events.EVENT_FAILED);
            switch(event.getTypeOfEvent()){
                case Events.BIRTH_EVENT:
                    event.setEventString(formattedDate + event.getName() + "didn't give birth");
                    break;
                case Events.READY_MATING_EVENT:
                    event.setEventString(formattedDate + event.getName() + "was ready for mating");
                    break;
                case Events.MOVE_GROUP_EVENT:
                    event.setEventString(formattedDate + "the group "+event.getName()+" wasn't moved into another cage");
                    break;
                case Events.SLAUGHTER_EVENT:
                    event.setEventString(formattedDate + "the group " + event.getName()+" wasn't slaughtered");
                    break;

            }
        }
        return event;
    }
}
