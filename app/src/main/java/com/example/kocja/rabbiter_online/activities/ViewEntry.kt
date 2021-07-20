package com.example.kocja.rabbiter_online.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle

import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import coil.load

import com.example.kocja.rabbiter_online.R
import com.example.kocja.rabbiter_online.databinding.ActivityViewEntryBinding
import com.example.kocja.rabbiter_online.fragments.HistoryFragment
import com.example.kocja.rabbiter_online.models.Entry
import com.example.kocja.rabbiter_online.viewmodels.ViewEntryViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel


/**
 * Created by kocja on 27/01/2018.
 */

class ViewEntry : AppCompatActivity() {
    private var dataChanged = false
    private val viewEntryViewModel: ViewEntryViewModel by viewModel()
    private lateinit var binding : ActivityViewEntryBinding
    private val viewLargerImage by lazy{ Intent(this, LargerMainImage::class.java)}

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewEntryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val mergedEntryUUID = intent.getStringExtra("mergedEntryUUID")

        binding.mainEntryView.setOnClickListener {
            binding.mainEntryView.transitionName = "closerLook"
            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(this, binding.mainEntryView, "closerLook")
            startActivity(viewLargerImage, options.toBundle())
        }
        if(mergedEntryUUID != null){
            lifecycleScope.launch{
                val result = viewEntryViewModel.findEntryByUUID(mergedEntryUUID)
                initViewEntry(result)
            }
        }
        else initViewEntry(intent.getParcelableExtra("entry")!!)
    }

    private fun initViewEntry(entry : Entry){
        viewEntryViewModel.entry.value = entry
        viewLargerImage.putExtra("imageURL", entry.entryPhotoURL)

        val historyFragment = supportFragmentManager.findFragmentById(R.id.historyFragment) as HistoryFragment

        if (entry.chooseGender == getString(R.string.genderMale)) historyFragment.maleParentOf(this, entry.entryName, historyFragment.fragmentUpcomingHistoryLayoutBinding!!.upcomingAdapter)
        else historyFragment.setPastEvents(this, entry.entryName, historyFragment.fragmentUpcomingHistoryLayoutBinding!!.upcomingAdapter)

        binding.mainEntryView.load(viewEntryViewModel.entry.value!!.entryPhotoURL)


        if (entry.isMerged) {
            binding.mergedView.setOnClickListener {
                val startMergedMain = Intent(this, ViewEntry::class.java)
                startMergedMain.putExtra("entryUUID", entry.mergedEntryID)
                val compat = ActivityOptionsCompat.makeSceneTransitionAnimation(this@ViewEntry, binding.mainEntryView, "mergedName")
                startActivity(startMergedMain, compat.toBundle())
            }
            binding.mergedView.visibility = View.VISIBLE
            binding.mergedView.load(viewEntryViewModel.entry.value?.mergedEntryPhotoURL)
        }
    }

    override fun onBackPressed() {
        if (dataChanged) {
            setResult(Activity.RESULT_OK,Intent().putExtra("updatedEntry",viewEntryViewModel.entry.value!!))
            supportFinishAfterTransition()
        } else super.onBackPressed()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_view_entry_data, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.editEntry -> {
                val result = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                    if (it.resultCode == Activity.RESULT_OK) {
                        viewEntryViewModel.entry.value = it.data?.getParcelableExtra("updatedEntry")

                        binding.mainEntryView.load(viewEntryViewModel.entry.value!!.entryPhotoURL)
                        dataChanged = true
                    }
                }
                val addEntry = Intent(this@ViewEntry, AddEntryActivity::class.java)
                addEntry.putExtra("getMode", AddEntryActivity.EDIT_EXISTING_ENTRY)
                addEntry.putExtra("entry", viewEntryViewModel.entry.value!!)
                result.launch(addEntry)
            }
            R.id.deleteEntry -> {
                val entryUUID = viewEntryViewModel.entry.value!!.entryUUID
                val areYouSure = AlertDialog.Builder(this@ViewEntry)
                        .setTitle(R.string.confirmDeletion)
                        .setPositiveButton(R.string.confirm) { _, _ ->
                            lifecycleScope.launch {
                                val result = viewEntryViewModel.findEventsName()
                                result.forEach{
                                    viewEntryViewModel.deleteEvent(it.eventUUID)
                                }
                                viewEntryViewModel.deleteEntry()
                                setResult(Activity.RESULT_OK, Intent().putExtra("deletedEntryUUID", entryUUID))
                                finish()
                            }
                        }
                        .setNegativeButton(R.string.decline) { dialogInterface, _ -> dialogInterface.cancel() }
                areYouSure.show()
            }
            R.id.entryStats -> {
                val startStatActivity = Intent(applicationContext, ViewEntryStats::class.java)
                startStatActivity.putExtra("entry", viewEntryViewModel.entry.value)
                startActivity(startStatActivity)
            }
            R.id.showMerged -> {
                if (viewEntryViewModel.entry.value!!.isMerged) {
                    val showMerged = Intent(applicationContext, ViewEntry::class.java)
                    showMerged.putExtra("mergedEntryUUID", viewEntryViewModel.entry.value?.mergedEntryID)
                    startActivity(showMerged)
                } else {
                    val alertNotMerged = AlertDialog.Builder(this)
                            .setTitle("Oops")
                            .setMessage("your entry is not merged")
                            .setPositiveButton("OK") { dialogInterface, _ -> dialogInterface.cancel() }
                    alertNotMerged.show()
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
