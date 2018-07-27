package com.example.kocja.rabbiter_online.broadcastrecievers;

import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

import com.example.kocja.rabbiter_online.services.ProcessService;

public class ProcessReciever extends ServiceStarterBroadCReciever {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent processEventIntent = new Intent(context, ProcessService.class);
        startService(context, processEventIntent);
        if (ProcessService.wakeLock == null) {
            PowerManager manager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            ProcessService.wakeLock = manager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "processServiceWake");
        }
        if (!ProcessService.wakeLock.isHeld())
            ProcessService.wakeLock.acquire(1000L * 60 * 5);
    }
}
