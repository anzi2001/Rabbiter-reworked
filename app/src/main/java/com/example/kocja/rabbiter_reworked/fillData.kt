package com.example.kocja.rabbiter_reworked


import android.app.Activity
import androidx.recyclerview.widget.RecyclerView

import com.example.kocja.rabbiter_reworked.adapters.EntriesRecyclerAdapter
import com.example.kocja.rabbiter_reworked.databases.Entry
import com.example.kocja.rabbiter_reworked.databases.Entry_Table
import com.raizlabs.android.dbflow.sql.language.SQLite
import java.util.ArrayList


/**
 * Created by kocja on 05/02/2018.
 */

internal object fillData {
    fun getEntries(context: Activity, view: RecyclerView, listener: EntriesRecyclerAdapter.OnItemClickListener): List<Entry> {
        val temporaryList = ArrayList<Entry>(5)
        SQLite.select()
                .from(Entry::class.java)
                .where(Entry_Table.isChildMerged.eq(false))
                .async()
                .queryListResultCallback { _, tResult ->
                    val adapter = EntriesRecyclerAdapter(context, tResult)
                    adapter.setLongClickListener(listener)
                    temporaryList.addAll(tResult)
                    view.adapter = adapter
                }.execute()
        return temporaryList
    }


}
