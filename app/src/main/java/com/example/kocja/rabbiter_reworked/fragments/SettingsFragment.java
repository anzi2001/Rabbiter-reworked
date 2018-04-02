package com.example.kocja.rabbiter_reworked.fragments;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.example.kocja.rabbiter_reworked.R;

import java.util.Locale;

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener{
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        if(s.equals("sl-rSI")){
            Locale locale = new Locale("sl- SI");
            Locale.setDefault(locale);
            Configuration config = getResources().getConfiguration();
            config.locale = locale;
            getResources().updateConfiguration(config,getResources().getDisplayMetrics());
        }
        else{
            Locale locale = new Locale("en_GB");
            Locale.setDefault(locale);
            Configuration config = getResources().getConfiguration();
            config.locale = locale;
            getResources().updateConfiguration(config,getResources().getDisplayMetrics());
        }
    }

}
