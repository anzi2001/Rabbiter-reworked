package com.example.kocja.rabbiter_online.activities

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.AsyncTask
import android.os.Bundle
import android.widget.TextView

import com.example.kocja.rabbiter_online.managers.GsonManager
import com.example.kocja.rabbiter_online.managers.HttpManager
import com.example.kocja.rabbiter_online.R
import com.example.kocja.rabbiter_online.databases.Entry
import com.example.kocja.rabbiter_online.databases.Events
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Arrays
import java.util.Locale
import java.util.UUID

import androidx.appcompat.app.AppCompatActivity


/**
 * Created by kocja on 06/03/2018.
 */

class viewEntryStats : AppCompatActivity() {
    private var failedBirths = 0
    private var successBirths = 0
    private var formatter: SimpleDateFormat? = null

    @SuppressLint("StaticFieldLeak")
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.stats_birth_fragment)
        val entryID = intent.getSerializableExtra("entryUUID") as UUID

        formatter = SimpleDateFormat("dd/MM/yyyy", Locale.GERMAN)
        HttpManager.postRequest("seekSingleEntry", GsonManager.getGson()!!.toJson(entryID)) { response, _ ->
            val entry = GsonManager.getGson()!!.fromJson(response, Entry::class.java)
            val birthGraph = findViewById<GraphView>(R.id.BirthChart)
            val deadRabbitsGraph = findViewById<GraphView>(R.id.deadChart)
            val successfulBirths = findViewById<TextView>(R.id.successBirths)
            val failedBirthsText = findViewById<TextView>(R.id.failedBirthsText)

            val numBirthsSeries = LineGraphSeries<DataPoint>()
            val averageBirthSeries = LineGraphSeries<DataPoint>()
            val numDeathSeries = LineGraphSeries<DataPoint>()
            val avgDeadRabbits = LineGraphSeries<DataPoint>()

            HttpManager.postRequest("seekEventsByNameType", GsonManager.getGson()!!.toJson(entry.entryName)) { response1, _ ->
                object : AsyncTask<Void, Void, Void>() {
                    override fun doInBackground(vararg voids: Void): Void? {
                        val result = ArrayList(Arrays.asList(*GsonManager.getGson()!!.fromJson(response1, Array<Events>::class.java)))
                        var avgRabbitsNum = 0f
                        var deadRabbitsNum = 0f

                        for (singleEvent in result) {
                            if (singleEvent.notificationState == Events.EVENT_SUCCESSFUL) {
                                successBirths++
                            } else {
                                failedBirths++
                            }
                            avgRabbitsNum += singleEvent.rabbitsNum.toFloat()
                            deadRabbitsNum += singleEvent.numDead.toFloat()

                            try {
                                numBirthsSeries.appendData(DataPoint(formatter!!.parse(singleEvent.dateOfEvent).time.toDouble(), singleEvent.rabbitsNum.toDouble()), true, 50)
                            } catch (e: ParseException) {
                                e.printStackTrace()
                            }

                        }
                        avgRabbitsNum /= result.size.toFloat()
                        deadRabbitsNum /= result.size.toFloat()

                        for (singleEvent in result) {
                            try {
                                averageBirthSeries.appendData(DataPoint(formatter!!.parse(singleEvent.dateOfEvent).time.toDouble(), avgRabbitsNum.toDouble()), true, 50)
                            } catch (e: ParseException) {
                                e.printStackTrace()
                            }

                        }

                        for (deadNumEvent in result) {
                            try {
                                numDeathSeries.appendData(DataPoint(formatter!!.parse(deadNumEvent.dateOfEvent).time.toDouble(), deadNumEvent.numDead.toDouble()), true, 50)
                                avgDeadRabbits.appendData(DataPoint(formatter!!.parse(deadNumEvent.dateOfEvent).time.toDouble(), deadRabbitsNum.toDouble()), true, 50)
                            } catch (e: ParseException) {
                                e.printStackTrace()
                            }

                        }

                        numBirthsSeries.color = Color.BLUE
                        averageBirthSeries.color = Color.YELLOW

                        numDeathSeries.color = Color.BLUE
                        avgDeadRabbits.color = Color.YELLOW

                        return null
                    }

                    override fun onPostExecute(aVoid: Void) {
                        deadRabbitsGraph.addSeries(numDeathSeries)
                        deadRabbitsGraph.addSeries(avgDeadRabbits)

                        birthGraph.addSeries(averageBirthSeries)
                        birthGraph.addSeries(numBirthsSeries)

                        birthGraph.gridLabelRenderer.labelFormatter = DateAsXAxisLabelFormatter(this@viewEntryStats)
                        birthGraph.gridLabelRenderer.numHorizontalLabels = 3
                        deadRabbitsGraph.gridLabelRenderer.labelFormatter = DateAsXAxisLabelFormatter(this@viewEntryStats)
                        deadRabbitsGraph.gridLabelRenderer.numHorizontalLabels = 3
                        failedBirthsText.text = failedBirths.toString()
                        successfulBirths.text = successBirths.toString()
                        super.onPostExecute(aVoid)
                    }
                }.execute()
            }
        }
        /*
        SQLite.select()
                .from(com.example.kocja.rabbiter_reworked.databases.Entry.class)
                .where(Entry_Table.entryID.eq(entryID))
                .async()
                .querySingleResultCallback((transaction, entry) -> {


                            SQLite.select()
                                    .from(Events.class)
                                    .where(Events_Table.name.eq(entry.entryName))
                                    .and(Events_Table.typeOfEvent.eq(0))
                                    .or(Events_Table.secondParent.eq(entry.entryName))
                                    .and(Events_Table.typeOfEvent.eq(0))
                                    .orderBy(Events_Table.dateOfEvent, true)
                                    .async()
                                    .queryListResultCallback((transaction1, tResult) ->
                     *
                }).execute();
          */
    }
}
