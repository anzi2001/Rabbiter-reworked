package com.example.kocja.rabbiter_reworked.activities;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
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
import com.raizlabs.android.dbflow.structure.database.transaction.QueryTransaction;
import com.raizlabs.android.dbflow.structure.database.transaction.Transaction;

import java.util.List;
import java.util.UUID;

/**
 * Created by kocja on 27/01/2018.
 */

public class viewEntry extends AppCompatActivity {
    private Entry mergedEntry;
    private Entry mainEntry;
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_entry);
        Intent currentIntent = getIntent();
        UUID mainEntryUUID =(UUID) currentIntent.getSerializableExtra("entryID");
        SQLite.select()
                .from(Entry.class)
                .where(Entry_Table.entryID.eq(mainEntryUUID))
                .async()
                .querySingleResultCallback((transaction, entry) -> {
                    HistoryFragment.setPastEvents(entry.entryName);

                    mainEntry = entry;
                    viewEntryData mainEntryFragment = (viewEntryData) getSupportFragmentManager().findFragmentById(R.id.mainEntryFragment);
                    mainEntryFragment.setData(entry.entryName,entry.chooseGender,entry.birthDate,entry.matedDate,entry.matedWithOrParents);

                    ImageView mainView = findViewById(R.id.imageView);
                    Glide.with(viewEntry.this).load(entry.entryPhLoc).into(mainView);

                    viewEntryData mergedEntryFragment =(viewEntryData)getSupportFragmentManager().findFragmentById(R.id.mergedEntryFragment);
                    FragmentManager manager = getSupportFragmentManager();

                    if(entry.isMerged){
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
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            SQLite.select()
                                    .from(Events.class)
                                    .where(Events_Table.name.eq(mainEntry.entryName))
                                    .async()
                                    .queryListResultCallback(new QueryTransaction.QueryResultListCallback<Events>() {
                                        @Override
                                        public void onListQueryResult(QueryTransaction transaction, @NonNull List<Events> tResult) {
                                            for(Events event: tResult){
                                                event.delete();
                                            }
                                            mainEntry.delete();
                                            setResult(RESULT_OK);
                                            finish();
                                        }
                                    }).execute();
                        }
                    })
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
}
