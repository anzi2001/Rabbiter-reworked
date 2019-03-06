package com.example.kocja.rabbiter_reworked

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.util.Log
import android.util.SparseIntArray
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager

import com.example.kocja.rabbiter_reworked.activities.AddEntryActivity
import com.example.kocja.rabbiter_reworked.activities.viewEntry
import com.example.kocja.rabbiter_reworked.adapters.EntriesRecyclerAdapter
import com.example.kocja.rabbiter_reworked.databases.Entry
import com.example.kocja.rabbiter_reworked.databases.Events
import com.example.kocja.rabbiter_reworked.fragments.UpcomingEventsFragment
import com.example.kocja.rabbiter_reworked.services.alertIfNotAlertedService
import com.google.gson.Gson
import com.raizlabs.android.dbflow.sql.language.SQLite
import kotlinx.android.synthetic.main.activity_rabbit.*
import kotlinx.android.synthetic.main.content_rabbit.*
import kotlinx.android.synthetic.main.upcoming_history_fragment_layout.*

import java.io.File
import java.io.IOException
import java.util.UUID

import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response

class RabbitActivity : AppCompatActivity(), EntriesRecyclerAdapter.OnItemClickListener {
    private var chosenEntriesCounter = 0
    private var entriesList: List<Entry>? = null

    private val chosenPositions = SparseIntArray()
    private var secondMergeEntry: Entry? = null
    private var firstMergeEntry: Entry? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rabbit)
        setSupportActionBar(toolbar)


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), START_PERMISSION_REQUEST)

        }

        val checkAlarms = Intent(this, alertIfNotAlertedService::class.java)
        startService(checkAlarms)


        addFab.setOnClickListener {
            val addEntryIntent = Intent(this, AddEntryActivity::class.java)
            startActivityForResult(addEntryIntent, ADD_ENTRY_START)
        }
        rabbitEntryView.setHasFixedSize(true)
        val rabbitEntryManager = LinearLayoutManager(this)
        rabbitEntryView!!.layoutManager = rabbitEntryManager
        entriesList = fillData.getEntries(this, rabbitEntryView!!, this)



        merge.setOnClickListener {
            secondMergeEntry = entriesList!![chosenPositions.keyAt(1)]
            firstMergeEntry = entriesList!![chosenPositions.keyAt(0)]

            secondMergeEntry!!.isMerged = true
            secondMergeEntry!!.mergedEntryName = firstMergeEntry!!.entryName
            secondMergeEntry!!.mergedEntry = firstMergeEntry
            secondMergeEntry!!.mergedEntryPhLoc = firstMergeEntry!!.entryPhLoc
            secondMergeEntry!!.update()


            firstMergeEntry!!.isChildMerged = true
            firstMergeEntry!!.update()

            //reset and refresh the grid at the end
            merge.visibility = View.GONE
            chosenEntriesCounter = 0
            firstMergeEntry = null
            secondMergeEntry = null

            entriesList = fillData.getEntries(this@RabbitActivity, rabbitEntryView!!, this)
        }
        split.setOnClickListener {
            secondMergeEntry = entriesList!![chosenPositions.keyAt(1)]
            firstMergeEntry = entriesList!![chosenPositions.keyAt(0)]

            firstMergeEntry!!.isMerged = false
            firstMergeEntry!!.update()

            firstMergeEntry!!.mergedEntry!!.load()
            val secondMerge = firstMergeEntry!!.mergedEntry
            secondMerge!!.isChildMerged = false
            secondMerge.update()

            //reset and refresh the grid at the end
            split!!.visibility = View.GONE
            chosenEntriesCounter = 0
            firstMergeEntry = null
            secondMergeEntry = null

            entriesList = fillData.getEntries(this@RabbitActivity, rabbitEntryView!!, this)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_rabbit_acitivty, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.moveOnline) {
            val json = MediaType.parse("application/json; charset=utf-8")
            val client = OkHttpClient()
            val gson = Gson()
            SQLite.select()
                    .from(Entry::class.java)
                    .async()
                    .queryListResultCallback { _, tResult ->
                        for (entry in tResult) {
                                val imgFile: File
                                val reqBody: RequestBody
                                val realPath = getRealPathContentUri(Uri.parse(entry.entryPhLoc))
                                imgFile = File(realPath)
                                Log.d("imgFileName", imgFile.name)
                                Log.d("uri path", Uri.parse(entry.entryPhLoc).path!!.substring(11))
                                val multipartBody = MultipartBody.Builder()
                                        .setType(MultipartBody.FORM)
                                        .addFormDataPart("entry", gson.toJson(entry))
                                        .addFormDataPart("entryImage", imgFile.name, RequestBody.create(MediaType.parse("image/jpg"), imgFile))
                                    entry.mergedEntryPhLoc = Uri.parse(entry.mergedEntryPhLoc).path!!.substring(11)
                                reqBody = multipartBody.build()
                                val req = Request.Builder()
                                        .url("http://nodejs-mongo-persistent-rabbit.a3c1.starter-us-west-1.openshiftapps.com/moveOnlineEntry")
                                        .post(reqBody)
                                        .build()
                                client.newCall(req).enqueue(object : Callback {
                                    override fun onFailure(call: Call, e: IOException) {
                                        e.printStackTrace()
                                    }

                                    override fun onResponse(call: Call, response: Response) {

                                    }
                                })
                        }
                    }.execute()

            SQLite.select()
                    .from(Events::class.java)
                    .async()
                    .queryListResultCallback { _, tResult ->
                        for (event in tResult) {
                            val req = Request.Builder()
                                    .url("http://nodejs-mongo-persistent-rabbit.a3c1.starter-us-west-1.openshiftapps.com/moveOnlineEvent")
                                    .post(RequestBody.create(json, gson.toJson(event)))
                                    .build()
                            client.newCall(req).enqueue(object : Callback {
                                override fun onFailure(call: Call, e: IOException) {

                                }

                                override fun onResponse(call: Call, response: Response) {}
                            })
                        }
                    }.execute()
        }
        return true
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ADD_ENTRY_START && resultCode == Activity.RESULT_OK) {
            entriesList = fillData.getEntries(this, rabbitEntryView!!, this)
            UpcomingEventsFragment.refreshFragment(upcomingAdapter, this)
            UpcomingEventsFragment.updateNotesToDisplay()
        } else if (requestCode == START_VIEW_ENTRY) {
            entriesList = fillData.getEntries(this, rabbitEntryView!!, this)
            UpcomingEventsFragment.updateNotesToDisplay()
        }
    }

    override fun onItemClick(view: View, position: Int) {
        val startViewEntry = Intent(this, viewEntry::class.java)
        startViewEntry.putExtra("entryID", view.tag as UUID)
        startActivityForResult(startViewEntry, START_VIEW_ENTRY)
    }

    override fun onLongItemClick(view: View, position: Int) {

        if (view.background.alpha.toFloat() == 0.0f) {
            view.setBackgroundColor(Color.parseColor("#33171717"))

            chosenPositions.append(position, position)
            if (chosenPositions.size() == 2) {
                merge.visibility = View.VISIBLE
            } else {
                merge.visibility = View.GONE
            }

            if (entriesList!![chosenPositions.get(position)].isMerged && chosenPositions.size() == 1) {
                split.visibility = View.VISIBLE
            } else {
                split.visibility = View.GONE
            }


        } else {
            view.setBackgroundColor(Color.TRANSPARENT)

            //chosenEntriesCounter--;


            if (chosenPositions.size() == 1 && entriesList!![chosenPositions.get(position)].isMerged) {
                split.visibility = View.GONE
            }

            //If we're deselecting the entry, the second entry became the first, since
            //we now have only 1 entry
            chosenPositions.delete(position)

            if (chosenPositions.size() == 2) {
                merge.visibility = View.VISIBLE
            } else {
                merge.visibility = View.GONE
            }


        }

    }

    private fun getRealPathContentUri(contentUri: Uri): String {
        val images = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = contentResolver.query(contentUri, images, null, null, null)
        cursor!!.moveToFirst()
        val columnIndex = cursor.getColumnIndex(images[0])
        val path = cursor.getString(columnIndex)
        cursor.close()
        return path
    }

    companion object {
        private const  val ADD_ENTRY_START = 0
        const val START_VIEW_ENTRY = 1
        private  const val START_PERMISSION_REQUEST = 2
    }
}
