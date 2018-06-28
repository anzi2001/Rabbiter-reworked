package com.example.kocja.rabbiter_online.activities;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;  
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.kocja.rabbiter_online.GsonManager;
import com.example.kocja.rabbiter_online.HttpManager;
import com.example.kocja.rabbiter_online.R;
import com.example.kocja.rabbiter_online.broadcastrecievers.NotifReciever;
import com.example.kocja.rabbiter_online.databases.Entry;
import com.example.kocja.rabbiter_online.databases.Events;
import com.example.kocja.rabbiter_online.services.AlertEventService;
import com.example.kocja.rabbiter_online.services.processEvents;
import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;

/**
 * Created by kocja on 21/01/2018.
 */

public class addEntryActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {
    private static final int REQUEST_TAKE_PHOTO = 0;
    private static final int SELECT_PHOTO = 1;
    public static final int EDIT_EXISTING_ENTRY = 2;
    private boolean takeBirthDateCal = false;
    private SimpleDateFormat defaultFormatter;
    private EditText addBirthDate;
    private EditText addMatingDate;
    private EditText addName;
    private ImageView baseImage;
    private Spinner matedWithSpinner;
    private Spinner genderSpinner;
    private Date birthDate;
    private Date matingDate;
    private Date lastDate;
    private Uri baseImageUri;
    private Entry editable;
    private ArrayAdapter<String> matedWithAdapter;
    private ArrayAdapter<String> genderAdapter;
    private String lastGender;
    private Spinner parentSpinner;
    private EditText rabbitsNum;
    private EditText deadRabbitNum;
    private AlarmManager eventsManager;
    private final  Random randGen = new Random();
    private Gson gson;
    private File latestImage;
    //NOTE: type 0: birth
    //NOTE: type 1: ready for mating
    //NOTE: type 2: move group
    //NOTE: type 3: slaughter date

    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_entry);
        setTitle(R.string.title);
        gson = GsonManager.getGson();
        defaultFormatter = new SimpleDateFormat("dd/MM/yyyy", Locale.GERMANY);
        addBirthDate = findViewById(R.id.addBirthDate);
        addMatingDate = findViewById(R.id.addMatingDate);
        baseImage = findViewById(R.id.mainImage);
        genderSpinner = findViewById(R.id.addGender);
        matedWithSpinner = findViewById(R.id.matedWithSpinner);
        addName = findViewById(R.id.addName);
        rabbitsNum = findViewById(R.id.NumRabbits);
        deadRabbitNum = findViewById(R.id.deadRabbits);
        parentSpinner = findViewById(R.id.parentSpinner);

        final ImageButton addPhoto = findViewById(R.id.takePhoto);
        final TextView matedWith = findViewById(R.id.matedWith);
        final ImageButton addBirthDateCal = findViewById(R.id.addBirthDateCal);
        final ImageButton addMatingDateCal = findViewById(R.id.addMatingDateCal);
        final ImageButton addEntry = findViewById(R.id.addEntry);

        int getMode = getIntent().getIntExtra("getMode",-1);

        addPhoto.setOnClickListener(view -> {
            AlertDialog.Builder chooseMethod = new AlertDialog.Builder(this)
                    .setTitle(R.string.photoOption)
                    .setItems(R.array.DecideOnPhType, (dialogInterface, i) -> {
                        if(i == 0){
                            Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                            photoPickerIntent.setType("image/*");
                            startActivityForResult(photoPickerIntent, SELECT_PHOTO);
                        }
                        else{
                            baseImageUri = dispatchTakePictureIntent();
                        }
                    });
            chooseMethod.show();

        });

        HttpManager.getRequest("allEntries", response -> {
            Log.d("response",response);
            Entry[] allEntries = gson.fromJson(response,Entry[].class);
            List<String> allEntryNames = new ArrayList<>(allEntries.length);
            allEntryNames.add(getString(R.string.none));
            for(Entry entry : allEntries){
                allEntryNames.add(entry.entryName);
            }

            this.runOnUiThread(() -> {
                matedWithAdapter = new ArrayAdapter<>(addEntryActivity.this,android.R.layout.simple_spinner_dropdown_item,allEntryNames);
                matedWithSpinner.setAdapter(matedWithAdapter);
                parentSpinner.setAdapter(matedWithAdapter);
                setEditableEntryProps(getMode);
            });

        });


        Calendar c = Calendar.getInstance();
        DatePickerDialog pickDate = new DatePickerDialog(this,addEntryActivity.this,c.get(Calendar.YEAR),c.get(Calendar.MONTH),c.get(Calendar.DAY_OF_MONTH));
        addBirthDateCal.setOnClickListener(view -> {
            takeBirthDateCal = true;
            pickDate.show();
        });

        addMatingDateCal.setOnClickListener(view -> {
            takeBirthDateCal = false;
            pickDate.show();
        });

        genderAdapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,getResources().getStringArray(R.array.decideOnGender));
        genderSpinner.setAdapter(genderAdapter);
        TextView numDeadRabTitle = findViewById(R.id.deadNumTextTitle);
        TextView rabbitsNumText = findViewById(R.id.rabbitsNumText);
        TextView matingDateText = findViewById(R.id.matingDate);
        genderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(genderSpinner.getSelectedItem().toString().equals(getString(R.string.genderMale))){
                    parentSpinner.setVisibility(View.GONE);
                    matedWith.setText(getString(R.string.entryMatedWith));

                    rabbitsNum.setVisibility(View.GONE);
                    deadRabbitNum.setVisibility(View.GONE);
                    numDeadRabTitle.setVisibility(View.GONE);
                    rabbitsNumText.setVisibility(View.GONE);

                    //Temporary fix, would have to reconstraint views. will see.
                    /*addMatingDate.setVisibility(View.INVISIBLE);
                    addMatingDateCal.setVisibility(View.INVISIBLE);
                    matingDateText.setVisibility(View.INVISIBLE);*/
                }
                else if(genderSpinner.getSelectedItem().toString().equals("Group")){
                    matedWith.setText(getString(R.string.setParents));
                    parentSpinner.setVisibility(View.VISIBLE);
                    rabbitsNum.setVisibility(View.VISIBLE);
                    deadRabbitNum.setVisibility(View.VISIBLE);
                    numDeadRabTitle.setVisibility(View.VISIBLE);
                    rabbitsNumText.setVisibility(View.VISIBLE);
                }
                else{
                    parentSpinner.setVisibility(View.GONE);
                    matedWith.setText(getString(R.string.entryMatedWith));

                    rabbitsNum.setVisibility(View.GONE);
                    deadRabbitNum.setVisibility(View.GONE);
                    numDeadRabTitle.setVisibility(View.GONE);
                    rabbitsNumText.setVisibility(View.GONE );

                    /*addMatingDateCal.setVisibility(View.VISIBLE);
                    addMatingDate.setVisibility(View.VISIBLE);
                    matingDateText.setVisibility(View.VISIBLE);*/
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        addEntry.setOnClickListener(view ->{

            if(getMode == EDIT_EXISTING_ENTRY){
                Date lastMateDate = null;
                try {
                    lastMateDate = defaultFormatter.parse(editable.matedDate);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                if(!defaultFormatter.format(matingDate).equals(editable.matedDate) && matingDate != null){
                    createEvents(editable);
                }
                editable.entryName = addName.getText().toString();
                editable.chooseGender = genderSpinner.getSelectedItem().toString();
                editable.matedWithOrParents = matedWithSpinner.getSelectedItem().toString();
                editable.secondParent = parentSpinner.getSelectedItem().toString();
                editable.birthDate = defaultFormatter.format(birthDate);
                editable.matedDate = defaultFormatter.format(matingDate);
                if(!rabbitsNum.getText().toString().isEmpty()){
                    editable.rabbitNumber = Integer.parseInt(rabbitsNum.getText().toString());
                }
                if(!deadRabbitNum.getText().toString().isEmpty()){
                    editable.rabbitDeadNumber = Integer.parseInt(deadRabbitNum.getText().toString());
                }

                if(lastMateDate != matingDate){
                    createEvents(editable);
                }
                if(baseImageUri != null){
                    editable.entryPhLoc = baseImageUri.toString();
                }

                    // i check if the date is not the same, initialize new events based on those dates
                    // i do the same if the user is changing the gender from male to female or group
                if(lastDate != matingDate || (lastGender.equals(getString(R.string.genderMale)) && !editable.chooseGender.equals(getString(R.string.genderMale)))){
                    editable.matedDate = defaultFormatter.format(matingDate);
                    createEvents(editable);
                }

                HttpManager.postRequest("updateEntry", gson.toJson(editable), (response,bytes) -> {
                    if(response.equals("OK")){
                        setResult(RESULT_OK);
                        finish();
                    }
                });
            }
            else {
                Entry rabbitEntry = new Entry();
                rabbitEntry.entryID = UUID.randomUUID();
                rabbitEntry.entryName = addName.getText().toString();
                if(baseImageUri != null) {
                    rabbitEntry.entryPhLoc = baseImageUri.toString();
                }
                rabbitEntry.chooseGender = genderSpinner.getSelectedItem().toString();
                rabbitEntry.matedWithOrParents = matedWithSpinner.getSelectedItem().toString();

                if(birthDate != null){
                    rabbitEntry.birthDate = defaultFormatter.format(birthDate);
                }
                if(matingDate != null){
                    rabbitEntry.matedDate = defaultFormatter.format(matingDate);
                }


                if(!rabbitsNum.getText().toString().isEmpty()){
                    rabbitEntry.rabbitNumber = Integer.parseInt(rabbitsNum.getText().toString());

                }
                if(!deadRabbitNum.getText().toString().isEmpty()){
                    rabbitEntry.rabbitDeadNumber = Integer.parseInt(deadRabbitNum.getText().toString());
                }

                createEvents(rabbitEntry);

                HttpManager.postRequest("createNewEntry", gson.toJson(rabbitEntry), latestImage, (response,bytes) -> {
                    if(response.equals("OK")){
                        setResult(RESULT_OK);
                        finish();
                    }
                });

            }
        });

    }

    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
        if(takeBirthDateCal){
            birthDate = new GregorianCalendar(year,month,dayOfMonth).getTime();
            addBirthDate.setText(defaultFormatter.format(birthDate));
        }
        else{
            matingDate = new GregorianCalendar(year,month,dayOfMonth).getTime();
            addMatingDate.setText(defaultFormatter.format(matingDate));
        }
    }

    public void onActivityResult(int requestCode,int resultCode,Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode == SELECT_PHOTO && resultCode == RESULT_OK && data != null){
            baseImageUri = data.getData();
            String realPath = getRealPathContentUri(baseImageUri);
            latestImage = new File(realPath);
            Glide.with(this).load(baseImageUri).into(baseImage);

        }
    }

    private static File createImageFile(Context mainC) throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.GERMANY).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + '_';
        File storageDir = mainC.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir)
                ;
    }

    private Uri dispatchTakePictureIntent() {

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        Uri photoURI = null;
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(this.getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile(this);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                latestImage = photoFile;
                photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.photoprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                this.startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
            Glide.with(this).load(photoURI).into(baseImage);
        }
        return photoURI;
    }
    private void createEvents(Entry rabbitEntry){

        eventsManager =(AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Calendar cal = Calendar.getInstance();

        if (rabbitEntry.chooseGender.equals(getString(R.string.genderFemale))) {
            if(rabbitEntry.matedDate != null) {

                Date UpcomingBirth = null;
                Date readyMateDate = null;

                try {
                    UpcomingBirth = defaultFormatter.parse(rabbitEntry.matedDate);
                    cal.setTime(UpcomingBirth);
                    cal.add(Calendar.DAY_OF_YEAR,31);
                    UpcomingBirth = cal.getTime();

                    readyMateDate = UpcomingBirth;
                    cal.setTime(readyMateDate);
                    cal.add(Calendar.DAY_OF_YEAR,66);
                    readyMateDate = cal.getTime();

                } catch (ParseException e) {
                    e.printStackTrace();
                }
                //newEvent(rabbitEntry,defaultFormatter.format(UpcomingBirth) + ": " + "Did " + rabbitEntry.entryName + " give birth?",UpcomingBirth,0);
                newEvent(rabbitEntry,getString(R.string.femaleGaveBirth,defaultFormatter.format(UpcomingBirth),rabbitEntry.entryName),UpcomingBirth,0);


                //newEvent(rabbitEntry,defaultFormatter.format(readyMateDate) + ": " + rabbitEntry.entryName + " is ready for mating",readyMateDate,1);
                newEvent(rabbitEntry,getString(R.string.femaleReadyForMating,defaultFormatter.format(readyMateDate),rabbitEntry.entryName),readyMateDate,1);
            }
        } else if (rabbitEntry.chooseGender.equals(getString(R.string.genderGroup))) {
            if(rabbitEntry.birthDate != null) {
                Date moveDate = null;
                Date slaughterDate = null;
                try {
                    moveDate = defaultFormatter.parse(rabbitEntry.birthDate);
                    slaughterDate = moveDate;

                    cal.setTime(moveDate);
                    cal.add(Calendar.DAY_OF_YEAR,62);
                    moveDate = cal.getTime();

                    cal.setTime(slaughterDate);
                    cal.add(Calendar.DAY_OF_YEAR,124);
                    slaughterDate = cal.getTime();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                //newEvent(rabbitEntry,defaultFormatter.format(moveDate) + ": Was the group " + rabbitEntry.entryName + " moved into another cage?",moveDate,2);
                newEvent(rabbitEntry,getString(R.string.groupMovedIntoCage,defaultFormatter.format(moveDate),rabbitEntry.entryName),moveDate,2);

                rabbitEntry.secondParent = parentSpinner.getSelectedItem().toString();



                //newEvent(rabbitEntry,defaultFormatter.format(slaughterDate) + ": Was the group " + rabbitEntry.entryName + " slaughtered?",slaughterDate,3);
                newEvent(rabbitEntry,getString(R.string.groupSlauhtered,defaultFormatter.format(slaughterDate),rabbitEntry.entryName),slaughterDate,3);

            }
        }
    }
    private void newEvent(Entry rabbitEntry, String eventString, Date dateOfEvent, int type){
        Events createEvent = new Events();
        createEvent.eventUUID = UUID.randomUUID();
        createEvent.name = rabbitEntry.entryName;
        createEvent.eventString = eventString;
        createEvent.dateOfEvent = defaultFormatter.format(dateOfEvent);
        createEvent.typeOfEvent = type;
        /*if(type == 0 && !deadRabbitNum.getText().toString().isEmpty()){
            createEvent.numDead = Integer.parseInt(deadRabbitNum.getText().toString());

        }
        if(type == 0 && !rabbitsNum.getText().toString().isEmpty()){
            createEvent.rabbitsNum = Integer.parseInt(rabbitsNum.getText().toString());
        }*/


        Intent alertEventService = new Intent(this, NotifReciever.class);
        alertEventService.putExtra("eventUUID", createEvent.eventUUID);
        createEvent.id = randGen.nextInt();
        PendingIntent slaughterEventAlarm = PendingIntent.getBroadcast(this, createEvent.id, alertEventService,PendingIntent.FLAG_CANCEL_CURRENT);
        eventsManager.set(AlarmManager.RTC_WAKEUP, dateOfEvent.getTime(), slaughterEventAlarm);

        HttpManager.postRequest("createNewEvent", gson.toJson(createEvent), (response,bytes) -> {});
    }
    private void setEditableEntryProps(int getMode){

        if(getMode == EDIT_EXISTING_ENTRY){
            UUID entryUUID = (UUID) getIntent().getSerializableExtra("entryEdit");
            HttpManager.postRequest("seekSingleEntry", gson.toJson(entryUUID), (response,bytes) -> {
                this.editable = gson.fromJson(response,Entry.class);
                lastGender = this.editable.chooseGender;

                this.runOnUiThread(() -> {
                    matedWithSpinner.setSelection(matedWithAdapter.getPosition(this.editable.matedWithOrParents));
                    genderSpinner.setSelection(genderAdapter.getPosition(this.editable.chooseGender));
                    parentSpinner.setSelection(matedWithAdapter.getPosition(this.editable.secondParent));
                    //i believe i can just set rabbitsNum and deadRabbits since it hides anyway
                    rabbitsNum.setText(String.valueOf(this.editable.rabbitNumber));
                    deadRabbitNum.setText(String.valueOf(this.editable.rabbitDeadNumber));
                    addName.setText(this.editable.entryName);
                    Glide.with(addEntryActivity.this).load(this.editable.mergedEntryPhLoc).into(baseImage);
                    try{
                        if (this.editable.birthDate != null) {
                            birthDate = defaultFormatter.parse(this.editable.birthDate);
                            addBirthDate.setText(defaultFormatter.format(this.editable.birthDate));
                        }
                        if(this.editable.matedDate != null){
                            matingDate = defaultFormatter.parse(this.editable.matedDate);
                            lastDate = defaultFormatter.parse(this.editable.matedDate);
                            addMatingDate.setText(defaultFormatter.format(this.editable.matedDate));
                        }
                    }
                    catch(ParseException e){
                        e.printStackTrace();
                    }
                });


            });
            /*
            SQLite.select()
                    .from(Entry.class)
                    .where(Entry_Table.entryID.eq(entryUUID))
                    .async()
                    .querySingleResultCallback((transaction, editable) -> {

                    }).execute();
             */
        }
        else if(getMode == AlertEventService.ADD_BIRTH_FROM_SERVICE){
            Intent intent = getIntent();
            HttpManager.postRequest("getAddBirthReq",gson.toJson(intent.getSerializableExtra("eventUUID")), (response,bytes) -> {
                Events addBirthEvent = gson.fromJson(response,Events[].class)[0];
                NotificationManager manager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
                manager.cancel(addBirthEvent.id);

                this.runOnUiThread(() -> {
                    matedWithSpinner.setSelection(matedWithAdapter.getPosition(addBirthEvent.name));
                    parentSpinner.setSelection(matedWithAdapter.getPosition(addBirthEvent.secondParent));

                    Intent processEventsIntent = new Intent(this,processEvents.class);
                    processEventsIntent.putExtra("processEventUUID",addBirthEvent.eventUUID);
                    processEventsIntent.putExtra("happened",intent.getBooleanExtra("happened",false));
                    startService(processEventsIntent);
                });

            });
        }
        //public void setAdaptersToSpinners()
    }
    String getRealPathContentUri(Uri contentUri){
        String Projs[] = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri,Projs,null,null,null);
        cursor.moveToFirst();
        int columnIndex = cursor.getColumnIndex(Projs[0]);
        String path = cursor.getString(columnIndex);
        cursor.close();
        return path;

    }
}
