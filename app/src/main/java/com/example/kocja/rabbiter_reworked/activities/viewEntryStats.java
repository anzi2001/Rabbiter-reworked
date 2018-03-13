package com.example.kocja.rabbiter_reworked.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import com.example.kocja.rabbiter_reworked.R;
import com.example.kocja.rabbiter_reworked.adapters.statsPagerAdapter;

import java.util.UUID;

/**
 * Created by kocja on 06/03/2018.
 */

public class viewEntryStats extends AppCompatActivity {
    public void onCreate(Bundle savedIntstanceState){
        super.onCreate(savedIntstanceState);
        setContentView(R.layout.viewstatentry_activity);
        UUID entryUUID =(UUID) getIntent().getSerializableExtra("entryUUID");
        FragmentPagerAdapter adapter = new statsPagerAdapter(getSupportFragmentManager(),entryUUID.toString());
        ViewPager statsPager = findViewById(R.id.statsPager);
        statsPager.setAdapter(adapter);

    }
}
