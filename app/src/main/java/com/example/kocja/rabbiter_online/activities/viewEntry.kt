package com.example.kocja.rabbiter_online.activities

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle

import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView

import com.bumptech.glide.Glide
import com.example.kocja.rabbiter_online.managers.GsonManager
import com.example.kocja.rabbiter_online.managers.HttpManager
import com.example.kocja.rabbiter_online.R
import com.example.kocja.rabbiter_online.databases.Entry
import com.example.kocja.rabbiter_online.databases.Events
import com.example.kocja.rabbiter_online.fragments.HistoryFragment
import com.example.kocja.rabbiter_online.fragments.viewEntryData
import com.google.gson.Gson
import kotlinx.android.synthetic.main.upcoming_history_fragment_layout.*

import java.util.UUID


/**
 * Created by kocja on 27/01/2018.
 */

class viewEntry : AppCompatActivity() {
    private var mainEntry: Entry? = null
    private var mainEntryFragment: viewEntryData? = null
    private var dataChanged = false
    private var mainEntryView: ImageView? = null
    private var gson: Gson? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_entry)
        val currentIntent = intent

        gson = GsonManager.getGson()
        val mainEntryUUID = currentIntent.getSerializableExtra("entryID") as UUID
        mainEntryView = findViewById(R.id.mainEntryView)
        val viewLargerImage = Intent(this, largerMainImage::class.java)
        mainEntryView!!.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mainEntryView!!.transitionName = "closerLook"
                val options = ActivityOptionsCompat.makeSceneTransitionAnimation(this, mainEntryView!!, "closerLook")
                startActivity(viewLargerImage, options.toBundle())
            } else {
                startActivity(viewLargerImage)
            }

        }
        HttpManager.postRequest("seekSingleEntry", gson!!.toJson(mainEntryUUID)) { response, _ ->

            this.runOnUiThread {
                val entry = gson!!.fromJson(response, Entry::class.java)
                viewLargerImage.putExtra("imageURI", entry.entryPhLoc)
                mainEntry = entry
                mainEntryFragment = supportFragmentManager.findFragmentById(R.id.mainEntryFragment) as viewEntryData?


                if (entry.chooseGender == getString(R.string.genderMale)) {
                    HistoryFragment.maleParentOf(this, entry.entryName, upcomingAdapter, this@viewEntry)
                } else {
                    HistoryFragment.setPastEvents(this, entry.entryName, upcomingAdapter)
                }

                mainEntryFragment!!.setData(entry)

                HttpManager.postRequest("searchForImage", gson!!.toJson(entry.entryPhLoc)) { _, bytes1 ->
                    entry.entryBitmap = BitmapFactory.decodeByteArray(bytes1, 0, bytes1!!.size)
                    this.runOnUiThread { Glide.with(this@viewEntry).load(entry.entryBitmap).into(mainEntryView!!) }

                }


                if (entry.isMerged) {
                    val mergedView = findViewById<ImageView>(R.id.mergedView)
                    mergedView.setOnClickListener {
                        val startMergedMain = Intent(this, viewEntry::class.java)
                        startMergedMain.putExtra("entryID", entry.mergedEntry)
                        val compat = ActivityOptionsCompat.makeSceneTransitionAnimation(this@viewEntry, mainEntryView!!, "mergedName")
                        startActivity(startMergedMain, compat.toBundle())
                    }
                    mergedView.visibility = View.VISIBLE
                    HttpManager.postRequest("searchForImage", gson!!.toJson(entry.mergedEntryPhLoc)) { _, bytes1 ->
                        entry.mergedEntryBitmap = BitmapFactory.decodeByteArray(bytes1, 0, bytes1!!.size)
                        this.runOnUiThread { Glide.with(this@viewEntry).load(entry.mergedEntryBitmap).into(mergedView) }
                    }

                }
            }

        }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == addEntryActivity.EDIT_EXISTING_ENTRY && resultCode == Activity.RESULT_OK) {
            HttpManager.postRequest("seekSingleEntry", gson!!.toJson(mainEntry)) { response, _ ->
                val entry = gson!!.fromJson(response, Entry::class.java)
                this.runOnUiThread {
                    mainEntryFragment!!.setData(entry)
                    Glide.with(this@viewEntry)
                            .load(entry.entryPhLoc)
                            .into(mainEntryView!!)
                }

            }
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
            val startEditProc = Intent(this@viewEntry, addEntryActivity::class.java)
            startEditProc.putExtra("getMode", addEntryActivity.EDIT_EXISTING_ENTRY)
            startEditProc.putExtra("entryEdit", mainEntry!!.entryID)
            startActivityForResult(startEditProc, addEntryActivity.EDIT_EXISTING_ENTRY)

        } else if (id == R.id.deleteEntry) {
            val assureDeletion = AlertDialog.Builder(this@viewEntry)
                    .setTitle(R.string.confirmDeletion)
                    .setPositiveButton(R.string.confirm) { _, _ ->
                        HttpManager.postRequest("seekEventsName", gson!!.toJson(mainEntry!!.entryName)) { response, _ ->
                            val events = gson!!.fromJson(response, Array<Events>::class.java)
                            this.runOnUiThread {
                                for (event in events) {
                                    HttpManager.postRequest("deleteEvent", gson!!.toJson(event.eventUUID)) { _, _ -> }
                                }
                                HttpManager.postRequest("deleteEntry", gson!!.toJson(mainEntry!!.entryID)) { _, _ ->
                                    setResult(Activity.RESULT_OK)
                                    finish()
                                }
                            }


                        }
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
                showMerged.putExtra("entryID", UUID.fromString(mainEntry!!.mergedEntry))
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
