package com.example.kocja.rabbiter_online.viewmodels

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.AsyncTask
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.kocja.rabbiter_online.managers.DataFetcher
import com.example.kocja.rabbiter_online.models.Entry
import com.example.kocja.rabbiter_online.models.Events
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

@SuppressLint("StaticFieldLeak")
class ViewEntryStatsViewModel(private val fetcher : DataFetcher) : ViewModel(){
    val entry : MutableLiveData<Entry> = MutableLiveData()


    val numBirthsSeries = LineGraphSeries<DataPoint>()
    val averageBirthSeries = LineGraphSeries<DataPoint>()
    val numDeathSeries = LineGraphSeries<DataPoint>()
    val avgDeadRabbits = LineGraphSeries<DataPoint>()

    val formatter : SimpleDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    fun populateGraphs(onPostExecute : (series : HashMap<String,LineGraphSeries<DataPoint>>,births : List<Float>)->Unit){
        fetcher.findEventsByNameType(entry.value!!.entryName!!){

            object : AsyncTask<Void, Void, List<Float>>() {
                override fun doInBackground(vararg voids: Void): List<Float>? {
                    var avgRabbitsNum = 0f
                    var deadRabbitsNum = 0f
                    var failedBirths = 0f
                    var successBirthsNum = 0f

                    for (singleEvent in it) {
                        if (singleEvent.notificationState == Events.EVENT_SUCCESSFUL) {
                            successBirthsNum++
                        } else {
                            failedBirths++
                        }
                        avgRabbitsNum += singleEvent.rabbitsNum
                        deadRabbitsNum += singleEvent.numDead

                        numBirthsSeries.appendData(DataPoint(formatter.parse(singleEvent.dateOfEvent).time.toDouble(), singleEvent.rabbitsNum.toDouble()), true, 50)

                    }
                    avgRabbitsNum /= it.size
                    deadRabbitsNum /= it.size

                    for (singleEvent in it) {
                        val time = formatter.parse(singleEvent.dateOfEvent).time.toDouble()
                        averageBirthSeries.appendData(DataPoint(time, avgRabbitsNum.toDouble()), true, 50)
                    }

                    for (deadNumEvent in it) {
                        val deadNumEventDate = formatter.parse(deadNumEvent.dateOfEvent).time.toDouble()
                        numDeathSeries.appendData(DataPoint(deadNumEventDate, deadNumEvent.numDead.toDouble()), true, 50)
                        avgDeadRabbits.appendData(DataPoint(deadNumEventDate, deadRabbitsNum.toDouble()), true, 50)
                    }

                    numBirthsSeries.color = Color.BLUE
                    averageBirthSeries.color = Color.YELLOW

                    numDeathSeries.color = Color.BLUE
                    avgDeadRabbits.color = Color.YELLOW

                    return listOf(failedBirths,successBirthsNum)
                }

                override fun onPostExecute(births: List<Float>) {
                    val hashMap = hashMapOf(
                            "numBirthSeries" to numBirthsSeries,
                            "numDeathSeries" to numDeathSeries,
                            "averageBirthSeries" to averageBirthSeries,
                            "avgDeadRabbits" to avgDeadRabbits
                    )
                    onPostExecute(hashMap, births)
                    super.onPostExecute(births)
                }
            }.execute()
        }

    }

}