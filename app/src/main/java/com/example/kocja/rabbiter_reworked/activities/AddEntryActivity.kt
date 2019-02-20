package com.example.kocja.rabbiter_reworked.activities

import android.app.*
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.constraint.ConstraintLayout
import android.support.constraint.ConstraintSet
import android.support.v4.content.FileProvider
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.DatePicker
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView

import com.bumptech.glide.Glide
import com.example.kocja.rabbiter_reworked.R
import com.example.kocja.rabbiter_reworked.broadcastrecievers.NotifReciever
import com.example.kocja.rabbiter_reworked.databases.Entry
import com.example.kocja.rabbiter_reworked.databases.Entry_Table
import com.example.kocja.rabbiter_reworked.databases.Events
import com.example.kocja.rabbiter_reworked.databases.Events_Table
import com.example.kocja.rabbiter_reworked.databases.appDatabase
import com.example.kocja.rabbiter_reworked.services.AlertEventService
import com.example.kocja.rabbiter_reworked.services.processEvents
import com.raizlabs.android.dbflow.config.FlowManager
import com.raizlabs.android.dbflow.sql.language.SQLite

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
    private var defaultFormatter: SimpleDateFormat? = null
    private var addBirthDate: EditText? = null
    private var addMatingDate: EditText? = null
    private var addName: EditText? = null
    private var baseImage: ImageView? = null
    private var matedWithSpinner: Spinner? = null
    private var genderSpinner: Spinner? = null
    private var birthDate: Date? = null
    private var matingDate: Date? = null
    private var lastDate: Date? = null
    private var baseImageUri: Uri? = null
    private var editable: Entry? = null
    private var matedWithAdapter: ArrayAdapter<String>? = null
    private var genderAdapter: ArrayAdapter<String>? = null
    private var lastGender: String? = null
    private var parentSpinner: Spinner? = null
    private var rabbitsNum: EditText? = null
    private var deadRabbitNum: EditText? = null
    private var eventsManager: AlarmManager? = null
    private val randGen = Random()

    //NOTE: type 0: birth
    //NOTE: type 1: ready for mating
    //NOTE: type 2: move group
    //NOTE: type 3: slaughter date

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_entry)
        setTitle(R.string.title)

        defaultFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.GERMANY)
        addBirthDate = findViewById(R.id.addBirthDate)
        addMatingDate = findViewById(R.id.addMatingDate)
        baseImage = findViewById(R.id.mainImage)
        genderSpinner = findViewById(R.id.addGender)
        matedWithSpinner = findViewById(R.id.matedWithSpinner)
        addName = findViewById(R.id.addName)
        rabbitsNum = findViewById(R.id.NumRabbits)
        deadRabbitNum = findViewById(R.id.deadRabbits)
        parentSpinner = findViewById(R.id.parentSpinner)

        val addPhoto = findViewById<ImageButton>(R.id.takePhoto)
        val matedWith = findViewById<TextView>(R.id.matedWith)
        val addBirthDateCal = findViewById<ImageButton>(R.id.addBirthDateCal)
        val addMatingDateCal = findViewById<ImageButton>(R.id.addMatingDateCal)
        val addEntry = findViewById<ImageButton>(R.id.addEntry)

        val getMode = intent.getIntExtra("getMode", -1)

        addPhoto.setOnClickListener {
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
        genderSpinner!!.adapter = genderAdapter
        val numDeadRabTitle = findViewById<TextView>(R.id.deadNumTextTitle)
        val rabbitsNumText = findViewById<TextView>(R.id.rabbitsNumText)
        val matingDateText = findViewById<TextView>(R.id.matingDate)
        val layout = findViewById<ConstraintLayout>(R.id.layout)
        genderSpinner!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>, view: View, i: Int, l: Long) {
                when {
                    genderSpinner!!.selectedItem.toString() == getString(R.string.genderMale) -> {
                        parentSpinner!!.visibility = View.GONE
                        matedWith.text = getString(R.string.entryMatedWith)

                        rabbitsNum!!.visibility = View.GONE
                        deadRabbitNum!!.visibility = View.GONE
                        numDeadRabTitle.visibility = View.GONE
                        rabbitsNumText.visibility = View.GONE

                        //Set these to gone, reconstraint views below these to gender
                        addMatingDate!!.visibility = View.GONE
                        addMatingDateCal.visibility = View.GONE
                        matingDateText.visibility = View.GONE

                        val set = ConstraintSet()
                        set.clone(layout)
                        set.connect(R.id.matedWith, ConstraintSet.TOP, R.id.addBirthDate, ConstraintSet.BOTTOM)

                        set.applyTo(layout)
                    }
                    genderSpinner!!.selectedItem.toString() == "Group" -> {
                        matedWith.text = getString(R.string.setParents)
                        parentSpinner!!.visibility = View.VISIBLE
                        rabbitsNum!!.visibility = View.VISIBLE
                        deadRabbitNum!!.visibility = View.VISIBLE
                        numDeadRabTitle.visibility = View.VISIBLE
                        rabbitsNumText.visibility = View.VISIBLE
                    }
                    else -> {
                        parentSpinner!!.visibility = View.GONE
                        matedWith.text = getString(R.string.entryMatedWith)

                        rabbitsNum!!.visibility = View.GONE
                        deadRabbitNum!!.visibility = View.GONE
                        numDeadRabTitle.visibility = View.GONE
                        rabbitsNumText.visibility = View.GONE

                        addMatingDateCal.visibility = View.VISIBLE
                        addMatingDate!!.visibility = View.VISIBLE
                        matingDateText.visibility = View.VISIBLE

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
                    val lastMateDate = editable!!.matedDate
                    if (matingDate !== editable!!.matedDate && matingDate != null) {
                        createEvents(editable!!)
                    }

                    editable!!.entryName = addName!!.text.toString()
                    editable!!.chooseGender = genderSpinner!!.selectedItem.toString()
                    editable!!.matedWithOrParents = matedWithSpinner!!.selectedItem.toString()
                    if (parentSpinner!!.visibility == View.VISIBLE) {
                        editable!!.secondParent = parentSpinner!!.selectedItem.toString()
                    }

                    editable!!.birthDate = birthDate
                    editable!!.matedDate = matingDate

                    if (!rabbitsNum!!.text.toString().isEmpty()) {
                        editable!!.rabbitNumber = Integer.parseInt(rabbitsNum!!.text.toString())

                    }
                    if (!deadRabbitNum!!.text.toString().isEmpty()) {
                        editable!!.rabbitDeadNumber = Integer.parseInt(deadRabbitNum!!.text.toString())
                    }

                    if (lastMateDate !== matingDate) {
                        createEvents(editable!!)
                    }
                    if (baseImageUri != null) {
                        editable!!.entryPhLoc = baseImageUri!!.toString()
                    }

                    // i check if the date is not the same, initialize new events based on those dates
                    // i do the same if the user is changing the gender from male to female or group
                    if (lastDate !== matingDate || lastGender == getString(R.string.genderMale) && editable!!.chooseGender != getString(R.string.genderMale)) {
                        editable!!.matedDate = matingDate
                        createEvents(editable!!)
                    }


                    editable!!.update()
                } else {
                    val rabbitEntry = Entry()

                    rabbitEntry.entryID = UUID.randomUUID()
                    rabbitEntry.entryName = addName!!.text.toString()
                    if (baseImageUri != null) {
                        rabbitEntry.entryPhLoc = baseImageUri!!.toString()
                    }
                    rabbitEntry.chooseGender = genderSpinner!!.selectedItem.toString()
                    rabbitEntry.matedWithOrParents = matedWithSpinner!!.selectedItem.toString()

                    rabbitEntry.birthDate = birthDate
                    rabbitEntry.matedDate = matingDate

                    if (!rabbitsNum!!.text.toString().isEmpty()) {
                        rabbitEntry.rabbitNumber = Integer.parseInt(rabbitsNum!!.text.toString())

                    }
                    if (!deadRabbitNum!!.text.toString().isEmpty()) {
                        rabbitEntry.rabbitDeadNumber = Integer.parseInt(deadRabbitNum!!.text.toString())
                    }

                    createEvents(rabbitEntry)

                    rabbitEntry.save(databaseWrapper)
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
            addBirthDate!!.setText(defaultFormatter!!.format(birthDate))
        } else {
            matingDate = GregorianCalendar(year, month, dayOfMonth).time
            addMatingDate!!.setText(defaultFormatter!!.format(matingDate))
        }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SELECT_PHOTO && resultCode == Activity.RESULT_OK && data != null) {
            baseImageUri = data.data
            Glide.with(this).load(baseImageUri).into(baseImage!!)

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
                val dialog = android.support.v7.app.AlertDialog.Builder(this)
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
            Glide.with(this).load(photoURI).into(baseImage!!)
        }
        return photoURI
    }

    private fun createEvents(rabbitEntry: Entry) {

        eventsManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if (rabbitEntry.chooseGender == getString(R.string.genderFemale)) {
            if (rabbitEntry.matedDate != null) {

                val upcomingBirth = Date(rabbitEntry.matedDate!!.time + 1000L * 60 * 60 * 24 * 31)
                //newEvent(rabbitEntry,defaultFormatter.format(UpcomingBirth) + ": " + "Did " + rabbitEntry.entryName + " give birth?",UpcomingBirth,0);
                newEvent(rabbitEntry, getString(R.string.femaleGaveBirth, defaultFormatter!!.format(upcomingBirth), rabbitEntry.entryName), upcomingBirth, birthEvent)

                val readyMateDate = Date(upcomingBirth.time + 1000L * 60 * 60 * 24 * 66)
                //newEvent(rabbitEntry,defaultFormatter.format(readyMateDate) + ": " + rabbitEntry.entryName + " is ready for mating",readyMateDate,1);
                newEvent(rabbitEntry, getString(R.string.femaleReadyForMating, defaultFormatter!!.format(readyMateDate), rabbitEntry.entryName), readyMateDate, readyMatingEvent)
            }
        } else if (rabbitEntry.chooseGender == getString(R.string.genderGroup)) {
            if (rabbitEntry.birthDate != null) {
                val moveDate = Date(rabbitEntry.birthDate!!.time + 1000L * 60 * 60 * 24 * 62)
                //newEvent(rabbitEntry,defaultFormatter.format(moveDate) + ": Was the group " + rabbitEntry.entryName + " moved into another cage?",moveDate,2);
                newEvent(rabbitEntry, getString(R.string.groupMovedIntoCage, defaultFormatter!!.format(moveDate), rabbitEntry.entryName), moveDate, moveGroupEvent)

                rabbitEntry.secondParent = parentSpinner!!.selectedItem.toString()


                val slaughterDate = Date(rabbitEntry.birthDate!!.time + 1000L * 60 * 60 * 24 * 124)
                //newEvent(rabbitEntry,defaultFormatter.format(slaughterDate) + ": Was the group " + rabbitEntry.entryName + " slaughtered?",slaughterDate,3);
                newEvent(rabbitEntry, getString(R.string.groupSlauhtered, defaultFormatter!!.format(slaughterDate), rabbitEntry.entryName), slaughterDate, slaughterEvent)

            }
        }
    }

    private fun newEvent(rabbitEntry: Entry, eventString: String, dateOfEvent: Date, type: Int) {
        val createEvent = Events()
        createEvent.eventUUID = UUID.randomUUID()
        createEvent.name = rabbitEntry.entryName
        createEvent.eventString = eventString
        createEvent.dateOfEvent = dateOfEvent
        createEvent.typeOfEvent = type
        /*if(type == 0 && !deadRabbitNum.getText().toString().isEmpty()){
            createEvent.numDead = Integer.parseInt(deadRabbitNum.getText().toString());
        }
        if(type == 0 && !rabbitsNum.getText().toString().isEmpty()){
            createEvent.rabbitsNum = Integer.parseInt(rabbitsNum.getText().toString());
        }*/


        val alertEventService = Intent(this, NotifReciever::class.java)
        alertEventService.putExtra("eventUUID", createEvent.eventUUID)
        createEvent.id = randGen.nextInt()
        val slaughterEventAlarm = PendingIntent.getBroadcast(this, createEvent.id, alertEventService, PendingIntent.FLAG_CANCEL_CURRENT)
        eventsManager!!.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, dateOfEvent.time, slaughterEventAlarm)

        createEvent.save()
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
                        genderSpinner!!.setSelection(genderAdapter!!.getPosition(editable?.chooseGender))
                        parentSpinner!!.setSelection(matedWithAdapter!!.getPosition(editable?.secondParent))

                        //i believe i can just set rabbitsNum and deadRabbits since it hides anyway
                        rabbitsNum!!.setText(editable?.rabbitNumber.toString())
                        deadRabbitNum!!.setText(editable?.rabbitDeadNumber.toString())

                        if (editable?.birthDate != null) {
                            birthDate = editable.birthDate
                            addBirthDate!!.setText(defaultFormatter!!.format(editable.birthDate))
                        }
                        if (editable?.matedDate != null) {
                            matingDate = editable.matedDate
                            lastDate = editable.matedDate
                            addMatingDate!!.setText(defaultFormatter!!.format(editable.matedDate))
                        }
                        Glide.with(this@AddEntryActivity).load(editable?.entryPhLoc).into(baseImage!!)
                    }.execute()
        } else if (getMode == AlertEventService.ADD_BIRTH_FROM_SERVICE) {
            val intent = intent
            SQLite.select()
                    .from(Events::class.java)
                    .where(Events_Table.eventUUID.eq(intent.getSerializableExtra("eventUUID") as UUID))
                    .async()
                    .querySingleResultCallback { _, events ->
                        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                        manager.cancel(events!!.id)
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

        private const val birthEvent = 0
        private const val readyMatingEvent = 1
        private const val moveGroupEvent = 2
        private const val slaughterEvent = 3

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