package com.example.kocja.rabbiter_online

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle

import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.get
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope

import com.example.kocja.rabbiter_online.activities.AddEntryActivity
import com.example.kocja.rabbiter_online.activities.ViewEntry
import com.example.kocja.rabbiter_online.adapters.EntriesRecyclerAdapter
import com.example.kocja.rabbiter_online.fragments.UpcomingEventsFragment

import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kocja.rabbiter_online.databinding.ActivityRabbitBinding
import com.example.kocja.rabbiter_online.models.Entry
import com.example.kocja.rabbiter_online.viewmodels.RabbitViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel

class RabbitActivity : AppCompatActivity(), View.OnClickListener,View.OnLongClickListener {

    private val rabbitViewModel by viewModel<RabbitViewModel>()
    private val longClickedColor = Color.parseColor("#33171717")
    lateinit var activityRabbitBinding: ActivityRabbitBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityRabbitBinding = ActivityRabbitBinding.inflate(layoutInflater)
        setContentView(activityRabbitBinding.root)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), START_PERMISSION_REQUEST)
        }

        activityRabbitBinding.addFab.setOnClickListener {
            val addEntryResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
                val upcomingEvents = supportFragmentManager.findFragmentById(R.id.fragment) as UpcomingEventsFragment
                val newEntry: Entry? = it.data?.getParcelableExtra("addNewEntry")
                rabbitViewModel.entriesList.add(newEntry!!)
                upcomingEvents.refreshFragment()
                upcomingEvents.updateNotesToDisplay()
            }
            addEntryResult.launch(Intent(this, AddEntryActivity::class.java))
        }
        rabbitViewModel.getEntries()
        activityRabbitBinding.rabbitEntryView.setHasFixedSize(true)
        activityRabbitBinding.rabbitEntryView.layoutManager = LinearLayoutManager(this)
        activityRabbitBinding.rabbitEntryView.adapter = EntriesRecyclerAdapter(this,rabbitViewModel.entriesList).apply{
            setClickListeners(this@RabbitActivity,this@RabbitActivity)
        }


        activityRabbitBinding.merge.setOnClickListener {
            rabbitViewModel.onMergeClick()
            activityRabbitBinding.rabbitEntryView[rabbitViewModel.chosenPositions.keyAt(0)].setBackgroundColor(Color.TRANSPARENT)
            activityRabbitBinding.rabbitEntryView[rabbitViewModel.chosenPositions.keyAt(1)].setBackgroundColor(Color.TRANSPARENT)
            it.visibility = View.GONE
            rabbitViewModel.chosenPositions.clear()
        }

        activityRabbitBinding.split.setOnClickListener {
            lifecycleScope.launch{
                rabbitViewModel.onSplitClick()
                withContext(Dispatchers.Main){
                    activityRabbitBinding.rabbitEntryView[rabbitViewModel.chosenPositions.keyAt(0)].setBackgroundColor(Color.TRANSPARENT)
                    it.visibility = View.GONE
                    rabbitViewModel.chosenPositions.clear()
                }
            }
        }
    }

    override fun onClick(view: View) {
        val startViewEntryResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
            val data = result.data
            if(data != null && data.hasExtra("deletedEntryUUID")){
                val deletedEventUUID : String? = data.getStringExtra("deletedEntryUUID")
                val position = rabbitViewModel.entriesList.indexOfFirst { it.entryUUID == deletedEventUUID }
                rabbitViewModel.entriesList.removeAt(position)
            }

            data?.getParcelableExtra<Entry>("updatedEntry")?.let{
                val index = rabbitViewModel.entriesList.indexOfFirst {entry -> entry.entryUUID == it.entryUUID }
                if(index != -1){
                    val upcomingEvents = supportFragmentManager.findFragmentById(R.id.fragment) as UpcomingEventsFragment
                    rabbitViewModel.entriesList[index] = it
                    upcomingEvents.updateNotesToDisplay()
                }
            }
        }
        val startViewEntry = Intent(this, ViewEntry::class.java)
        startViewEntry.putExtra("entry", rabbitViewModel.entriesList[view.tag as Int])
        startViewEntryResult.launch(startViewEntry)
    }

    override fun onLongClick(view: View): Boolean {
        val position = view.tag as Int
        if (view.background.alpha.toFloat() == 0.0f) {
            view.setBackgroundColor(longClickedColor)
            with(rabbitViewModel) {
                chosenPositions.append(position, position)

                if (chosenPositions.size() == 2) {
                    val neitherMerged = chosenPositions.run {
                        !entriesList[keyAt(0)].isMerged && !entriesList[keyAt(1)].isMerged
                    }
                    activityRabbitBinding.merge.visibility = if (neitherMerged) View.VISIBLE else View.GONE
                }
                else{
                    activityRabbitBinding.merge.visibility = View.GONE
                }
                activityRabbitBinding.split.visibility = if (chosenPositions.size() == 1 && entriesList[position].isMerged) {
                    View.VISIBLE
                } else View.GONE
            }
        } else {
            view.setBackgroundColor(Color.TRANSPARENT)
            with(rabbitViewModel){
                if (chosenPositions.size() == 1 && entriesList[position].isMerged) {
                    activityRabbitBinding.split.visibility = View.GONE
                }
                //If we're deselecting the entry, the second entry became the first, since
                //we now have only 1 entry
                chosenPositions.delete(position)

                activityRabbitBinding.merge.visibility = if (chosenPositions.size() == 2) View.VISIBLE else View.GONE

            }
        }
        return true
    }

    companion object {
        private const val START_PERMISSION_REQUEST = 2
    }
}
