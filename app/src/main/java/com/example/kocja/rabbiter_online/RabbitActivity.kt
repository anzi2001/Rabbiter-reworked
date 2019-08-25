package com.example.kocja.rabbiter_online

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle

import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.view.View
import androidx.core.view.get
import androidx.lifecycle.Observer

import com.example.kocja.rabbiter_online.activities.AddEntryActivity
import com.example.kocja.rabbiter_online.activities.ViewEntry
import com.example.kocja.rabbiter_online.adapters.EntriesRecyclerAdapter
import com.example.kocja.rabbiter_online.fragments.UpcomingEventsFragment

import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kocja.rabbiter_online.extensions.notifyObserver
import com.example.kocja.rabbiter_online.models.Entry
import com.example.kocja.rabbiter_online.viewmodels.RabbitViewModel
import kotlinx.android.synthetic.main.activity_rabbit.*
import kotlinx.android.synthetic.main.fragment_upcoming_history_layout.*
import org.koin.android.viewmodel.ext.android.viewModel

class RabbitActivity : AppCompatActivity(), View.OnClickListener,View.OnLongClickListener {

    private val rabbitViewModel by viewModel<RabbitViewModel>()
    private val longClickedColor = Color.parseColor("#33171717")
    private val entriesRecyclerAdapter : EntriesRecyclerAdapter by lazy{
        EntriesRecyclerAdapter(this,rabbitViewModel.entriesList.value!!)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rabbit)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), START_PERMISSION_REQUEST)
        }

        //startService(Intent(this, AlertIfNotAlertedService::class.java))

        addFab.setOnClickListener {
            val addEntryIntent = Intent(this, AddEntryActivity::class.java)
            startActivityForResult(addEntryIntent, ADD_ENTRY_START)
        }
        rabbitViewModel.getEntries()
        rabbitEntryView.setHasFixedSize(true)
        rabbitEntryView.layoutManager = LinearLayoutManager(this)
        entriesRecyclerAdapter.setClickListeners(this,this)
        rabbitEntryView.adapter = entriesRecyclerAdapter

        rabbitViewModel.entriesList.observe(this, Observer {
            entriesRecyclerAdapter.notifyDataSetChanged()
        })

        merge.setOnClickListener {
            rabbitViewModel.onMergeClick {
                rabbitEntryView[rabbitViewModel.chosenPositions.keyAt(0)].setBackgroundColor(Color.TRANSPARENT)
                rabbitEntryView[rabbitViewModel.chosenPositions.keyAt(1)].setBackgroundColor(Color.TRANSPARENT)
                it.visibility = View.GONE
                rabbitViewModel.chosenPositions.clear()
            }

        }
        split.setOnClickListener {
            rabbitViewModel.onSplitClick {
                rabbitEntryView[rabbitViewModel.chosenPositions.keyAt(0)].setBackgroundColor(Color.TRANSPARENT)
                it.visibility = View.GONE
                rabbitViewModel.chosenPositions.clear()
            }
        }

    }

    public override fun onActivityResult(requestCode: Int, resultcode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultcode, data)

        val upcomingEvents = supportFragmentManager.findFragmentById(R.id.fragment) as UpcomingEventsFragment
        if (requestCode == ADD_ENTRY_START && resultcode == Activity.RESULT_OK) {
            val newEntry = data!!.getParcelableExtra<Entry>("addNewEntry")
            rabbitViewModel.entriesList.value?.add(newEntry)
            rabbitViewModel.entriesList.notifyObserver()
            upcomingEvents.refreshFragment(upcomingAdapter, this)
            upcomingEvents.updateNotesToDisplay { }
        } else if (requestCode == START_VIEW_ENTRY && resultcode == Activity.RESULT_OK) {
            if(data != null && data.hasExtra("deletedEntryUUID")){
                val deletedEventUUID : String = data.getStringExtra("deletedEntryUUID")
                val position = rabbitViewModel.entriesList.value!!.map{it.entryUUID}.indexOf(deletedEventUUID)
                rabbitViewModel.entriesList.value!!.removeAt(position)
                rabbitViewModel.entriesList.notifyObserver()
            }

            val updatedEntry : Entry? = data?.getParcelableExtra("updatedEntry")

            updatedEntry?.let{
                val index = rabbitViewModel.entriesList.value!!.map{it.entryUUID}.indexOf(updatedEntry.entryUUID)
                if(index != -1){
                    rabbitViewModel.entriesList.value!![index] = updatedEntry
                    rabbitViewModel.entriesList.notifyObserver()
                    upcomingEvents.updateNotesToDisplay { }
                }
            }

        }
    }

    override fun onClick(view: View) {
        val startViewEntry = Intent(this, ViewEntry::class.java)
        startViewEntry.putExtra("entry", rabbitViewModel.entriesList.value!![rabbitEntryView.getChildAdapterPosition(view)])
        startActivityForResult(startViewEntry, START_VIEW_ENTRY)
    }

    override fun onLongClick(view: View): Boolean {
        val position = rabbitEntryView.getChildAdapterPosition(view)
        if (view.background.alpha.toFloat() == 0.0f) {
            view.setBackgroundColor(longClickedColor)
            with(rabbitViewModel) {
                chosenPositions.append(position, position)

                if (chosenPositions.size() == 2) {
                    val neitherMerged = chosenPositions.run {
                        !entriesList.value!![keyAt(0)].isMerged && !entriesList.value!![keyAt(1)].isMerged
                    }
                    merge.visibility = if (neitherMerged) {
                        View.VISIBLE
                    } else {
                        View.GONE
                    }
                }
                else{
                    merge.visibility = View.GONE
                }
                split.visibility = if (chosenPositions.size() == 1 && entriesList.value!![position].isMerged) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
            }
        } else {
            view.setBackgroundColor(Color.TRANSPARENT)
            with(rabbitViewModel){
                if (chosenPositions.size() == 1 && entriesList.value!![position].isMerged) {
                    split.visibility = View.GONE
                }
                //If we're deselecting the entry, the second entry became the first, since
                //we now have only 1 entry
                chosenPositions.delete(position)

                merge.visibility = if (chosenPositions.size() == 2) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
            }
        }
        return true
    }

    companion object {
        private const val ADD_ENTRY_START = 0
        private const val START_VIEW_ENTRY = 1
        private const val START_PERMISSION_REQUEST = 2
    }
}
