package com.example.kocja.rabbiter_online.broadcastrecievers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class ServiceStarterBroadCReciever extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

    }
    public static void startService(Context c,Intent notifyService){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            c.startForegroundService(notifyService);
        }
        else{
            c.startService(notifyService);
        }
    }
}
