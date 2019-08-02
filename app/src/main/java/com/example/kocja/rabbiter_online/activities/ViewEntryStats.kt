package com.example.kocja.rabbiter_online.activities

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.AsyncTask
import android.os.Bundle

import com.example.kocja.rabbiter_online.managers.GsonManager
import com.example.kocja.rabbiter_online.managers.HttpManager
import com.example.kocja.rabbiter_online.R
import com.example.kocja.rabbiter_online.models.Entry
import com.example.kocja.rabbiter_online.models.Events
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries

import java.text.SimpleDateFormat
import java.util.Locale
import java.util.UUID

import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.stats_birth_fragment.*


/**
 * Created by kocja on 06/03/2018.
 */
@SuppressLint("StaticFieldLeak")
class ViewEntryStats : AppCompatActivity() {

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.stats_birth_fragment)
        val entryID = intent.getSerializableExtra("entryUUID") as UUID

        var failedBirths = 0
        var successBirthsNum = 0

        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.GERMAN)

        HttpManager.postRequest("seekSingleEntry", GsonManager.gson.toJson(entryID)) { response, _ ->
            val entry = GsonManager.gson.fromJson(response, Entry::class.java)

            val numBirthsSeries = LineGraphSeries<DataPoint>()
            val averageBirthSeries = LineGraphSeries<DataPoint>()
            val numDeathSeries = LineGraphSeries<DataPoint>()
            val avgDeadRabbits = LineGraphSeries<DataPoint>()

            HttpManager.postRequest("seekEventsByNameType", GsonManager.gson.toJson(entry.entryName)) { response1, _ ->

                object : AsyncTask<Void, Void, Void>() {
                    override fun doInBackground(vararg voids: Void): Void? {
                        val result = GsonManager.gson.fromJson(response1, Array<Events>::class.java).toList()
                        var avgRabbitsNum = 0f
                        var deadRabbitsNum = 0f

                        for (singleEvent in result) {
                            if (singleEvent.notificationState == Events.EVENT_SUCCESSFUL) {
                                successBirthsNum++
                            } else {
                                failedBirths++
                            }
                            avgRabbitsNum += singleEvent.rabbitsNum.toFloat()
                            deadRabbitsNum += singleEvent.numDead.toFloat()

                            numBirthsSeries.appendData(DataPoint(formatter.parse(singleEvent.dateOfEvent).time.toDouble(), singleEvent.rabbitsNum.toDouble()), true, 50)

                        }
                        avgRabbitsNum /= result.size.toFloat()
                        deadRabbitsNum /= result.size.toFloat()

                        for (singleEvent in result) {
                            averageBirthSeries.appendData(DataPoint(formatter.parse(singleEvent.dateOfEvent).time.toDouble(), avgRabbitsNum.toDouble()), true, 50)
                        }

                        for (deadNumEvent in result) {
                            numDeathSeries.appendData(DataPoint(formatter.parse(deadNumEvent.dateOfEvent).time.toDouble(), deadNumEvent.numDead.toDouble()), true, 50)
                            avgDeadRabbits.appendData(DataPoint(formatter.parse(deadNumEvent.dateOfEvent).time.toDouble(), deadRabbitsNum.toDouble()), true, 50)
                        }

                        numBirthsSeries.color = Color.BLUE
                        averageBirthSeries.color = Color.YELLOW

                        numDeathSeries.color = Color.BLUE
                        avgDeadRabbits.color = Color.YELLOW

                        return null
                    }

                    override fun onPostExecute(aVoid: Void) {
                        deadChart.addSeries(numDeathSeries)
                        deadChart.addSeries(avgDeadRabbits)

                        BirthChart.addSeries(averageBirthSeries)
                        BirthChart.addSeries(numBirthsSeries)

                        BirthChart.gridLabelRenderer.labelFormatter = DateAsXAxisLabelFormatter(this@ViewEntryStats)
                        BirthChart.gridLabelRenderer.numHorizontalLabels = 3
                        deadChart.gridLabelRenderer.labelFormatter = DateAsXAxisLabelFormatter(this@ViewEntryStats)
                        deadChart.gridLabelRenderer.numHorizontalLabels = 3
                        failedBirthsText.text = failedBirths.toString()
                        successBirths.text = successBirths.toString()
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
