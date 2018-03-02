package com.example.kocja.rabbiter_reworked.activities;

import android.support.v4.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.kocja.rabbiter_reworked.R;
import com.example.kocja.rabbiter_reworked.databases.Entry;
import com.example.kocja.rabbiter_reworked.databases.Entry_Table;
import com.example.kocja.rabbiter_reworked.databases.Events;
import com.example.kocja.rabbiter_reworked.databases.Events_Table;
import com.example.kocja.rabbiter_reworked.fragments.HistoryFragment;
import com.example.kocja.rabbiter_reworked.fragments.viewEntryData;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.structure.database.transaction.Transaction;

import java.util.UUID;

/**
 * Created by kocja on 27/01/2018.
 */

public class viewEntry extends AppCompatActivity {
    private Entry mergedEntry;
    private Entry mainEntry;
    private viewEntryData mainEntryFragment;
    private viewEntryData mergedEntryFragment;
    private UUID mainEntryUUID;
    private boolean dataChanged = false;
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_entry);
        Intent currentIntent = getIntent();
        mainEntryUUID =(UUID) currentIntent.getSerializableExtra("entryID");
        SQLite.select()
                .from(Entry.class)
                .where(Entry_Table.entryID.eq(mainEntryUUID))
                .async()
                .querySingleResultCallback((transaction, entry) -> {
                    mainEntryFragment = (viewEntryData) getSupportFragmentManager().findFragmentById(R.id.mainEntryFragment);

                    HistoryFragment.setPastEvents(entry.entryName);

                    mainEntry = entry;

                    mainEntryFragment.setData(entry.entryName,entry.chooseGender,entry.birthDate,entry.matedDate,entry.matedWithOrParents);

                    ImageView mainView = findViewById(R.id.mainEntryView);
                    Glide.with(viewEntry.this).load(entry.entryPhLoc).into(mainView);

                    mergedEntryFragment =(viewEntryData)getSupportFragmentManager().findFragmentById(R.id.mergedEntryFragment);
                    FragmentManager manager = getSupportFragmentManager();

                    if(entry.isMerged){
                        View line = findViewById(R.id.line);
                        line.setVisibility(View.VISIBLE);
                        ImageView mergedView = findViewById(R.id.mergedView);
                        mergedView.setVisibility(View.VISIBLE);
                        Glide.with(this).load(entry.mergedEntryPhLoc).into(mergedView);

                        entry.mergedEntry.async().success((Transaction.Success) transaction1 -> {

                            mergedEntry = entry.mergedEntry;

                            mergedEntryFragment.setData(
                                    mergedEntry.entryName,mergedEntry.chooseGender,mergedEntry.birthDate,
                                    mergedEntry.matedDate,mergedEntry.matedWithOrParents);
                        }).load();
                    }
                    else{
                        manager.beginTransaction()
                                .hide(mergedEntryFragment)
                                .commit();
                    }
                }).execute();

        ImageButton deleteEntry = findViewById(R.id.deleteEntry);
        deleteEntry.setOnClickListener(view -> {

            AlertDialog.Builder assureDeletion = new AlertDialog.Builder(viewEntry.this)
                    .setTitle("Are you sure you want to delete?")
                    .setPositiveButton("Yes", (dialogInterface, i) ->
                            SQLite.select()
                            .from(Events.class)
                            .where(Events_Table.name.eq(mainEntry.entryName))
                            .async()
                            .queryListResultCallback((transaction, tResult) -> {
                                for(Events event: tResult){
                                    event.delete();
                                }
                                mainEntry.delete();
                                setResult(RESULT_OK);
                                finish();
                            }).execute())
                    .setNegativeButton("No", (dialogInterface, i) -> dialogInterface.cancel());
            assureDeletion.show();

        });
        ImageButton editEntry = findViewById(R.id.editEntry);
        editEntry.setOnClickListener(view -> {
            Intent startEditProc = new Intent(viewEntry.this,addEntryActivity.class);
            startEditProc.putExtra("getMode",addEntryActivity.EDIT_EXISTING_ENTRY);
            startEditProc.putExtra("entryEdit",mainEntry.entryID);
            startActivityForResult(startEditProc,addEntryActivity.EDIT_EXISTING_ENTRY);
        });
    }
    public void onActivityResult(int requestCode,int resultCode,Intent data){
        if(requestCode == addEntryActivity.EDIT_EXISTING_ENTRY && resultCode == RESULT_OK){
            SQLite.select()
                    .from(Entry.class)
                    .where(Entry_Table.entryID.eq(mainEntryUUID))
                    .async()
                    .querySingleResultCallback((transaction, entry) -> mainEntryFragment.setData(entry.entryName, entry.chooseGender, entry.birthDate, entry.matedDate, entry.matedWithOrParents)
                    ).execute();
            dataChanged = true;

        }
    }
    public void onBackPressed(){
        if(dataChanged){
            setResult(RESULT_OK);
            finish();
        }
        else{
            super.onBackPressed();
        }
    }
}
