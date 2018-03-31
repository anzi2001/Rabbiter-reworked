package com.example.kocja.rabbiter_reworked;

import android.app.Application;

import com.example.kocja.rabbiter_reworked.databases.appDatabase;
import com.raizlabs.android.dbflow.config.DatabaseConfig;
import com.raizlabs.android.dbflow.config.FlowConfig;
import com.raizlabs.android.dbflow.config.FlowManager;

/**
 * Created by kocja on 27/01/2018.
 */

public class mainApplication extends Application {
    public void onCreate(){
        FlowManager.init(FlowConfig.builder(this)
        .addDatabaseConfig(DatabaseConfig.builder(appDatabase.class)
        .databaseName("AppDatabase")
        .build())
        .build());
        super.onCreate();
    }
}
