package com.example.kocja.rabbiter_reworked.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.core.app.ActivityOptionsCompat
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

import com.bumptech.glide.Glide
import com.example.kocja.rabbiter_reworked.R
import com.example.kocja.rabbiter_reworked.databases.Entry
import com.example.kocja.rabbiter_reworked.databases.Entry_Table
import com.example.kocja.rabbiter_reworked.databases.Events
import com.example.kocja.rabbiter_reworked.databases.Events_Table
import com.example.kocja.rabbiter_reworked.fragments.HistoryFragment
import com.example.kocja.rabbiter_reworked.fragments.viewEntryData
import com.raizlabs.android.dbflow.sql.language.SQLite
import kotlinx.android.synthetic.main.activity_view_entry.*
import kotlinx.android.synthetic.main.upcoming_history_fragment_layout.*

import java.util.UUID

/**
 * Created by kocja on 27/01/2018.
 */

class viewEntry : AppCompatActivity() {
    private var mainEntry: Entry? = null
    private var mainEntryFragment: viewEntryData? = null
    private var mainEntryUUID: UUID? = null
    private var dataChanged = false
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_entry)
        val currentIntent = intent
        mainEntryUUID = currentIntent.getSerializableExtra("entryID") as UUID
        val viewLargerImage = Intent(this, largerMainImage::class.java)
        mainEntryView!!.setOnClickListener {
            mainEntryView!!.transitionName = "closerLook"
            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(this, mainEntryView!!, "closerLook")
            startActivity(viewLargerImage, options.toBundle())

        }
        SQLite.select()
                .from(Entry::class.java)
                .where(Entry_Table.entryID.eq(mainEntryUUID))
                .async()
                .querySingleResultCallback { _, entry ->
                    viewLargerImage.putExtra("imageURI", entry?.entryPhLoc)

                    mainEntry = entry
                    mainEntryFragment = supportFragmentManager.findFragmentById(R.id.mainEntryFragment) as viewEntryData?


                    if (entry!!.chooseGender == getString(R.string.genderMale)) {
                        HistoryFragment.maleParentOf(this, entry.entryName, upcomingAdapter, this@viewEntry)
                    } else {
                        HistoryFragment.setPastEvents(this, entry.entryName, upcomingAdapter)
                    }

                    mainEntryFragment!!.setData(entry)

                    Glide.with(this@viewEntry).load(entry.entryPhLoc).into(mainEntryView!!)

                    if (entry.isMerged) {
                        mergedView.setOnClickListener {
                            val startMergedMain = Intent(this, viewEntry::class.java)
                            startMergedMain.putExtra("entryID", entry.mergedEntry!!.entryID)
                            val compat = ActivityOptionsCompat.makeSceneTransitionAnimation(this, mainEntryView!!, "mergedName")
                            startActivity(startMergedMain, compat.toBundle())
                        }
                        mergedView.visibility = View.VISIBLE
                        Glide.with(this).load(entry.mergedEntryPhLoc).into(mergedView)
                    }
                }.execute()
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == AddEntryActivity.EDIT_EXISTING_ENTRY && resultCode == Activity.RESULT_OK) {
            SQLite.select()
                    .from(Entry::class.java)
                    .where(Entry_Table.entryID.eq(mainEntryUUID))
                    .async()
                    .querySingleResultCallback { _, entry ->
                        mainEntryFragment!!.setData(entry)
                        Glide.with(this)
                                .load(entry?.entryPhLoc)
                                .into(mainEntryView!!)
                    }.execute()
            dataChanged = true

        }
    }

    override fun onBackPressed() {
        if (dataChanged) {
            setResult(Activity.RESULT_OK)
            supportFinishAfterTransition()
            //finish();
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_view_entry_data, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.editEntry) {
            val startEditProc = Intent(this@viewEntry, AddEntryActivity::class.java)
            startEditProc.putExtra("getMode", AddEntryActivity.EDIT_EXISTING_ENTRY)
            startEditProc.putExtra("entryEdit", mainEntry!!.entryID)
            startActivityForResult(startEditProc, AddEntryActivity.EDIT_EXISTING_ENTRY)

        } else if (id == R.id.deleteEntry) {
            val assureDeletion = AlertDialog.Builder(this@viewEntry)
                    .setTitle(R.string.confirmDeletion)
                    .setPositiveButton(R.string.confirm) { _, _ ->
                        SQLite.select()
                                .from(Events::class.java)
                                .where(Events_Table.name.eq(mainEntry!!.entryName))
                                .async()
                                .queryListResultCallback { _, tResult ->
                                    for (event in tResult) {
                                        event.delete()
                                    }
                                    mainEntry!!.delete()
                                    setResult(Activity.RESULT_OK)
                                    finish()
                                }.execute()
                    }
                    .setNegativeButton(R.string.decline) { dialogInterface, _ -> dialogInterface.cancel() }
            assureDeletion.show()

        } else if (id == R.id.entryStats) {
            val startStatActivity = Intent(applicationContext, viewEntryStats::class.java)
            startStatActivity.putExtra("entryUUID", mainEntry!!.entryID)
            startActivity(startStatActivity)

        } else if (id == R.id.showMerged) {
            if (mainEntry!!.isMerged) {
                val showMerged = Intent(applicationContext, viewEntry::class.java)
                showMerged.putExtra("entryID", mainEntry!!.mergedEntry!!.entryID)
                startActivity(showMerged)
            } else {
                val alertNotMerged = AlertDialog.Builder(this)
                        .setTitle("Oops")
                        .setMessage("your entry is not merged")
                        .setPositiveButton("OK") { dialogInterface, _ -> dialogInterface.cancel() }
                alertNotMerged.show()
            }

        }
        return super.onOptionsItemSelected(item)
    }
}
