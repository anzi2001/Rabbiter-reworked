package com.example.kocja.rabbiter_online.activities

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore

import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer

import com.bumptech.glide.Glide
import com.example.kocja.rabbiter_online.R
import com.example.kocja.rabbiter_online.databinding.ActivityAddEntryBinding
import com.example.kocja.rabbiter_online.extensions.observeOnce
import com.example.kocja.rabbiter_online.models.Entry
import com.example.kocja.rabbiter_online.models.Events
import com.example.kocja.rabbiter_online.services.NotifyUser
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

/**
 * Created by kocja on 21/01/2018.
 */

class AddEntryActivity : AppCompatActivity() {

    private var takeBirthDateCal = false
    private val parseFormatter: SimpleDateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm:ss a", Locale.getDefault())
    private val textFormatter: SimpleDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private lateinit var matedWithAdapter: ArrayAdapter<String>
    private lateinit var genderAdapter: ArrayAdapter<String>
    private var latestImage: File? = null
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
                            val photoPickerIntent = Intent(Intent.ACTION_PICK)
                            photoPickerIntent.type = "image/*"
                            startActivityForResult(photoPickerIntent, SELECT_PHOTO)
                        } else {
                            addEntryViewModel.entry.value?.entryPhLoc = dispatchTakePictureIntent().toString()
                        }
                    }
            chooseMethod.show()

        }

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
                addBirthDate.setText(parseFormatter.format(GregorianCalendar(year, month, dayOfMonth).time))
            } else {
                addMatingDate.setText(parseFormatter.format(GregorianCalendar(year, month, dayOfMonth).time))
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
                if (addEntryViewModel.entry.value!!.matedDateChanged) {
                    createEvents(addEntryViewModel.entry.value!!)
                }

                addEntryViewModel.updateEntry().observeOnce(this, Observer {
                    if (it == "OK") {
                        setResult(Activity.RESULT_OK)
                        finish()
                    }
                })
            } else {
                createEvents(addEntryViewModel.entry.value!!)
                addEntryViewModel.createNewEntry().observeOnce(this, Observer {
                    if (it == "OK") {
                        setResult(Activity.RESULT_OK)
                        finish()
                    }
                })

            }
        }

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SELECT_PHOTO && resultCode == Activity.RESULT_OK && data != null) {
            addEntryViewModel.entry.value?.entryPhLoc = data.data!!.toString()
            val realPath = getRealPathContentUri(data.data)
            latestImage = File(realPath)
            Glide.with(this).load(data.data).into(mainImage)

        }
    }

    private fun dispatchTakePictureIntent(): Uri? {

        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        var photoURI: Uri? = null
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(this.packageManager) != null) {
            // Create the File where the photo should go
            var photoFile: File? = null
            try {
                photoFile = createImageFile(this)
            } catch (ex: IOException) {
                ex.printStackTrace()
            }

            // Continue only if the File was successfully created
            photoFile?.let {
                latestImage = it
                photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.photoprovider12",
                        it)
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                this.startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO)
            }
            Glide.with(this).load(photoURI).into(mainImage)
        }
        return photoURI
    }

    private fun createEvents(rabbitEntry: Entry) {
        val cal = Calendar.getInstance()

        if (rabbitEntry.chooseGender == getString(R.string.genderFemale)) {
            rabbitEntry.matedDate?.let {
                var upcomingBirth: Date = parseFormatter.parse(it)

                var readyMateDate: Date?

                cal.time = upcomingBirth
                cal.add(Calendar.DAY_OF_YEAR, 31)
                upcomingBirth = cal.time

                readyMateDate = upcomingBirth
                cal.time = readyMateDate
                cal.add(Calendar.DAY_OF_YEAR, 66)
                readyMateDate = cal.time

                newEvent(getString(R.string.femaleGaveBirth, parseFormatter.format(upcomingBirth), rabbitEntry.entryName), upcomingBirth, Events.BIRTH_EVENT)

                newEvent(getString(R.string.femaleReadyForMating, parseFormatter.format(readyMateDate), rabbitEntry.entryName), readyMateDate, Events.READY_MATING_EVENT)
            }
        } else if (rabbitEntry.chooseGender == getString(R.string.genderGroup)) {
            rabbitEntry.birthDate?.let {
                var moveDate: Date = parseFormatter.parse(it)


                var slaughterDate = moveDate

                cal.time = moveDate
                cal.add(Calendar.DAY_OF_YEAR, 62)
                moveDate = cal.time

                cal.time = slaughterDate
                cal.add(Calendar.DAY_OF_YEAR, 124)
                slaughterDate = cal.time

                newEvent(getString(R.string.groupMovedIntoCage, parseFormatter.format(moveDate), rabbitEntry.entryName), moveDate, Events.MOVE_GROUP_EVENT)

                rabbitEntry.secondParent = parentSpinner.selectedItem.toString()

                newEvent(getString(R.string.groupSlauhtered, parseFormatter.format(slaughterDate), rabbitEntry.entryName), slaughterDate, Events.SLAUGHTER_EVENT)
            }
        }
    }

    private fun newEvent(eventStr: String, eventDate: Date?, type: Int) {
        val uuid = UUID.randomUUID()
        addEntryViewModel.createNewEvent(eventStr, parseFormatter.format(eventDate), type, uuid)

        NotifyUser.schedule(this, eventDate!!.time, uuid.toString())
    }

    private fun setEditableEntryProps(getMode: Int) {

        if (getMode == EDIT_EXISTING_ENTRY) {
            val entryUUID = intent.getSerializableExtra("entryEdit") as UUID
            addEntryViewModel.setExistingEntryProperties(entryUUID.toString()).observeOnce(this, Observer { entry ->
                addEntryViewModel.entry.value = entry

                matedWithSpinner.setSelection(matedWithAdapter.getPosition(addEntryViewModel.entry.value?.matedWithOrParents))
                addGender.setSelection(genderAdapter.getPosition(addEntryViewModel.entry.value?.chooseGender))
                parentSpinner.setSelection(matedWithAdapter.getPosition(addEntryViewModel.entry.value?.secondParent))

                Glide.with(this@AddEntryActivity).load(addEntryViewModel.entry.value?.mergedEntryPhLoc).into(mainImage)

                addEntryViewModel.entry.value?.birthDate?.let {
                    addBirthDate.setText(textFormatter.format(it))
                }
                addEntryViewModel.entry.value?.matedDate?.let {
                    addMatingDate.setText(textFormatter.format(it))
                }
            })
        } else if (getMode == NotifyUser.ADD_ENTRY_FROM_BIRTH) {
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

    private fun getRealPathContentUri(contentUri: Uri?): String {
        val images = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = contentResolver.query(contentUri!!, images, null, null, null)
        cursor!!.moveToFirst()
        val columnIndex = cursor.getColumnIndex(images[0])
        val path = cursor.getString(columnIndex)
        cursor.close()
        return path

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
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.GERMANY).format(Date())
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
