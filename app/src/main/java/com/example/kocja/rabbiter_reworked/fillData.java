package com.example.kocja.rabbiter_reworked;


import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.example.kocja.rabbiter_reworked.adapters.EntriesRecyclerAdapter;
import com.example.kocja.rabbiter_reworked.databases.Entry;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Response;


/**
 * Created by kocja on 05/02/2018.
 */

class fillData {
    static List<Entry> getEntries(Activity context, RecyclerView view,EntriesRecyclerAdapter.onItemClickListener listener){
        final List<Entry> temporaryList = new ArrayList<>(0);
        HttpManager.getRequest("seekChildMergedEntries", response -> {
            Log.v("seekChildMergedEntries",response.toString());

            temporaryList.add(GsonManager.getGson().fromJson(response.toString(),Entry.class));
            EntriesRecyclerAdapter adapter = new EntriesRecyclerAdapter(context,temporaryList);
            adapter.setLongClickListener(listener);
            view.setAdapter(adapter);

        });
        /*SQLite.select()
                .from(Entry.class)
                .where(Entry_Table.isChildMerged.eq(false))
                .async()
                .queryListResultCallback((transaction, tResult) -> {
                    EntriesRecyclerAdapter adapter = new EntriesRecyclerAdapter(context,tResult);
                    adapter.setLongClickListener(listener);
                    temporaryList.addAll(tResult);
                    view.setAdapter(adapter);
                }).execute();
        */
        return temporaryList;
    }


}
