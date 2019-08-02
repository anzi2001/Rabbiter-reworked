package com.example.kocja.rabbiter_online

import android.app.Application
import com.example.kocja.rabbiter_online.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class RabbiterApplication : Application(){
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@RabbiterApplication)
            modules(viewModelModule)
        }
    }
}