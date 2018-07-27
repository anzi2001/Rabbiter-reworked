package com.example.kocja.rabbiter_online.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GsonManager {
    private static Gson gson;
    public static void initGson(){
        GsonBuilder builder = new GsonBuilder();
        //builder.registerTypeAdapter(boolean.class,new BooleanConvertAdapter());
        //builder.registerTypeAdapter(Boolean.class,new BooleanConvertAdapter());
        gson = builder.create();
    }

    public static Gson getGson(){
        if(gson == null){
            initGson();
        }
        return gson;

    }
}
