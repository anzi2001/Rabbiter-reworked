package com.example.kocja.rabbiter_reworked;

import com.google.gson.Gson;

public class GsonManager {
    private static Gson gson;
    public static void initGson(){
        gson = new Gson();
    }

    public static Gson getGson(){
        return gson;
    }
}
