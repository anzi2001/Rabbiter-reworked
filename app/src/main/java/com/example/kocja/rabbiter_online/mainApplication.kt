package com.example.kocja.rabbiter_online

import android.app.Application

import com.example.kocja.rabbiter_online.managers.HttpManager


/**
 * Created by kocja on 27/01/2018.
 */

class mainApplication : Application() {
    override fun onCreate() {
        HttpManager.initHttpClient()
        super.onCreate()

    }

}
