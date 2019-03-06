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
import android.util.Log
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
import com.example.kocja.rabbiter_online.managers.GsonManager
import com.example.kocja.rabbiter_online.managers.HttpManager
import com.example.kocja.rabbiter_online.R
import com.example.kocja.rabbiter_online.databases.Entry
import com.example.kocja.rabbiter_online.databases.Events
import com.example.kocja.rabbiter_online.services.NotifyUser
import com.example.kocja.rabbiter_online.services.ProcessService
import com.google.gson.Gson

import java.io.File
import java.io.IOException
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar
import java.util.Locale
import java.util.UUID

/**
 * Created by kocja on 21/01/2018.
 */

class addEntryActivity : AppCompatActivity(), DatePickerDialog.OnDateSetListener {

    private var takeBirthDateCal = false
    private var parseFormatter: SimpleDateFormat? = null
    private var textFormatter: SimpleDateFormat? = null
    private var addBirthDate: EditText? = null
    private var addMatingDate: EditText? = null
    private var addName: EditText? = null
    private var baseImage: ImageView? = null
    private var matedWithSpinner: Spinner? = null
    private var genderSpinner: Spinner? = null
    private var birthDate: Date? = null
    private var matedDate: Date? = null
    private var lastDate: Date? = null
    private var baseImageUri: Uri? = null
    private var editable: Entry? = null
    private var matedWithAdapter: ArrayAdapter<String>? = null
    private var genderAdapter: ArrayAdapter<String>? = null
    private var lastGender: String? = null
    private var parentSpinner: Spinner? = null
    private var rabbitsNum: EditText? = null
    private var deadRabbitNum: EditText? = null
    private var gson: Gson? = null
    private var latestImage: File? = null
    private var rabbitsNumText: TextView? = null
    private var numDeadRabTitle: TextView? = null
    private var matedWith: TextView? = null
    //NOTE: type 0: birth
    //NOTE: type 1: ready for mating
    //NOTE: type 2: move group
    //NOTE: type 3: slaughter date

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_entry)
        setTitle(R.string.title)
        gson = GsonManager.getGson()
        parseFormatter = SimpleDateFormat("MMM dd, yyyy HH:mm:ss a", Locale.US)
        textFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.US)

        addBirthDate = findViewById(R.id.addBirthDate)
        addMatingDate = findViewById(R.id.addMatingDate)
        baseImage = findViewById(R.id.mainImage)
        genderSpinner = findViewById(R.id.addGender)
        matedWithSpinner = findViewById(R.id.matedWithSpinner)
        addName = findViewById(R.id.addName)
        rabbitsNum = findViewById(R.id.NumRabbits)
        deadRabbitNum = findViewById(R.id.deadRabbits)
        parentSpinner = findViewById(R.id.parentSpinner)
        matedWith = findViewById(R.id.matedWith)

        val addPhoto = findViewById<ImageButton>(R.id.takePhoto)
        val addBirthDateCal = findViewById<ImageButton>(R.id.addBirthDateCal)
        val addMatingDateCal = findViewById<ImageButton>(R.id.addMatingDateCal)
        val addEntry = findViewById<ImageButton>(R.id.addEntry)

        val getMode = intent.getIntExtra("getMode", -1)

        addPhoto.setOnClickListener {
            val chooseMethod = AlertDialog.Builder(this)
                    .setTitle(R.string.photoOption)
                    .setItems(R.array.DecideOnPhType) { _, i ->
                        if (i == 0) {
                            val photoPickerIntent = Intent(Intent.ACTION_PICK)
                            photoPickerIntent.type = "image/*"
                            startActivityForResult(photoPickerIntent, SELECT_PHOTO)
                        } else {
                            baseImageUri = dispatchTakePictureIntent()
                        }
                    }
            chooseMethod.show()

        }

        HttpManager.getRequest("allEntries") { response ->
            Log.d("response", response)
            val allEntries = gson!!.fromJson(response, Array<Entry>::class.java)
            val allEntryNames = ArrayList<String>(allEntries.size)
            allEntryNames.add(getString(R.string.none))
            for (entry in allEntries) {
                allEntryNames.add(entry.entryName)
            }

            this.runOnUiThread {
                matedWithAdapter = ArrayAdapter(this@addEntryActivity, android.R.layout.simple_spinner_dropdown_item, allEntryNames)
                matedWithSpinner!!.adapter = matedWithAdapter
                parentSpinner!!.adapter = matedWithAdapter
                setEditableEntryProps(getMode)
            }

        }

        //set today date for a datePickerDialog and set listeners
        val c = Calendar.getInstance()
        val pickDate = DatePickerDialog(this, this@addEntryActivity, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH))
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

        numDeadRabTitle = findViewById(R.id.deadNumTextTitle)
        rabbitsNumText = findViewById(R.id.rabbitsNumText)
        genderSpinner!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>, view: View, i: Int, l: Long) {
                when {
                    genderSpinner!!.selectedItem.toString() == getString(R.string.genderMale) -> setGenderSpecificVisibility(View.GONE, getString(R.string.entryMatedWith))
                    //Temporary fix, would have to reconstraint views. will see.
                    /*addMatingDate.setVisibility(View.INVISIBLE);
                    addMatingDateCal.setVisibility(View.INVISIBLE);
                    matingDateText.setVisibility(View.INVISIBLE);*/
                    genderSpinner!!.selectedItem.toString() == "Group" -> setGenderSpecificVisibility(View.VISIBLE, getString(R.string.setParents))
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
                var lastMateDate: Date? = null
                try {
                    lastMateDate = parseFormatter!!.parse(editable!!.matedDate)
                } catch (e: ParseException) {
                    e.printStackTrace()
                }

                if (matedDate != null && parseFormatter!!.format(matedDate) != editable!!.matedDate) {
                    createEvents(editable!!)
                }

                editable!!.entryName = addName!!.text.toString()
                editable!!.chooseGender = genderSpinner!!.selectedItem.toString()
                editable!!.matedWithOrParents = matedWithSpinner!!.selectedItem.toString()
                editable!!.secondParent = parentSpinner!!.selectedItem.toString()
                editable!!.birthDate = parseFormatter!!.format(birthDate)
                editable!!.matedDate = parseFormatter!!.format(matedDate)

                editable!!.setRabbitNumber(rabbitsNum!!.toString())
                editable!!.setRabbitDeadNumber(deadRabbitNum!!.text.toString())

                if (lastMateDate !== matedDate) {
                    createEvents(editable!!)
                }
                editable!!.entryPhLoc = baseImageUri!!.toString()

                // i check if the date is not the same, initialize new events based on those dates
                // i do the same if the user is changing the gender from male to female or group
                if (lastDate !== matedDate || lastGender == getString(R.string.genderMale) && editable!!.chooseGender != getString(R.string.genderMale)) {
                    editable!!.matedDate = parseFormatter!!.format(matedDate)
                    createEvents(editable!!)
                }

                HttpManager.postRequest("updateEntry", gson!!.toJson(editable)) { response, _ ->
                    if (response == "OK") {
                        setResult(Activity.RESULT_OK)
                        finish()
                    }
                }
            } else {
                val rabbitEntry = Entry()
                rabbitEntry.entryID = UUID.randomUUID()
                rabbitEntry.entryName = addName!!.text.toString()
                if (isNotNull(baseImageUri)) {
                    rabbitEntry.entryPhLoc = baseImageUri!!.toString()
                }


                rabbitEntry.chooseGender = genderSpinner!!.selectedItem.toString()
                rabbitEntry.matedWithOrParents = matedWithSpinner!!.selectedItem.toString()

                rabbitEntry.birthDate = parseFormatter!!.format(birthDate)
                rabbitEntry.matedDate = parseFormatter!!.format(matedDate)

                rabbitEntry.setRabbitNumber(rabbitsNum!!.text.toString())
                rabbitEntry.setRabbitDeadNumber(deadRabbitNum!!.text.toString())

                createEvents(rabbitEntry)

                HttpManager.postRequest("createNewEntry", gson!!.toJson(rabbitEntry), latestImage) { response, _ ->
                    if (response == "OK") {
                        setResult(Activity.RESULT_OK)
                        finish()
                    }
                }

            }
        }

    }

    override fun onDateSet(datePicker: DatePicker, year: Int, month: Int, dayOfMonth: Int) {
        if (takeBirthDateCal) {
            birthDate = GregorianCalendar(year, month, dayOfMonth).time
            addBirthDate!!.setText(parseFormatter!!.format(birthDate))
        } else {
            matedDate = GregorianCalendar(year, month, dayOfMonth).time
            addMatingDate!!.setText(parseFormatter!!.format(matedDate))
        }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SELECT_PHOTO && resultCode == Activity.RESULT_OK && data != null) {
            baseImageUri = data.data
            val realPath = getRealPathContentUri(baseImageUri)
            latestImage = File(realPath)
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
                ex.printStackTrace()
            }

            // Continue only if the File was successfully created
            if (photoFile != null) {
                latestImage = photoFile
                photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.photoprovider12",
                        photoFile)
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                this.startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO)
            }
            Glide.with(this).load(photoURI).into(baseImage!!)
        }
        return photoURI
    }

    private fun createEvents(rabbitEntry: Entry) {

        val cal = Calendar.getInstance()

        if (rabbitEntry.chooseGender == getString(R.string.genderFemale)) {
            if (rabbitEntry.matedDate != null) {

                var upcomingBirth: Date? = null
                var readyMateDate: Date? = null

                try {
                    upcomingBirth = parseFormatter!!.parse(rabbitEntry.matedDate)
                    cal.time = upcomingBirth
                    cal.add(Calendar.DAY_OF_YEAR, 31)
                    upcomingBirth = cal.time

                    readyMateDate = upcomingBirth
                    cal.time = readyMateDate
                    cal.add(Calendar.DAY_OF_YEAR, 66)
                    readyMateDate = cal.time

                } catch (e: ParseException) {
                    e.printStackTrace()
                }

                newEvent(rabbitEntry, getString(R.string.femaleGaveBirth, parseFormatter!!.format(upcomingBirth), rabbitEntry.entryName), upcomingBirth, Events.BIRTH_EVENT)


                newEvent(rabbitEntry, getString(R.string.femaleReadyForMating, parseFormatter!!.format(readyMateDate), rabbitEntry.entryName), readyMateDate, Events.READY_MATING_EVENT)
            }
        } else if (rabbitEntry.chooseGender == getString(R.string.genderGroup)) {
            if (rabbitEntry.birthDate != null) {
                var moveDate: Date? = null
                var slaughterDate: Date? = null
                try {
                    moveDate = parseFormatter!!.parse(rabbitEntry.birthDate)
                    slaughterDate = moveDate

                    cal.time = moveDate
                    cal.add(Calendar.DAY_OF_YEAR, 62)
                    moveDate = cal.time

                    cal.time = slaughterDate
                    cal.add(Calendar.DAY_OF_YEAR, 124)
                    slaughterDate = cal.time
                } catch (e: ParseException) {
                    e.printStackTrace()
                }

                newEvent(rabbitEntry, getString(R.string.groupMovedIntoCage, parseFormatter!!.format(moveDate), rabbitEntry.entryName), moveDate, Events.MOVE_GROUP_EVENT)

                rabbitEntry.secondParent = parentSpinner!!.selectedItem.toString()



                newEvent(rabbitEntry, getString(R.string.groupSlauhtered, parseFormatter!!.format(slaughterDate), rabbitEntry.entryName), slaughterDate, Events.SLAUGHTER_EVENT)

            }
        }
    }

    private fun newEvent(rabbitEntry: Entry, eventString: String, dateOfEvent: Date?, type: Int) {
        val createEvent = Events()
        createEvent.eventUUID = UUID.randomUUID()
        createEvent.name = rabbitEntry.entryName
        createEvent.eventString = eventString
        createEvent.dateOfEvent = parseFormatter!!.format(dateOfEvent)
        createEvent.typeOfEvent = type
        createEvent.setNumDead(deadRabbitNum!!.text.toString(), Events.BIRTH_EVENT)
        createEvent.setRabbitsNum(rabbitsNum!!.text.toString(), Events.BIRTH_EVENT)
        /*if(type == 0 && !deadRabbitNum.getText().toString().isEmpty()){
            createEvent.numDead = Integer.parseInt(deadRabbitNum.getText().toString());

        }
        if(type == 0 && !rabbitsNum.getText().toString().isEmpty()){
            createEvent.rabbitsNum = Integer.parseInt(rabbitsNum.getText().toString());
        }*/


        NotifyUser.schedule(this, dateOfEvent!!.time, createEvent.eventUUID.toString())
        //PendingIntent slaughterEventAlarm = PendingIntent.getBroadcast(this, createEvent.getId(), alertEventService,PendingIntent.FLAG_CANCEL_CURRENT);
        //eventsManager.set(AlarmManager.RTC_WAKEUP, dateOfEvent.getTime(), slaughterEventAlarm);

        HttpManager.postRequest("createNewEvent", gson!!.toJson(createEvent)) { _, _ -> }
    }

    private fun setEditableEntryProps(getMode: Int) {

        if (getMode == EDIT_EXISTING_ENTRY) {
            val entryUUID = intent.getSerializableExtra("entryEdit") as UUID
            HttpManager.postRequest("seekSingleEntry", gson!!.toJson(entryUUID)) { response, _ ->
                editable = gson!!.fromJson(response, Entry::class.java)
                lastGender = editable!!.chooseGender

                this.runOnUiThread {
                    matedWithSpinner!!.setSelection(matedWithAdapter!!.getPosition(editable!!.matedWithOrParents))
                    genderSpinner!!.setSelection(genderAdapter!!.getPosition(editable!!.chooseGender))
                    parentSpinner!!.setSelection(matedWithAdapter!!.getPosition(editable!!.secondParent))

                    rabbitsNum!!.setText(editable!!.rabbitNumber.toString())
                    deadRabbitNum!!.setText(editable!!.rabbitDeadNumber.toString())
                    addName!!.setText(editable!!.entryName)

                    Glide.with(this@addEntryActivity).load(editable!!.mergedEntryPhLoc).into(baseImage!!)

                    try {
                        if (isNotNull(editable!!.birthDate)) {
                            birthDate = parseFormatter!!.parse(editable!!.birthDate)
                            addBirthDate!!.setText(textFormatter!!.format(birthDate))
                        }
                        if (isNotNull(editable!!.matedDate)) {
                            matedDate = parseFormatter!!.parse(editable!!.matedDate)
                            lastDate = matedDate
                            addMatingDate!!.setText(textFormatter!!.format(editable!!.matedDate))
                        }
                    } catch (e: ParseException) {
                        e.printStackTrace()
                    }
                }


            }
        } else if (getMode == NotifyUser.ADD_ENTRY_FROM_BIRTH) {
            val intent = intent
            HttpManager.postRequest("getAddBirthReq", gson!!.toJson(intent.getSerializableExtra("eventUUID"))) { response, _ ->
                val addBirthEvent = gson!!.fromJson(response, Array<Events>::class.java)[0]
                val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                manager.cancel(addBirthEvent.id)

                this.runOnUiThread {
                    matedWithSpinner!!.setSelection(matedWithAdapter!!.getPosition(addBirthEvent.name))
                    parentSpinner!!.setSelection(matedWithAdapter!!.getPosition(addBirthEvent.secondParent))

                    val processEventsIntent = Intent(this, ProcessService::class.java)
                            .putExtra("processEventUUID", addBirthEvent.eventUUID)
                            .putExtra("happened", intent.getBooleanExtra("happened", false))
                    startService(processEventsIntent)
                }

            }
        }
        //public void setAdaptersToSpinners()
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

    private fun isNotNull(obj: Any?): Boolean {
        return obj != null
    }

    private fun setGenderSpecificVisibility(visibility: Int, text: String) {
        matedWith!!.text = text
        parentSpinner!!.visibility = visibility
        rabbitsNum!!.visibility = visibility
        deadRabbitNum!!.visibility = visibility
        numDeadRabTitle!!.visibility = visibility
        rabbitsNumText!!.visibility = visibility
    }

    companion object {
        private const val REQUEST_TAKE_PHOTO = 0
        private const  val SELECT_PHOTO = 1
        const val EDIT_EXISTING_ENTRY = 2
        private val ONE_DAY = 1000L * 60 * 60 * 24

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
