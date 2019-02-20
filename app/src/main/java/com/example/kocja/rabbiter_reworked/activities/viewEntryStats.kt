package com.example.kocja.rabbiter_reworked.activities

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.TextView

import com.example.kocja.rabbiter_reworked.R
import com.example.kocja.rabbiter_reworked.databases.Entry_Table
import com.example.kocja.rabbiter_reworked.databases.Events
import com.example.kocja.rabbiter_reworked.databases.Events_Table
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import com.raizlabs.android.dbflow.sql.language.SQLite

import java.util.UUID

/**
 * Created by kocja on 06/03/2018.
 */

class viewEntryStats : AppCompatActivity() {
    private var failedBirths = 0
    private var successBirths = 0

    @SuppressLint("StaticFieldLeak")
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.stats_birth_fragment)
        val entryID = intent.getSerializableExtra("entryUUID") as UUID
        SQLite.select()
                .from(com.example.kocja.rabbiter_reworked.databases.Entry::class.java)
                .where(Entry_Table.entryID.eq(entryID))
                .async()
                .querySingleResultCallback { _, entry ->
                    val birthGraph = findViewById<GraphView>(R.id.BirthChart)
                    val deadRabbitsGraph = findViewById<GraphView>(R.id.deadChart)
                    val successfulBirths = findViewById<TextView>(R.id.successBirths)
                    val failedBirthsText = findViewById<TextView>(R.id.failedBirthsText)

                    val numBirthsSeries = LineGraphSeries<DataPoint>()
                    val averageBirthSeries = LineGraphSeries<DataPoint>()
                    val numDeathSeries = LineGraphSeries<DataPoint>()
                    val avgDeadRabbits = LineGraphSeries<DataPoint>()

                    SQLite.select()
                            .from(Events::class.java)
                            .where(Events_Table.name.eq(entry?.entryName))
                            .and(Events_Table.typeOfEvent.eq(0))
                            .or(Events_Table.secondParent.eq(entry?.entryName))
                            .and(Events_Table.typeOfEvent.eq(0))
                            .orderBy(Events_Table.dateOfEvent, true)
                            .async()
                            .queryListResultCallback { _, tResult ->
                                object : AsyncTask<Void, Void, Void>() {
                                    override fun doInBackground(vararg voids: Void): Void? {

                                        var avgRabbitsNum = 0f
                                        var deadRabbitsNum = 0f

                                        for (singleEvent in tResult) {
                                            if (singleEvent.notificationState == Events.EVENT_SUCCESSFUL) {
                                                successBirths++
                                            } else {
                                                failedBirths++
                                            }
                                            avgRabbitsNum += singleEvent.rabbitsNum.toFloat()
                                            deadRabbitsNum += singleEvent.numDead.toFloat()

                                            numBirthsSeries.appendData(DataPoint(singleEvent.dateOfEvent!!.time.toDouble(), singleEvent.rabbitsNum.toDouble()), true, 50)
                                        }
                                        avgRabbitsNum /= tResult.size.toFloat()
                                        deadRabbitsNum /= tResult.size.toFloat()

                                        for (singleEvent in tResult) {
                                            averageBirthSeries.appendData(DataPoint(singleEvent.dateOfEvent!!.time.toDouble(), avgRabbitsNum.toDouble()), true, 50)
                                        }


                                        for (deadNumEvent in tResult) {
                                            numDeathSeries.appendData(DataPoint(deadNumEvent.dateOfEvent!!.time.toDouble(), deadNumEvent.numDead.toDouble()), true, 50)
                                            avgDeadRabbits.appendData(DataPoint(deadNumEvent.dateOfEvent!!.time.toDouble(), deadRabbitsNum.toDouble()), true, 50)
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
                            }.execute()

                }.execute()
    }
}
