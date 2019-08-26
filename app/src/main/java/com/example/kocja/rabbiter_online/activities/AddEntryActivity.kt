package com.example.kocja.rabbiter_online.activities

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns

import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import coil.api.load

import com.example.kocja.rabbiter_online.R
import com.example.kocja.rabbiter_online.databinding.ActivityAddEntryBinding
import com.example.kocja.rabbiter_online.extensions.getDownscaledBitmap
import com.example.kocja.rabbiter_online.extensions.observeOnce
import com.example.kocja.rabbiter_online.models.Entry
import com.example.kocja.rabbiter_online.models.Events
import com.example.kocja.rabbiter_online.services.EventTriggered
import com.example.kocja.rabbiter_online.services.ProcessService
import com.example.kocja.rabbiter_online.viewmodels.AddEntryViewModel
import kotlinx.android.synthetic.main.activity_add_entry.*
import org.koin.android.viewmodel.ext.android.viewModel

import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar
import java.util.Locale
import java.util.UUID
import java.util.concurrent.TimeUnit

/**
 * Created by kocja on 21/01/2018.
 */

class AddEntryActivity : AppCompatActivity() {

    private var takeBirthDateCal = false
    private val textFormatter: SimpleDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private lateinit var matedWithAdapter: ArrayAdapter<String>
    private lateinit var genderAdapter: ArrayAdapter<String>
    private val addEntryViewModel: AddEntryViewModel by viewModel()
    //NOTE: type 0: birth
    //NOTE: type 1: ready for mating
    //NOTE: type 2: move group
    //NOTE: type 3: slaughter date

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTitle(R.string.title)

        val binding: ActivityAddEntryBinding = DataBindingUtil.setContentView(this, R.layout.activity_add_entry)
        binding.lifecycleOwner = this
        binding.addEntryViewModel = addEntryViewModel

        val getMode = intent.getIntExtra("getMode", -1)

        takePhoto.setOnClickListener {
            val chooseMethod = AlertDialog.Builder(this)
                    .setTitle(R.string.photoOption)
                    .setItems(R.array.DecideOnPhType) { _, i ->
                        if (i == 0) {
                            val photoPickerIntent = Intent(Intent.ACTION_GET_CONTENT)
                            photoPickerIntent.addCategory(Intent.CATEGORY_OPENABLE)
                            photoPickerIntent.type = "image/*"
                            startActivityForResult(Intent.createChooser(photoPickerIntent,"choose type of image"), SELECT_PHOTO)
                        } else {
                            dispatchTakePictureIntent()
                        }
                    }
            chooseMethod.show()

        }

        addEntryViewModel.entry.value = Entry(UUID.randomUUID().toString())
        addEntryViewModel.entryBitmap = null

        addEntryViewModel.photoUri.observe(this, Observer {
            addEntryViewModel.hasEntryPhotoChanged = true
            addEntryViewModel.setUriSpecificValues(getFileName(it))
        })


        addEntryViewModel.getAllEntries().observeOnce(this, Observer {
            val allEntryNames = mutableListOf(getString(R.string.none))
            allEntryNames.addAll(it.map { it1 -> it1.entryName })

            matedWithAdapter = ArrayAdapter(this@AddEntryActivity, android.R.layout.simple_spinner_dropdown_item, allEntryNames)
            matedWithSpinner.adapter = matedWithAdapter
            parentSpinner.adapter = matedWithAdapter
            setEditableEntryProps(getMode)
        })
        //set today date for a datePickerDialog and set listeners
        val calendar = Calendar.getInstance()
        val pickDate = DatePickerDialog(this, DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            if (takeBirthDateCal) {
                addBirthDate.setText(textFormatter.format(GregorianCalendar(year, month, dayOfMonth).time))
            } else {
                val formattedText = textFormatter.format(GregorianCalendar(year, month, dayOfMonth).time)
                if (addEntryViewModel.entry.value!!.matedDate != formattedText) {
                    addEntryViewModel.matedDateChanged = true
                }
                addMatingDate.setText(formattedText)
            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))

        addBirthDateCal.setOnClickListener {
            takeBirthDateCal = true
            pickDate.show()
        }

        addMatingDateCal.setOnClickListener {
            takeBirthDateCal = false
            pickDate.show()
        }

        genderAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, resources.getStringArray(R.array.decideOnGender))
        addGender.adapter = genderAdapter
        addGender.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>, view: View, i: Int, l: Long) {
                when (addGender.selectedItem.toString()) {
                    getString(R.string.genderMale) -> setGenderSpecificVisibility(View.GONE, getString(R.string.entryMatedWith))

                    //Temporary fix, would have to reconstraint views. will see.
                    /*addMatingDate.setVisibility(View.INVISIBLE);
                    addMatingDateCal.setVisibility(View.INVISIBLE);
                    matingDateText.setVisibility(View.INVISIBLE);*/

                    "Group" -> setGenderSpecificVisibility(View.VISIBLE, getString(R.string.setParents))
                    else -> setGenderSpecificVisibility(View.GONE, getString(R.string.entryMatedWith))
                    /*addMatingDateCal.setVisibility(View.VISIBLE);
                    addMatingDate.setVisibility(View.VISIBLE);
                    matingDateText.setVisibility(View.VISIBLE);*/
                }
            }

            override fun onNothingSelected(adapterView: AdapterView<*>) {}
        }

        addEntry.setOnClickListener {
            if (getMode == EDIT_EXISTING_ENTRY) {
                if (addEntryViewModel.matedDateChanged) {
                    createEvents(addEntryViewModel.entry.value!!)
                }

                addEntryViewModel.updateEntry().observeOnce(this, Observer {
                    if (addEntryViewModel.hasEntryPhotoChanged) {
                        addEntryViewModel.uploadImage(getFileName(addEntryViewModel.photoUri.value!!), addEntryViewModel.entryBitmap!!) { _ ->
                            if (it == "OK") {
                                val updatedEntry = Intent().putExtra("updatedEntry", addEntryViewModel.entry.value!!)
                                setResult(Activity.RESULT_OK, updatedEntry)
                                finish()
                            }
                        }
                    } else {
                        if (it == "OK") {
                            val updatedEntry = Intent().putExtra("updatedEntry", addEntryViewModel.entry.value!!)
                            setResult(Activity.RESULT_OK, updatedEntry)
                            finish()
                        }
                    }

                })
            } else {
                createEvents(addEntryViewModel.entry.value!!)
                addEntryViewModel.createNewEntry().observeOnce(this, Observer {
                    if (addEntryViewModel.photoUri.value != null) {
                        addEntryViewModel.uploadImage(getFileName(addEntryViewModel.photoUri.value!!), addEntryViewModel.entryBitmap!!) { _ ->
                            if (it == "OK") {
                                val addNewEntry = Intent().putExtra("addNewEntry", addEntryViewModel.entry.value!!)
                                setResult(Activity.RESULT_OK, addNewEntry)
                                finish()
                            }
                        }
                    } else {
                        if (it == "OK") {
                            val addNewEntry = Intent().putExtra("addNewEntry", addEntryViewModel.entry.value!!)
                            setResult(Activity.RESULT_OK, addNewEntry)
                            finish()
                        }
                    }


                })

            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SELECT_PHOTO && resultCode == Activity.RESULT_OK && data != null) {
            data.data?.getDownscaledBitmap(this)?.let{
                addEntryViewModel.entryBitmap = it
                addEntryViewModel.photoUri.value = data.data
                mainImage.load(data.data)
            }
        }
        else if(requestCode == REQUEST_TAKE_PHOTO && resultCode == Activity.RESULT_OK){
            addEntryViewModel.photoUri.value?.getDownscaledBitmap(this)?.let{
                addEntryViewModel.entryBitmap = it
                mainImage.load(addEntryViewModel.photoUri.value)
            }

        }


    }
    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also {
            it.resolveActivity(this.packageManager)?.also { _ ->
                // Create the File where the photo should go
                val photoFile = try {
                    createImageFile(this)
                } catch (ex: IOException) {
                    null
                }
                // Continue only if the File was successfully created
                photoFile?.also { file ->
                    val photoURI = FileProvider.getUriForFile(
                            this,
                            "com.example.kocja.rabbiter_online.fileprovider",
                            file
                    )
                    addEntryViewModel.photoUri.value = photoURI

                    it.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(it, REQUEST_TAKE_PHOTO)
                }
            }
        }
    }

    private fun createEvents(rabbitEntry: Entry) {
        val cal = Calendar.getInstance()

        if (rabbitEntry.chooseGender == getString(R.string.genderFemale)) {
            rabbitEntry.matedDate?.let {
                var upcomingBirth: Date = textFormatter.parse(it)

                var readyMateDate: Date?

                cal.time = upcomingBirth
                cal.add(Calendar.DAY_OF_YEAR, 31)
                upcomingBirth = cal.time

                readyMateDate = upcomingBirth
                cal.time = readyMateDate
                cal.add(Calendar.DAY_OF_YEAR, 66)
                readyMateDate = cal.time

                newEvent(getString(R.string.femaleGaveBirth, textFormatter.format(upcomingBirth), rabbitEntry.entryName), upcomingBirth, Events.BIRTH_EVENT)

                newEvent(getString(R.string.femaleReadyForMating, textFormatter.format(readyMateDate), rabbitEntry.entryName), readyMateDate, Events.READY_MATING_EVENT)
            }
        } else if (rabbitEntry.chooseGender == getString(R.string.genderGroup)) {
            rabbitEntry.birthDate?.let {
                var moveDate: Date = textFormatter.parse(it)

                cal.time = moveDate
                cal.add(Calendar.DAY_OF_YEAR, 62)
                moveDate = cal.time

                var slaughterDate: Date = textFormatter.parse(it)

                cal.time = slaughterDate
                cal.add(Calendar.DAY_OF_YEAR, 124)
                slaughterDate = cal.time

                newEvent(getString(R.string.groupMovedIntoCage, textFormatter.format(moveDate), rabbitEntry.entryName), moveDate, Events.MOVE_GROUP_EVENT)

                rabbitEntry.secondParent = parentSpinner.selectedItem.toString()

                newEvent(getString(R.string.groupSlauhtered, textFormatter.format(slaughterDate), rabbitEntry.entryName), slaughterDate, Events.SLAUGHTER_EVENT)
            }
        }
    }

    private fun newEvent(eventStr: String, eventDate: Date?, type: Int) {
        val uuid = UUID.randomUUID()
        val event = addEntryViewModel.createNewEvent(eventStr, textFormatter.format(eventDate), type, uuid)

        EventTriggered.scheduleWorkManager(this,eventDate!!.time,event.eventUUID)
        //NotifyUser.schedule(this, eventDate!!.time, event)
    }

    private fun setEditableEntryProps(getMode: Int) {

        if (getMode == EDIT_EXISTING_ENTRY) {
            addEntryViewModel.entry.value = intent.getParcelableExtra("entry") as Entry

            matedWithSpinner.setSelection(matedWithAdapter.getPosition(addEntryViewModel.entry.value?.matedWithOrParents))
            addGender.setSelection(genderAdapter.getPosition(addEntryViewModel.entry.value?.chooseGender))
            parentSpinner.setSelection(matedWithAdapter.getPosition(addEntryViewModel.entry.value?.secondParent))

            mainImage.load(addEntryViewModel.entry.value?.mergedEntryPhotoURL)
        } else if (getMode == EventTriggered.ADD_ENTRY_FROM_BIRTH) {
            addEntryViewModel.findEventByUUID(intent.getSerializableExtra("eventUUID") as String).observeOnce(this, Observer {
                (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).cancel(it.id)

                matedWithSpinner.setSelection(matedWithAdapter.getPosition(it.name))
                parentSpinner.setSelection(matedWithAdapter.getPosition(it.secondParent))

                val processEventsIntent = Intent(this, ProcessService::class.java)
                        .putExtra("processEventUUID", it.eventUUID)
                        .putExtra("happened", intent.getBooleanExtra("happened", false))
                startService(processEventsIntent)
            })
        }
    }

    private fun getFileName(uri: Uri): String {
        var result = ""
        if (uri.scheme == "content") {
            val cursor = contentResolver.query(uri, null, null, null, null)
            cursor.use { cursor1 ->
                if (cursor1 != null && cursor1.moveToFirst()) {
                    result = cursor1.getString(cursor1.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                }
            }
        }
        return result
    }


    private fun setGenderSpecificVisibility(visibility: Int, text: String) {
        matedWith.text = text
        parentSpinner.visibility = visibility
        NumRabbits.visibility = visibility
        deadRabbits.visibility = visibility
        deadNumTextTitle.visibility = visibility
        rabbitsNumText.visibility = visibility
    }

    @Throws(IOException::class)
    private fun createImageFile(mainC: Context): File {
        // Create an image file name
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "JPEG_" + timeStamp + '_'.toString()
        val storageDir = mainC.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
                imageFileName, /* prefix */
                ".jpg", /* suffix */
                storageDir)
    }

    companion object {
        private const val REQUEST_TAKE_PHOTO = 0
        private const val SELECT_PHOTO = 1
        const val EDIT_EXISTING_ENTRY = 2
    }
}
