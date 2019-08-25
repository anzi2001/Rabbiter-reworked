package com.example.kocja.rabbiter_online.activities

import android.os.Bundle

import com.example.kocja.rabbiter_online.R
import com.example.kocja.rabbiter_online.models.Entry
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter


import androidx.appcompat.app.AppCompatActivity
import com.example.kocja.rabbiter_online.viewmodels.ViewEntryStatsViewModel
import kotlinx.android.synthetic.main.activity_stats_birth.*
import org.koin.android.viewmodel.ext.android.viewModel


/**
 * Created by kocja on 06/03/2018.
 */
class ViewEntryStats : AppCompatActivity() {
    private val viewEntryStatsViewModel : ViewEntryStatsViewModel by viewModel()
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stats_birth)

        viewEntryStatsViewModel.entry.value = intent.getParcelableExtra("entry") as Entry

        viewEntryStatsViewModel.populateGraphs {series,births->
            deadChart.addSeries(series["numDeadSeries"])
            deadChart.addSeries(series["avgDeadSeries"])

            BirthChart.addSeries(series["averageBirthSeries"])
            BirthChart.addSeries(series["numBirthSeries"])

            val dateAsXAxisLabelFormatter = DateAsXAxisLabelFormatter(this@ViewEntryStats)
            BirthChart.gridLabelRenderer.labelFormatter = dateAsXAxisLabelFormatter
            deadChart.gridLabelRenderer.labelFormatter = dateAsXAxisLabelFormatter
            BirthChart.gridLabelRenderer.numHorizontalLabels = 3
            deadChart.gridLabelRenderer.numHorizontalLabels = 3
            failedBirthsText.text = births[0].toString()
            successBirths.text = births[1].toString()
        }
    }
}
