package com.example.kocja.rabbiter_reworked.services;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;

import com.example.kocja.rabbiter_reworked.databases.Events;
import com.example.kocja.rabbiter_reworked.databases.Events_Table;
import com.raizlabs.android.dbflow.sql.language.SQLite;

import java.util.Date;
import java.util.UUID;

/**
 * If the user clicked that the event did not happen, try again in a day.
 */

public class askNotifAgain extends IntentService {
    public askNotifAgain(){
        super("this is askNotifAgain");
    }
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        SQLite.select()
                .from(Events.class)
                .where(Events_Table.eventUUID.eq((UUID)intent.getSerializableExtra("eventUUID")))
                .async()
                .querySingleResultCallback((transaction, events) -> {
                    NotificationManager notifManager =(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    notifManager.cancel(events.id);

                    if(events.timesNotified >2){
                        Intent processNoEvent = new Intent(this,processEvents.class);
                        processNoEvent.putExtra("processEventUUID",events.eventUUID);
                        processNoEvent.putExtra("happened",false);
                        startService(processNoEvent);
                    }
                    else {

                        events.timesNotified++;
                        events.dateOfEvent = new Date(events.dateOfEvent.getTime() + (1000L *60*60));
                        events.update();

                        Intent alertIntent = new Intent(this, AlertEventService.class);
                        alertIntent.putExtra("eventUUID",events.eventUUID);

                        PendingIntent alertPending = PendingIntent.getService(this, 2, alertIntent, 0);
                        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                        manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, events.dateOfEvent.getTime(), alertPending);
                    }
                }).execute();
    }
}
