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
import android.provider.OpenableColumns

import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import android.view.View
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.lifecycle.lifecycleScope
import coil.load

import com.example.kocja.rabbiter_online.R
import com.example.kocja.rabbiter_online.databinding.ActivityAddEntryBinding
import com.example.kocja.rabbiter_online.extensions.getDownscaledBitmap
import com.example.kocja.rabbiter_online.models.Entry
import com.example.kocja.rabbiter_online.models.Events
import com.example.kocja.rabbiter_online.services.EventTriggered
import com.example.kocja.rabbiter_online.services.ProcessService
import com.example.kocja.rabbiter_online.viewmodels.AddEntryViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel

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
    private val textFormatter: SimpleDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private lateinit var matedWithAdapter: ArrayAdapter<String>
    private lateinit var genderAdapter: ArrayAdapter<String>
    private val addEntryViewModel: AddEntryViewModel by viewModel()
    private lateinit var binding: ActivityAddEntryBinding
    fun setValues(){
        when (binding.addGender.selectedItem.toString()) {
            getString(R.string.genderMale) -> setMaleFemaleSpecificVisibility(View.GONE,R.id.addBirthDate)
            "Group" -> setGenderSpecificVisibility(View.VISIBLE, getString(R.string.setParents))
            else -> setMaleFemaleSpecificVisibility(View.VISIBLE,R.id.addMatingDate)
        }
    }
    private fun setGenderSpecificVisibility(visibility: Int, text: String) {
        with(binding){
            matedWith.text = text
            parentSpinner.visibility = visibility
            NumRabbits.visibility = visibility
            deadRabbits.visibility = visibility
            deadNumTextTitle.visibility = visibility
            rabbitsNumText.visibility = visibility
        }
    }
    private fun setMaleFemaleSpecificVisibility(visibility: Int,bottomConstraint: Int){
        setGenderSpecificVisibility(View.GONE, getString(R.string.entryMatedWith))

        binding.addMatingDate.visibility = visibility
        binding.addMatingDateCal.visibility = visibility
        binding.matingDate.visibility = visibility

        binding.constraintLayout.setConstraints(R.id.matedWith,ConstraintSet.TOP,bottomConstraint,ConstraintSet.BOTTOM)
    }

    //NOTE: type 0: birth
    //NOTE: type 1: ready for mating
    //NOTE: type 2: move group
    //NOTE: type 3: slaughter date

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTitle(R.string.title)

        binding = ActivityAddEntryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val getMode = intent.getIntExtra("getMode", -1)

        binding.takePhoto.setOnClickListener {
            val chooseMethod = AlertDialog.Builder(this)
                    .setTitle(R.string.photoOption)
                    .setItems(R.array.DecideOnPhType) { _, i ->
                        if (i == 0) {
                            val selectPhotoResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ activity ->
                                activity.data?.data?.getDownscaledBitmap(this)?.let{
                                    addEntryViewModel.entryBitmap = it
                                    addEntryViewModel.photoUri.value = activity.data?.data
                                    binding.mainImage.load(activity.data?.data)
                                }
                            }
                            val photoPickerIntent = Intent(Intent.ACTION_GET_CONTENT)
                            photoPickerIntent.addCategory(Intent.CATEGORY_OPENABLE)
                            photoPickerIntent.type = "image/*"
                            selectPhotoResult.launch(photoPickerIntent)
                        } else {
                            dispatchTakePictureIntent()
                        }
                    }
            chooseMethod.show()
        }

        with(addEntryViewModel){
            entry = Entry(UUID.randomUUID().toString())
            entryBitmap = null

            photoUri.observe(this@AddEntryActivity) {
                hasEntryPhotoChanged = true
                setUriSpecificValues(getFileName(it))
            }
        }

        lifecycleScope.launch{
            val result = addEntryViewModel.getAllEntries()
            val allEntryNames = mutableListOf(getString(R.string.none))
            allEntryNames.addAll(result.map {it.entryName })

            withContext(Dispatchers.Main){
                matedWithAdapter = ArrayAdapter(this@AddEntryActivity, android.R.layout.simple_spinner_dropdown_item, allEntryNames)
                binding.matedWithSpinner.adapter = matedWithAdapter
                binding.parentSpinner.adapter = matedWithAdapter
                setEditableEntryProps(getMode)
            }
        }

        //set today date for a datePickerDialog and set listeners
        val calendar = Calendar.getInstance()
        val pickDate = DatePickerDialog(this, { _, year, month, dayOfMonth ->
            val formattedText = textFormatter.format(GregorianCalendar(year,month,dayOfMonth).time)
            if (takeBirthDateCal) {
                binding.addBirthDate.setText(formattedText)
            } else {
                if (addEntryViewModel.entry?.matedDate != formattedText) {
                    addEntryViewModel.matedDateChanged = true
                }
                binding.addMatingDate.setText(formattedText)
            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))

        binding.addBirthDateCal.setOnClickListener {
            takeBirthDateCal = true
            pickDate.show()
        }

        binding.addMatingDateCal.setOnClickListener {
            takeBirthDateCal = false
            pickDate.show()
        }

        genderAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, resources.getStringArray(R.array.decideOnGender))
        binding.addGender.adapter = genderAdapter
        binding.addEntry.setOnClickListener {
            if (getMode == EDIT_EXISTING_ENTRY) {
                if (addEntryViewModel.matedDateChanged) {
                    createEvents(addEntryViewModel.entry!!)
                }
                lifecycleScope.launch(Dispatchers.IO) {
                    val updateResult = addEntryViewModel.updateEntry(getFileName(addEntryViewModel.photoUri.value ?: Uri.EMPTY))
                    withContext(Dispatchers.Main) {
                        if (updateResult == "OK") {
                            val updatedEntry = Intent().putExtra("updatedEntry", addEntryViewModel.entry)
                            setResult(Activity.RESULT_OK, updatedEntry)
                            finish()
                        }
                    }
                }
            } else {
                createEvents(addEntryViewModel.entry!!)
                lifecycleScope.launch(Dispatchers.IO){
                    val result = addEntryViewModel.createNewEntry(getFileName(addEntryViewModel.photoUri.value ?: Uri.EMPTY))
                    withContext(Dispatchers.Main){
                        if (result == "OK") {
                            val addNewEntry = Intent().putExtra("addNewEntry", addEntryViewModel.entry)
                            setResult(Activity.RESULT_OK, addNewEntry)
                            finish()
                        }
                    }
                }

            }
        }
    }

    private fun dispatchTakePictureIntent() {
        val requestTakePhotoResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            addEntryViewModel.photoUri.value?.getDownscaledBitmap(this)?.let{
                addEntryViewModel.entryBitmap = it
                binding.mainImage.load(addEntryViewModel.photoUri.value)
            }
        }
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).let {
            it.resolveActivity(this.packageManager)?.let { _ ->
                // Create the File where the photo should go
                val photoFile = try {
                    createImageFile(this)
                } catch (ex: IOException) {
                    null
                }
                // Continue only if the File was successfully created
                photoFile?.let { file ->
                    val photoURI = FileProvider.getUriForFile(
                            this,
                            "com.example.kocja.rabbiter_online.fileprovider",
                            file
                    )
                    addEntryViewModel.photoUri.value = photoURI

                    it.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    requestTakePhotoResult.launch(it)
                }
            }
        }
    }

    private fun createEvents(rabbitEntry: Entry) {
        val cal = Calendar.getInstance()

        if (rabbitEntry.chooseGender == getString(R.string.genderFemale)) {
            rabbitEntry.matedDate?.let {
                lifecycleScope.launch(Dispatchers.Default){
                    var upcomingBirth: Date = textFormatter.parse(it)!!

                    cal.time = upcomingBirth
                    cal.add(Calendar.DAY_OF_YEAR, 31)
                    upcomingBirth = cal.time

                    var readyMateDate: Date = upcomingBirth
                    cal.time = readyMateDate
                    cal.add(Calendar.DAY_OF_YEAR, 66)
                    readyMateDate = cal.time

                    newEvent(getString(R.string.femaleGaveBirth, textFormatter.format(upcomingBirth), rabbitEntry.entryName), upcomingBirth, Events.BIRTH_EVENT)
                    newEvent(getString(R.string.femaleReadyForMating, textFormatter.format(readyMateDate), rabbitEntry.entryName), readyMateDate, Events.READY_MATING_EVENT)
                }

            }
        } else if (rabbitEntry.chooseGender == getString(R.string.genderGroup)) {
            rabbitEntry.birthDate?.let {
                lifecycleScope.launch(Dispatchers.Default){
                    var moveDate: Date = textFormatter.parse(it)!!

                    cal.time = moveDate
                    cal.add(Calendar.DAY_OF_YEAR, 62)
                    moveDate = cal.time

                    var slaughterDate: Date = textFormatter.parse(it)!!

                    cal.time = slaughterDate
                    cal.add(Calendar.DAY_OF_YEAR, 124)
                    slaughterDate = cal.time

                    newEvent(getString(R.string.groupMovedIntoCage, textFormatter.format(moveDate), rabbitEntry.entryName), moveDate, Events.MOVE_GROUP_EVENT)

                    rabbitEntry.secondParent = binding.parentSpinner.selectedItem.toString()

                    newEvent(getString(R.string.groupSlauhtered, textFormatter.format(slaughterDate), rabbitEntry.entryName), slaughterDate, Events.SLAUGHTER_EVENT)
                }

            }
        }
    }

    private fun newEvent(eventStr: String, eventDate: Date?, type: Int) {
        val uuid = UUID.randomUUID()
        val event = addEntryViewModel.createNewEvent(eventStr, textFormatter.format(eventDate!!), type, uuid)

        EventTriggered.scheduleWorkManager(this,eventDate.time,event.eventUUID)
    }

    private fun setEditableEntryProps(getMode: Int) {
        if (getMode == EDIT_EXISTING_ENTRY) {
            addEntryViewModel.entry = intent.getParcelableExtra("entry")!!

            addEntryViewModel.entry?.let{
                binding.matedWithSpinner.setSelection(matedWithAdapter.getPosition(it.matedWithOrParents))
                binding.addGender.setSelection(genderAdapter.getPosition(it.chooseGender))
                binding.parentSpinner.setSelection(matedWithAdapter.getPosition(it.secondParent))
            }

            binding.mainImage.load(addEntryViewModel.entry?.mergedEntryPhotoURL)
        } else if (getMode == EventTriggered.ADD_ENTRY_FROM_BIRTH) {
            lifecycleScope.launch(Dispatchers.IO) {
                val result = addEntryViewModel.findEventByUUID(intent.getSerializableExtra("eventUUID") as String)
                (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).cancel(result.id)

                withContext(Dispatchers.Main){
                    binding.matedWithSpinner.setSelection(matedWithAdapter.getPosition(result.name))
                    binding.parentSpinner.setSelection(matedWithAdapter.getPosition(result.secondParent))

                    val processEventsIntent = Intent(this@AddEntryActivity, ProcessService::class.java)
                            .putExtra("processEventUUID", result.eventUUID)
                            .putExtra("happened", intent.getBooleanExtra("happened", false))
                    startService(processEventsIntent)
                }
            }
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






    companion object {
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

        const val EDIT_EXISTING_ENTRY = 2
    }

    private fun ConstraintLayout.setConstraints(first : Int, toFirst : Int, second : Int, toSecond : Int){
        ConstraintSet().run {
            clone(this@setConstraints)
            connect(first, toFirst, second, toSecond)
            applyTo(this@setConstraints)
        }
    }
}
