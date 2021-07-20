package com.example.kocja.rabbiter_online.viewmodels

import android.graphics.Color
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kocja.rabbiter_online.managers.WebService
import com.example.kocja.rabbiter_online.models.Entry
import com.example.kocja.rabbiter_online.models.Events
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class ViewEntryStatsViewModel(private val fetcher : WebService) : ViewModel(){
    val entry : MutableLiveData<Entry> = MutableLiveData()


    private val numBirthsSeries = LineGraphSeries<DataPoint>()
    private val averageBirthSeries = LineGraphSeries<DataPoint>()
    private val numDeathSeries = LineGraphSeries<DataPoint>()
    private val avgDeadRabbits = LineGraphSeries<DataPoint>()
    private val formatter : SimpleDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    fun populateGraphs(onPostExecute : (series : HashMap<String,LineGraphSeries<DataPoint>>,births : List<Float>)->Unit) {
        viewModelScope.launch(Dispatchers.Default) {
            val events = fetcher.getEventByName(entry.value!!.entryName)

            var avgRabbitsNum = 0f
            var deadRabbitsNum = 0f
            var failedBirths = 0f
            var successBirthsNum = 0f

            events.forEach{
                if (it.notificationState == Events.EVENT_SUCCESSFUL) successBirthsNum++
                else failedBirths++

                avgRabbitsNum += it.rabbitsNum
                deadRabbitsNum += it.numDead

                numBirthsSeries.appendData(DataPoint(formatter.parse(it.dateOfEvent).time.toDouble(), it.rabbitsNum.toDouble()), true, 50)

            }
            avgRabbitsNum /= events.size
            deadRabbitsNum /= events.size

            events.forEach{
                val time = formatter.parse(it.dateOfEvent).time.toDouble()
                averageBirthSeries.appendData(DataPoint(time, avgRabbitsNum.toDouble()), true, 50)
            }

            events.forEach{
                val deadNumEventDate = formatter.parse(it.dateOfEvent).time.toDouble()
                numDeathSeries.appendData(DataPoint(deadNumEventDate, it.numDead.toDouble()), true, 50)
                avgDeadRabbits.appendData(DataPoint(deadNumEventDate, deadRabbitsNum.toDouble()), true, 50)
            }

            numBirthsSeries.color = Color.BLUE
            averageBirthSeries.color = Color.YELLOW

            numDeathSeries.color = Color.BLUE
            avgDeadRabbits.color = Color.YELLOW

            val hashMap = hashMapOf(
                    "numBirthSeries" to numBirthsSeries,
                    "numDeathSeries" to numDeathSeries,
                    "averageBirthSeries" to averageBirthSeries,
                    "avgDeadRabbits" to avgDeadRabbits
            )

            withContext(Dispatchers.Main){
                onPostExecute(hashMap, listOf(failedBirths, successBirthsNum))
            }

        }
    }
}