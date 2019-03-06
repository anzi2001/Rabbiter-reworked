package com.example.kocja.rabbiter_online

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle

import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.view.View
import android.widget.Toast

import com.bumptech.glide.Glide
import com.example.kocja.rabbiter_online.activities.addEntryActivity
import com.example.kocja.rabbiter_online.activities.viewEntry
import com.example.kocja.rabbiter_online.adapters.EntriesRecyclerAdapter
import com.example.kocja.rabbiter_online.databases.Entry
import com.example.kocja.rabbiter_online.fragments.UpcomingEventsFragment
import com.example.kocja.rabbiter_online.managers.GsonManager
import com.example.kocja.rabbiter_online.managers.HttpManager
import com.example.kocja.rabbiter_online.services.alertIfNotAlertedService
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import java.util.UUID

import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.hdodenhof.circleimageview.CircleImageView

class rabbitActivity : AppCompatActivity(), EntriesRecyclerAdapter.onItemClickListener, fillData.onPost {
    private var chosenEntriesCounter = 0
    private var rabbitEntryView: RecyclerView? = null
    private var entriesList: List<Entry>? = null
    private var firstMergeEntry: Entry? = null
    private var secondMergeEntry: Entry? = null
    private var mergeFab: FloatingActionButton? = null
    private var splitFab: FloatingActionButton? = null
    private var gson: Gson? = null
    private var wasMergedBefore = false
    private var secondMerge: Entry? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rabbit)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        GsonManager.initGson()
        gson = GsonManager.getGson()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), START_PERMISSION_REQUEST)

        }

        val checkAlarms = Intent(this, alertIfNotAlertedService::class.java)
        startService(checkAlarms)

        val addFab = findViewById<FloatingActionButton>(R.id.addFab)
        mergeFab = findViewById(R.id.mergeFab)
        splitFab = findViewById(R.id.splitFab)
        addFab.setOnClickListener {
            val addEntryIntent = Intent(this, addEntryActivity::class.java)
            startActivityForResult(addEntryIntent, ADD_ENTRY_START)
        }
        rabbitEntryView = findViewById(R.id.rabbitEntryView)
        rabbitEntryView!!.setHasFixedSize(true)
        val rabbitEntryManager = GridLayoutManager(this, 3)
        rabbitEntryView!!.layoutManager = rabbitEntryManager
        fillData.getEntries(this, rabbitEntryView!!, this, this)

        mergeFab!!.setOnClickListener {
            if (chosenEntriesCounter > 2) {
                chosenEntriesCounter--
                Toast.makeText(this@rabbitActivity, R.string.alertMoreThan2Chosen, Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            secondMergeEntry!!.isMerged = true
            secondMergeEntry!!.mergedEntryName = firstMergeEntry!!.entryName
            secondMergeEntry!!.mergedEntry = firstMergeEntry!!.entryID.toString()
            secondMergeEntry!!.mergedEntryPhLoc = firstMergeEntry!!.entryPhLoc
            HttpManager.postRequest("updateEntry", gson!!.toJson(secondMergeEntry)) { _, _ -> }
            firstMergeEntry!!.isChildMerged = true
            HttpManager.postRequest("updateEntry", gson!!.toJson(firstMergeEntry)) { _, _ -> }

            //reset and refresh the grid at the end
            animateDown(mergeFab!!)
            chosenEntriesCounter = 0
            firstMergeEntry = null
            secondMergeEntry = null

            fillData.getEntries(this@rabbitActivity, rabbitEntryView!!, this, this)
        }
        splitFab!!.setOnClickListener {
            firstMergeEntry!!.isMerged = false

            HttpManager.postRequest("updateEntry", gson!!.toJson(firstMergeEntry)) { _, _ ->  }

            HttpManager.postRequest("seekSingleEntry", gson!!.toJson(firstMergeEntry!!.mergedEntry)) { response, _ ->
                secondMerge = gson!!.fromJson<Entry>(response, Entry::class.java)
                secondMerge!!.isChildMerged = false
                HttpManager.postRequest("updateEntry", gson!!.toJson(secondMerge)) { _,_->
                    chosenEntriesCounter = 0
                    firstMergeEntry = null
                    secondMergeEntry = null
                    this.runOnUiThread {
                        animateDown(splitFab!!)
                        fillData.getEntries(this@rabbitActivity, rabbitEntryView!!, this, this)
                    }

                }
            }

            //reset and refresh the grid at the end

        }

    }

    public override fun onActivityResult(requestCode: Int, resultcode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultcode, data)
        if (requestCode == ADD_ENTRY_START && resultcode == Activity.RESULT_OK) {
            fillData.getEntries(this, rabbitEntryView!!, this, this)
            val upcomingEvents = findViewById<RecyclerView>(R.id.upcomingAdapter)
            UpcomingEventsFragment.refreshFragment(upcomingEvents, this)
            UpcomingEventsFragment.updateNotesToDisplay { }
        } else if (requestCode == START_VIEW_ENTRY) {
            fillData.getEntries(this, rabbitEntryView!!, this, this)
            UpcomingEventsFragment.updateNotesToDisplay { }
        }
    }

    override fun onItemClick(view: View, position: Int) {
        val startViewEntry = Intent(this, viewEntry::class.java)
        startViewEntry.putExtra("entryID", view.tag as UUID)
        startActivityForResult(startViewEntry, START_VIEW_ENTRY)
    }

    override fun onLongItemClick(view: View, position: Int) {
        val markedOrNot = view.findViewById<CircleImageView>(R.id.MarkedOrNot)
        if (markedOrNot.drawable == null) {
            Glide.with(this).load(R.drawable.ic_markedornot).into(markedOrNot)
        }
        if (markedOrNot.visibility == View.GONE) {
            chosenEntriesCounter++

            //Since we're adding a new entry we're passing the old one to the second one,
            //since it became a second one now

            secondMergeEntry = firstMergeEntry
            firstMergeEntry = entriesList!![position]

            if (firstMergeEntry!!.isMerged && chosenEntriesCounter < 2) {
                animateUp(splitFab!!)
            }

            markedOrNot.visibility = View.VISIBLE
            // if both are not null we can safely merge the 2 chosen
            if (secondMergeEntry != null /*&& firstMergeEntry != null*/) {
                animateUp(mergeFab!!)
                wasMergedBefore = true
            }
        } else {
            chosenEntriesCounter--
            markedOrNot.visibility = View.GONE
            if (firstMergeEntry!!.isMerged) {
                animateDown(splitFab!!)
            }

            //If we're deselecting the entry, the second entry became the first, since
            //we now have only 1 entry
            firstMergeEntry = secondMergeEntry
            secondMergeEntry = null

            if (chosenEntriesCounter < 2 && wasMergedBefore) {
                animateDown(mergeFab!!)
                wasMergedBefore = false
            }

        }

    }

    override fun onPostProcess(temporaryList: List<Entry>) {
        entriesList = temporaryList
    }

    companion object {
        private const val ADD_ENTRY_START = 0
        private const val START_VIEW_ENTRY = 1
        private const val START_PERMISSION_REQUEST = 2

        private fun animateDown(toMove: FloatingActionButton) {
            toMove.animate().translationY(200f)
        }

        private fun animateUp(toMove: FloatingActionButton) {
            toMove.animate().translationY(-100f)
        }
    }
}
