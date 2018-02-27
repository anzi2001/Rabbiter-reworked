package com.example.kocja.rabbiter_reworked.BroadcastRecievers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.kocja.rabbiter_reworked.services.onBootService;

/**
 * Created by kocja on 27/02/2018.
 */

public class onBootReciever extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent startOnBootService = new Intent(context,onBootService.class);
        context.startService(startOnBootService);
    }
}
