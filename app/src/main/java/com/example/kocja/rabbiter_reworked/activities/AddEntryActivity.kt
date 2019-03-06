package com.example.kocja.rabbiter_reworked.activities

import android.app.*
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.DatePicker
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintSet

import com.bumptech.glide.Glide
import com.example.kocja.rabbiter_reworked.R
import com.example.kocja.rabbiter_reworked.databases.*
import com.example.kocja.rabbiter_reworked.services.AlertEventService
import com.example.kocja.rabbiter_reworked.services.processEvents
import com.raizlabs.android.dbflow.config.FlowManager
import com.raizlabs.android.dbflow.sql.language.SQLite
import kotlinx.android.synthetic.main.activity_add_entry.*

import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar
import java.util.Locale
import java.util.Random
import java.util.UUID

/**
 * Created by kocja on 21/01/2018.
 */

class AddEntryActivity : AppCompatActivity(), DatePickerDialog.OnDateSetListener {

    private var takeBirthDateCal = false
    private val defaultFormatter: SimpleDateFormat = SimpleDateFormat("dd/MM/yyyy",Locale.GERMANY)
    private var birthDate: Date? = null
    private var matingDateDate: Date? = null
    private var lastDate: Date? = null
    private var baseImageUri: Uri? = null
    private var editable: Entry? = null
    private var matedWithAdapter: ArrayAdapter<String>? = null
    private var genderAdapter: ArrayAdapter<String>? = null
    private var lastGender: String? = null
    private lateinit var birthDateEvent : com.example.kocja.rabbiter_reworked.databases.Events

    //NOTE: type 0: birth
    //NOTE: type 1: ready for mating
    //NOTE: type 2: move group
    //NOTE: type 3: slaughter date

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_entry)
        setTitle(R.string.title)


        val getMode = intent.getIntExtra("getMode", -1)

        takePhoto.setOnClickListener {
            val chooseMethod = AlertDialog.Builder(this)
                    .setTitle(R.string.photoOption)
                    .setItems(R.array.DecideOnPhType) { _, i ->
                        if (i == 0) {
                            val photoPickerIntent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                            photoPickerIntent.type = "image/*"
                            startActivityForResult(photoPickerIntent, SELECT_PHOTO)
                        } else {
                            baseImageUri = dispatchTakePictureIntent()
                        }
                    }
            chooseMethod.show()
        }

        SQLite.select()
                .from(Entry::class.java)
                .async()
                .queryListResultCallback { _, tResult ->
                    val allEntryNames = ArrayList<String>(tResult.size)
                    allEntryNames.add(getString(R.string.none))
                    for (entry in tResult) {
                        allEntryNames.add(entry.entryName)
                    }
                    matedWithAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, allEntryNames)
                    matedWithSpinner!!.adapter = matedWithAdapter
                    parentSpinner!!.adapter = matedWithAdapter
                    setEditableEntryProps(getMode)
                }.execute()

        val c = Calendar.getInstance()
        val pickDate = DatePickerDialog(this, this@AddEntryActivity, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH))

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
                when(addGender.selectedItem.toString()) {
                    getString(R.string.genderMale) -> {
                        parentSpinner!!.visibility = View.GONE
                        matedWith.text = getString(R.string.entryMatedWith)

                        NumRabbits.visibility = View.GONE
                        deadRabbits.visibility = View.GONE
                        deadNumTextTitle.visibility = View.GONE
                        rabbitsNumText.visibility = View.GONE

                        //Set these to gone, reConstraint views below these to gender
                        addMatingDate!!.visibility = View.GONE
                        addMatingDateCal.visibility = View.GONE
                        matingDate.visibility = View.GONE

                        val set = ConstraintSet()
                        set.clone(layout)
                        set.connect(R.id.matedWith, ConstraintSet.TOP, R.id.addBirthDate, ConstraintSet.BOTTOM)

                        set.applyTo(layout)
                    }
                    "Group" -> {
                        matedWith.text = getString(R.string.setParents)
                        parentSpinner!!.visibility = View.VISIBLE
                        NumRabbits.visibility = View.VISIBLE
                        deadRabbits.visibility = View.VISIBLE
                        deadNumTextTitle.visibility = View.VISIBLE
                        rabbitsNumText.visibility = View.VISIBLE
                    }
                    else -> {
                        parentSpinner!!.visibility = View.GONE
                        matedWith.text = getString(R.string.entryMatedWith)

                        NumRabbits.visibility = View.GONE
                        deadRabbits.visibility = View.GONE
                        deadNumTextTitle.visibility = View.GONE
                        rabbitsNumText.visibility = View.GONE

                        addMatingDateCal.visibility = View.VISIBLE
                        addMatingDate!!.visibility = View.VISIBLE
                        matingDate.visibility = View.VISIBLE

                        val set = ConstraintSet()
                        set.clone(layout)
                        set.connect(R.id.matedWith, ConstraintSet.TOP, R.id.addMatingDate, ConstraintSet.BOTTOM)

                        set.applyTo(layout)
                    }
                }
            }

            override fun onNothingSelected(adapterView: AdapterView<*>) {}
        }

        addEntry.setOnClickListener {
            val database = FlowManager.getDatabase(appDatabase::class.java)
            val transaction = database.beginTransactionAsync { databaseWrapper ->
                if (getMode == EDIT_EXISTING_ENTRY) {
                    if (matingDateDate !== editable!!.matedDate && matingDateDate != null) {
                        SQLite.delete()
                                .from(Events::class.java)
                                .orderBy(Events_Table.id,false)
                                .limit(2)
                        Events.create(editable!!,this)
                    }

                    editable!!.entryName = addName!!.text.toString()
                    editable!!.chooseGender = addGender.selectedItem.toString()
                    editable!!.matedWithOrParents = matedWithSpinner!!.selectedItem.toString()
                    if (parentSpinner!!.visibility == View.VISIBLE) {
                        editable!!.secondParent = parentSpinner!!.selectedItem.toString()
                    }

                    editable!!.birthDate = birthDate
                    editable!!.matedDate = matingDateDate

                    editable!!.rabbitNumber = Integer.parseInt("0"+NumRabbits.text.toString())
                    editable!!.rabbitDeadNumber = Integer.parseInt("0"+deadRabbits.text.toString())

                    if (baseImageUri != null) {
                        editable!!.entryPhLoc = baseImageUri!!.toString()
                    }

                    editable!!.update()
                }
                else {
                    val rabbitEntry = Entry()

                    rabbitEntry.entryID = UUID.randomUUID()
                    rabbitEntry.entryName = addName!!.text.toString()
                    if (baseImageUri != null) {
                        rabbitEntry.entryPhLoc = baseImageUri!!.toString()
                    }
                    rabbitEntry.chooseGender = addGender.selectedItem.toString()
                    rabbitEntry.matedWithOrParents = matedWithSpinner!!.selectedItem.toString()

                    rabbitEntry.birthDate = birthDate
                    rabbitEntry.matedDate = matingDateDate

                    rabbitEntry.rabbitNumber = Integer.parseInt('0'+NumRabbits.text.toString())
                    rabbitEntry.rabbitDeadNumber = Integer.parseInt('0'+deadRabbits.text.toString())

                    Events.create(rabbitEntry,this)
                    rabbitEntry.secondParent = parentSpinner!!.selectedItem.toString()

                    rabbitEntry.save(databaseWrapper)

                    if(getMode == AlertEventService.ADD_BIRTH_FROM_SERVICE){
                        val processEventsIntent = Intent(this, processEvents::class.java)
                        processEventsIntent.putExtra("processEventUUID", birthDateEvent.eventUUID)
                        processEventsIntent.putExtra("groupName",rabbitEntry.entryName)
                        processEventsIntent.putExtra("groupBirthDate",defaultFormatter.format(rabbitEntry.birthDate))
                        processEventsIntent.putExtra("happened", intent.getBooleanExtra("happened", false))
                        startService(processEventsIntent)
                    }
                }
            }.build()
            transaction.execute()
            setResult(Activity.RESULT_OK)
            finish()
        }

    }

    override fun onDateSet(datePicker: DatePicker, year: Int, month: Int, dayOfMonth: Int) {
        if (takeBirthDateCal) {
            birthDate = GregorianCalendar(year, month, dayOfMonth).time
            addBirthDate!!.setText(defaultFormatter.format(birthDate))
        } else {
            matingDateDate = GregorianCalendar(year, month, dayOfMonth).time
            addMatingDate!!.setText(defaultFormatter.format(matingDateDate))
        }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SELECT_PHOTO && resultCode == Activity.RESULT_OK && data != null) {
            baseImageUri = data.data
            Glide.with(this).load(baseImageUri).into(mainImage!!)

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
                val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
                dialog.setTitle("Error occurred")
                dialog.show()
            }

            // Continue only if the File was successfully created
            if (photoFile != null) {
                photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.photoprovider",
                        photoFile)
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                this.startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO)
            }
            Glide.with(this).load(photoURI).into(mainImage)
        }
        return photoURI
    }


    private fun setEditableEntryProps(getMode: Int) {
        if (getMode == EDIT_EXISTING_ENTRY) {
            val entryUUID = intent.getSerializableExtra("entryEdit") as UUID
            SQLite.select()
                    .from(Entry::class.java)
                    .where(Entry_Table.entryID.eq(entryUUID))
                    .async()
                    .querySingleResultCallback { _, editable ->
                        this.editable = editable
                        lastGender = editable?.chooseGender
                        addName!!.setText(editable?.entryName)
                        matedWithSpinner!!.setSelection(matedWithAdapter!!.getPosition(editable?.matedWithOrParents))
                        addGender.setSelection(genderAdapter!!.getPosition(editable?.chooseGender))
                        parentSpinner!!.setSelection(matedWithAdapter!!.getPosition(editable?.secondParent))

                        //i believe i can just set rabbitsNum and deadRabbits since it hides anyway
                        NumRabbits.setText(editable?.rabbitNumber.toString())
                        deadRabbits.setText(editable?.rabbitDeadNumber.toString())

                        if (editable?.birthDate != null) {
                            birthDate = editable.birthDate
                            addBirthDate!!.setText(defaultFormatter.format(editable.birthDate))
                        }
                        if (editable?.matedDate != null) {
                            matingDateDate = editable.matedDate
                            lastDate = editable.matedDate
                            addMatingDate!!.setText(defaultFormatter.format(editable.matedDate))
                        }
                        Glide.with(this@AddEntryActivity).load(editable?.entryPhLoc).into(mainImage)
                    }.execute()
        } else if (getMode == AlertEventService.ADD_BIRTH_FROM_SERVICE) {
            val intent = intent
            addGender.setSelection(2)
            SQLite.select()
                    .from(Events::class.java)
                    .where(Events_Table.eventUUID.eq(intent.getSerializableExtra("eventUUID") as Int))
                    .async()
                    .querySingleResultCallback { _, events ->
                        birthDateEvent = events!!
                        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                        manager.cancel(events.id)
                        matedWithSpinner!!.setSelection(matedWithAdapter!!.getPosition(events.name))
                        parentSpinner!!.setSelection(matedWithAdapter!!.getPosition(events.secondParent))
                        val processEventsIntent = Intent(this, processEvents::class.java)
                        processEventsIntent.putExtra("processEventUUID", events.eventUUID)
                        processEventsIntent.putExtra("happened", intent.getBooleanExtra("happened", false))
                        startService(processEventsIntent)
                    }.execute()
        }
    }

    companion object {
        private const val REQUEST_TAKE_PHOTO = 0
        private const val SELECT_PHOTO = 1
        const val EDIT_EXISTING_ENTRY = 2

        const val birthEvent = 0
        const val readyMatingEvent = 1
        const val moveGroupEvent = 2
        const val slaughterEvent = 3

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
    }
}