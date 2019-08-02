package com.example.kocja.rabbiter_online.managers

import com.google.gson.Gson
import com.google.gson.GsonBuilder

object GsonManager {
    val gson: Gson by lazy{
        GsonBuilder().create()
    }
}
