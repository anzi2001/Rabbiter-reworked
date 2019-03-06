package com.example.kocja.rabbiter_reworked.activities

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kocja.rabbiter_reworked.R
import com.example.kocja.rabbiter_reworked.adapters.UpcomingEventsAdapter
import com.example.kocja.rabbiter_reworked.databases.Matings
import com.example.kocja.rabbiter_reworked.databases.Matings_Table
import com.raizlabs.android.dbflow.sql.language.SQLite
import kotlinx.android.synthetic.main.activity_view_all_matings.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ViewAllMatings : AppCompatActivity(){
    public override fun onCreate(savedInstanceState : Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_all_matings)


        val idString = intent.getSerializableExtra("entryID") as UUID
            SQLite.select()
                    .from(Matings::class.java)
                    .where(Matings_Table.entryID.eq(idString))
                    .async()
                    .queryListResultCallback{_,result ->
                        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.GERMANY)
                        Log.v("size",result.size.toString())
                        val matingsList = ArrayList<String>(10)
                        for(mating in result){
                            Log.v("mating",mating.entryID.toString())
                            matingsList.add(formatter.format(mating.matingDate))
                        }
                        val recyclerMatings = UpcomingEventsAdapter(matingsList,false)
                        val layout = LinearLayoutManager(this)
                        matingsView.adapter = recyclerMatings
                        matingsView.layoutManager = layout
                        matingsView.setHasFixedSize(true)
                        matingsView.adapter = recyclerMatings
                    }.execute()

    }
}