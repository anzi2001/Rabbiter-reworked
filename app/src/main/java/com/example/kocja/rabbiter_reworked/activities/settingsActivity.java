package com.example.kocja.rabbiter_reworked.activities;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import com.example.kocja.rabbiter_reworked.fragments.SettingsFragment;


public class settingsActivity extends PreferenceActivity {
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content,new SettingsFragment())
                .commit();

    }
}
