package com.example.kocja.rabbiter_online.services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;

import com.example.kocja.rabbiter_online.managers.GsonManager;
import com.example.kocja.rabbiter_online.managers.HttpManager;
import com.example.kocja.rabbiter_online.databases.Events;

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
        HttpManager.postRequest("seekSingleEntry", GsonManager.getGson().toJson(processEventUUID), (response,bytes) -> {
            Events events = GsonManager.getGson().fromJson(response,Events.class);

            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if(events.getTypeOfEvent() != 1){
                manager.cancel(events.getId());
            }
            if(happened) {
                Date currentDate = new Date();
                events.setNotificationState(Events.EVENT_SUCCESSFUL);
                switch (events.getTypeOfEvent()) {
                    case 0:
                        events.setEventString(dateFormatter.format(currentDate) + ": " + events.getName() + " gave birth");
                        break;
                    case 2:
                        events.setEventString(dateFormatter.format(currentDate) + ": " + events.getName() + " was moved into another cage");
                        break;
                    case 3:
                        events.setEventString(dateFormatter.format(currentDate) + ": The group " + events.getName() + "was slaughtered");
                        break;
                }
            }
            else{
                //since we process a no event we can set the notificationState to EVENT_FAILED, so it counts
                //as notified and doesn't annoy the user
                events.setNotificationState(Events.EVENT_FAILED);
                switch (events.getTypeOfEvent()) {
                    case 0:
                        events.setEventString(dateFormatter.format(events.getDateOfEvent()) + ": " + events.getName() + " did not give birth");
                        break;
                    case 2:
                        events.setEventString(dateFormatter.format(events.getDateOfEvent()) + ": " + events.getName() + " wasn't moved into another cage");
                        break;
                    case 3:
                        events.setEventString(dateFormatter.format(events.getDateOfEvent()) + ": The group " + events.getName() + "wasn't slaughtered");
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
