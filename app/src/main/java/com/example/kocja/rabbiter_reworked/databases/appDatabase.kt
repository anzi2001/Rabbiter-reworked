package com.example.kocja.rabbiter_reworked.databases

import com.raizlabs.android.dbflow.annotation.Database

/**
 * Created by kocja on 23/01/2018.
 */
@Database(version = appDatabase.VERSION)
class appDatabase {

    companion object {
        internal const val VERSION = 3
    }
}

