package com.example.kocja.rabbiter_online;

import android.app.Application;


/**
 * Created by kocja on 27/01/2018.
 */

public class mainApplication extends Application {
    public void onCreate(){
        HttpManager.initHttpClient();
        super.onCreate();

    }

}
