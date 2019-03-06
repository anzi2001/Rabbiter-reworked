package com.example.kocja.rabbiter_online.managers

import com.google.gson.Gson
import com.google.gson.GsonBuilder

object GsonManager {
    private var gson: Gson? = null
    fun initGson() {
        val builder = GsonBuilder()
        //builder.registerTypeAdapter(boolean.class,new BooleanConvertAdapter());
        //builder.registerTypeAdapter(Boolean.class,new BooleanConvertAdapter());
        gson = builder.create()
    }

    fun getGson(): Gson? {
        if (gson == null) {
            initGson()
        }
        return gson

    }
}
