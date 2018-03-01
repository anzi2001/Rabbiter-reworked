package com.example.kocja.rabbiter_reworked.services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;

import com.example.kocja.rabbiter_reworked.databases.Events;
import com.example.kocja.rabbiter_reworked.databases.Events_Table;
import com.raizlabs.android.dbflow.sql.language.SQLite;

import java.text.SimpleDateFormat;
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
        SQLite.select()
                .from(Events.class)
                .where(Events_Table.eventUUID.eq(processEventUUID))
                .async()
                .querySingleResultCallback((transaction, events) -> {
                    NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    manager.cancel(events.id);
                    if(happened) {
                        events.yesClicked = true;
                        if (events.typeOfEvent == 0) {
                            events.eventString = dateFormatter.format(events.dateOfEvent) + ": " + events.name + " gave birth";
                        } else if (events.typeOfEvent == 2) {
                            events.eventString = dateFormatter.format(events.dateOfEvent) + ": " + events.name + " was moved into another cage";
                        } else if (events.typeOfEvent == 3) {
                            events.eventString = dateFormatter.format(events.dateOfEvent) + ": The group " + events.name + "was slaughtered";
                        }
                    }
                    else{
                        //since we process a no event we can set the yesClicked to true, so it counts
                        //as notified so it doesn't annoy the user
                        events.yesClicked = true;
                        if (events.typeOfEvent == 0) {
                            events.eventString = dateFormatter.format(events.dateOfEvent) + ": " + events.name + " did not give birth";
                        } else if (events.typeOfEvent == 2) {
                            events.eventString = dateFormatter.format(events.dateOfEvent) + ": " + events.name + " wasn't moved into another cage";
                        } else if (events.typeOfEvent == 3) {
                            events.eventString = dateFormatter.format(events.dateOfEvent) + ": The group " + events.name + "wasn't slaughtered";
                        }
                    }

                    events.update();
                }).execute();
    }
}
