package com.example.kocja.rabbiter_reworked.services;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.example.kocja.rabbiter_reworked.broadcastrecievers.NotifReciever;
import com.example.kocja.rabbiter_reworked.databases.Events;
import com.example.kocja.rabbiter_reworked.databases.Events_Table;
import com.raizlabs.android.dbflow.sql.language.SQLite;

import java.util.Date;
import java.util.UUID;

/**
 * If the user clicked that the event did not happen, try again in a day.
 */

public class askNotifAgain extends Service {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
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

                        Intent alertIntent = new Intent(this, NotifReciever.class);
                        alertIntent.putExtra("eventUUID",events.eventUUID);

                        PendingIntent alertPending = PendingIntent.getBroadcast(this, events.id, alertIntent, 0);
                        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                        manager.set(AlarmManager.RTC_WAKEUP, events.dateOfEvent.getTime(), alertPending);
                    }
                }).execute();
        return super.onStartCommand(intent, flags, startId);
    }

}
