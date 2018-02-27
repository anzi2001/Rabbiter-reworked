package com.example.kocja.rabbiter_reworked.services;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;

/**
 * Created by kocja on 27/02/2018.
 */

public class processEvents extends IntentService {
    public processEvents(){
        super("This is processEvents");
    }
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

    }
}
