package com.example.kocja.rabbiter_reworked.services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;

import com.example.kocja.rabbiter_reworked.GsonManager;
import com.example.kocja.rabbiter_reworked.HttpManager;
import com.example.kocja.rabbiter_reworked.databases.Events;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;



/**
 * Created by kocja on 27/02/2018.
 */

public class processEvents extends IntentService {
    public processEvents(){
        super("This is processEvents");
    }
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        UUID processEventUUID = (UUID) intent.getSerializableExtra("processEventUUID");
        boolean happened = intent.getBooleanExtra("happened",false);
        SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy", Locale.GERMANY);
        HttpManager.postRequest("seekSingle", GsonManager.getGson().toJson(processEventUUID), (response,bytes) -> {
            Events events = GsonManager.getGson().fromJson(response,Events.class);
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if(events.typeOfEvent != 1){
                manager.cancel(events.id);
            }
            if(happened) {
                Date currentDate = new Date();
                events.notificationState = Events.EVENT_SUCCESSFUL;
                switch (events.typeOfEvent) {
                    case 0:
                        events.eventString = dateFormatter.format(currentDate) + ": " + events.name + " gave birth";
                        break;
                    case 2:
                        events.eventString = dateFormatter.format(currentDate) + ": " + events.name + " was moved into another cage";
                        break;
                    case 3:
                        events.eventString = dateFormatter.format(currentDate) + ": The group " + events.name + "was slaughtered";
                        break;
                }
            }
            else{
                //since we process a no event we can set the notificationState to EVENT_FAILED, so it counts
                //as notified and doesn't annoy the user
                events.notificationState = Events.EVENT_FAILED;
                switch (events.typeOfEvent) {
                    case 0:
                        events.eventString = dateFormatter.format(events.dateOfEvent) + ": " + events.name + " did not give birth";
                        break;
                    case 2:
                        events.eventString = dateFormatter.format(events.dateOfEvent) + ": " + events.name + " wasn't moved into another cage";
                        break;
                    case 3:
                        events.eventString = dateFormatter.format(events.dateOfEvent) + ": The group " + events.name + "wasn't slaughtered";
                        break;
                }
            }

            HttpManager.postRequest("updateEvent", GsonManager.getGson().toJson(events), (response1,bytes1) -> { });
        });
        /*
        SQLite.select()
                .from(Events.class)
                .where(Events_Table.eventUUID.eq(processEventUUID))
                .async()
                .querySingleResultCallback((transaction, events) -> {

                }).execute();
        */
    }
}
