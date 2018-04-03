package com.example.kocja.rabbiter_reworked;


import android.app.Activity;
import android.support.v7.widget.RecyclerView;

import com.example.kocja.rabbiter_reworked.adapters.EntriesRecyclerAdapter;
import com.example.kocja.rabbiter_reworked.databases.Entry;
import com.example.kocja.rabbiter_reworked.databases.Entry_Table;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by kocja on 05/02/2018.
 */

class fillData {
    static List<Entry> getEntries(Activity context, RecyclerView view,EntriesRecyclerAdapter.onItemClickListener listener){
        final List<Entry> temporaryList = new ArrayList<>(0);
        SQLite.select()
                .from(Entry.class)
                .where(Entry_Table.isChildMerged.eq(false))
                .async()
                .queryListResultCallback((transaction, tResult) -> {
                    EntriesRecyclerAdapter adapter = new EntriesRecyclerAdapter(context,tResult);
                    adapter.setLongClickListener(listener);
                    temporaryList.addAll(tResult);
                    view.setAdapter(adapter);
                }).execute();
        return temporaryList;
    }


}
