package com.example.kocja.rabbiter_reworked;


import android.content.Context;
import android.widget.GridView;

import com.example.kocja.rabbiter_reworked.adapters.EntriesAdapter;
import com.example.kocja.rabbiter_reworked.databases.Entry;
import com.example.kocja.rabbiter_reworked.databases.Entry_Table;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by kocja on 05/02/2018.
 */

class fillData {
    static List<Entry> getEntries(Context context, GridView view){
        final List<Entry> temporaryList = new ArrayList<>(0);
        SQLite.select()
                .from(Entry.class)
                .where(Entry_Table.isChildMerged.eq(false))
                .async()
                .queryListResultCallback((transaction, tResult) -> {
                    EntriesAdapter adapter = new EntriesAdapter(context,tResult);
                    temporaryList.addAll(tResult);
                    view.setAdapter(adapter);
                }).execute();
        return temporaryList;
    }


}
