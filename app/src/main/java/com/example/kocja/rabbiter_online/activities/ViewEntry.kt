package com.example.kocja.rabbiter_online.activities

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle

import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer

import com.bumptech.glide.Glide
import com.example.kocja.rabbiter_online.R
import com.example.kocja.rabbiter_online.databinding.ActivityViewEntryBinding
import com.example.kocja.rabbiter_online.extensions.observeOnce
import com.example.kocja.rabbiter_online.fragments.HistoryFragment
import com.example.kocja.rabbiter_online.fragments.ViewEntryFragment
import com.example.kocja.rabbiter_online.viewmodels.ViewEntryViewModel
import kotlinx.android.synthetic.main.activity_view_entry.*
import kotlinx.android.synthetic.main.fragment_upcoming_history_layout.*
import org.koin.android.viewmodel.ext.android.viewModel

import java.util.UUID


/**
 * Created by kocja on 27/01/2018.
 */

class ViewEntry : AppCompatActivity() {
    private lateinit var mainEntryFragmentFragment: ViewEntryFragment
    private var dataChanged = false
    private val viewEntryViewModel: ViewEntryViewModel by viewModel()

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityViewEntryBinding = DataBindingUtil.setContentView(this, R.layout.activity_view_entry)
        binding.lifecycleOwner = this
        binding.viewEntryViewModel = viewEntryViewModel

        //setContentView(R.layout.activity_view_entry)

        val mainEntryUUID = intent.getSerializableExtra("entryID") as UUID
        val viewLargerImage = Intent(this, LargerMainImage::class.java)
        mainEntryView.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mainEntryView.transitionName = "closerLook"
                val options = ActivityOptionsCompat.makeSceneTransitionAnimation(this, mainEntryView, "closerLook")
                startActivity(viewLargerImage, options.toBundle())
            } else {
                startActivity(viewLargerImage)
            }

        }
        viewEntryViewModel.findEntryByUUID(mainEntryUUID.toString()).observeOnce(this, Observer { entry ->
            viewEntryViewModel.entry.value = entry
            viewLargerImage.putExtra("imageURI", entry.entryPhLoc)
            mainEntryFragmentFragment = supportFragmentManager.findFragmentById(R.id.mainEntryFragment) as ViewEntryFragment

            val historyFragment = supportFragmentManager.findFragmentById(R.id.historyFragment) as HistoryFragment
            if (entry.chooseGender == getString(R.string.genderMale)) {
                historyFragment.maleParentOf(this, entry.entryName!!, upcomingAdapter)
            } else {
                historyFragment.setPastEvents(this, entry.entryName!!, upcomingAdapter)
            }

            mainEntryFragmentFragment.setData(entry)

            viewEntryViewModel.findImage(entry.entryPhLoc!!).observeOnce(this, Observer {
                viewEntryViewModel.entry.value?.entryBitmap = it
                Glide.with(this@ViewEntry).load(it).into(mainEntryView)
            })


            if (entry.isMerged) {
                mergedView.setOnClickListener {
                    val startMergedMain = Intent(this, ViewEntry::class.java)
                    startMergedMain.putExtra("entryID", entry.mergedEntry)
                    val compat = ActivityOptionsCompat.makeSceneTransitionAnimation(this@ViewEntry, mainEntryView, "mergedName")
                    startActivity(startMergedMain, compat.toBundle())
                }
                mergedView.visibility = View.VISIBLE
                viewEntryViewModel.findImage(entry.mergedEntryPhLoc!!).observeOnce(this, Observer {
                    viewEntryViewModel.entry.value?.mergedEntryBitmap = it
                    Glide.with(this@ViewEntry).load(it).into(mergedView)
                })
            }
        })
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == AddEntryActivity.EDIT_EXISTING_ENTRY && resultCode == Activity.RESULT_OK) {
            viewEntryViewModel.findEntryByUUID().observeOnce(this, Observer {
                viewEntryViewModel.entry.value = it
                mainEntryFragmentFragment.setData(it)
                Glide.with(this@ViewEntry)
                        .load(it.entryPhLoc)
                        .into(mainEntryView)
            })
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
        if (item.itemId == R.id.editEntry) {
            val startEditProc = Intent(this@ViewEntry, AddEntryActivity::class.java)
            startEditProc.putExtra("getMode", AddEntryActivity.EDIT_EXISTING_ENTRY)
            startEditProc.putExtra("entryEdit", viewEntryViewModel.entry.value?.entryID)
            startActivityForResult(startEditProc, AddEntryActivity.EDIT_EXISTING_ENTRY)

        } else if (item.itemId == R.id.deleteEntry) {
            val areYouSure = AlertDialog.Builder(this@ViewEntry)
                    .setTitle(R.string.confirmDeletion)
                    .setPositiveButton(R.string.confirm) { _, _ ->
                        viewEntryViewModel.findEventsName().observeOnce(this, Observer {
                            for (event in it) {
                                viewEntryViewModel.deleteEvent(event.eventUUID.toString())
                            }
                            viewEntryViewModel.deleteEntry().observeOnce(this,Observer{
                                setResult(Activity.RESULT_OK)
                                finish()
                            })
                        })
                    }
                    .setNegativeButton(R.string.decline) { dialogInterface, _ -> dialogInterface.cancel() }
            areYouSure.show()

        } else if (item.itemId == R.id.entryStats) {
            val startStatActivity = Intent(applicationContext, ViewEntryStats::class.java)
            startStatActivity.putExtra("entryUUID", viewEntryViewModel.entry.value?.entryID)
            startActivity(startStatActivity)

        } else if (item.itemId == R.id.showMerged) {
            if (viewEntryViewModel.entry.value!!.isMerged) {
                val showMerged = Intent(applicationContext, ViewEntry::class.java)
                showMerged.putExtra("entryID", UUID.fromString(viewEntryViewModel.entry.value?.mergedEntry))
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
