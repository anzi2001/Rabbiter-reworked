package com.example.kocja.rabbiter_reworked.broadcastrecievers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import com.example.kocja.rabbiter_reworked.services.onBootService;


/**
 * Created by kocja on 27/02/2018.
 */

public class onBootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent startOnBootService = new Intent(context,onBootService.class);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            context.startForegroundService(startOnBootService);
        }
        else{
            context.startService(startOnBootService);
        }

    }
}
