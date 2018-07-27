package com.example.kocja.rabbiter_online.broadcastrecievers;

import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

import com.example.kocja.rabbiter_online.services.NotifyUser;


public class AlarmReciever extends ServiceStarterBroadCReciever {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent notificationIntent = new Intent(context, NotifyUser.class);
        notificationIntent.putExtra("eventUUID",intent.getStringExtra("eventUUID"));
        startService(context,notificationIntent);
        if(NotifyUser.wakeLock == null){
            PowerManager manager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            NotifyUser.wakeLock = manager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"myWakeLock");

        }
        if(!NotifyUser.wakeLock.isHeld()){
            NotifyUser.wakeLock.acquire(1000L*60*5);
        }

    }


}
