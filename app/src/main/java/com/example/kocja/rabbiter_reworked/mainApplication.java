package com.example.kocja.rabbiter_reworked;

import android.app.Application;

import com.example.kocja.rabbiter_reworked.databases.appDatabase;
import com.raizlabs.android.dbflow.config.DatabaseConfig;
import com.raizlabs.android.dbflow.config.FlowConfig;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.squareup.leakcanary.LeakCanary;

/**
 * Created by kocja on 27/01/2018.
 */

public class mainApplication extends Application {
    public void onCreate(){
        super.onCreate();

        if(LeakCanary.isInAnalyzerProcess(this)){
            return;
        }
        LeakCanary.install(this);

        FlowManager.init(FlowConfig.builder(this)
        .addDatabaseConfig(DatabaseConfig.builder(appDatabase.class)
        .databaseName("AppDatabase")
        .build())
        .build());



    }
}
