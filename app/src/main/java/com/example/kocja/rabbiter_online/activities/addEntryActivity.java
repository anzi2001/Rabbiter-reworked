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
import com.example.kocja.rabbiter_online.managers.GsonManager;
import com.example.kocja.rabbiter_online.managers.HttpManager;
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
    private SimpleDateFormat parseFormatter;
    private SimpleDateFormat textFormatter;
    private EditText addBirthDate;
    private EditText addMatingDate;
    private EditText addName;
    private ImageView baseImage;
    private Spinner matedWithSpinner;
    private Spinner genderSpinner;
    private Date birthDate;
    private Date matedDate;
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
    private TextView rabbitsNumText;
    private TextView numDeadRabTitle;
    private TextView matedWith;
    //NOTE: type 0: birth
    //NOTE: type 1: ready for mating
    //NOTE: type 2: move group
    //NOTE: type 3: slaughter date

    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_entry);
        setTitle(R.string.title);
        gson = GsonManager.getGson();
        parseFormatter = new SimpleDateFormat("MMM dd, yyyy HH:mm:ss a", Locale.US);
        textFormatter = new SimpleDateFormat("dd/MM/yyyy", Locale.US);

        addBirthDate = findViewById(R.id.addBirthDate);
        addMatingDate = findViewById(R.id.addMatingDate);
        baseImage = findViewById(R.id.mainImage);
        genderSpinner = findViewById(R.id.addGender);
        matedWithSpinner = findViewById(R.id.matedWithSpinner);
        addName = findViewById(R.id.addName);
        rabbitsNum = findViewById(R.id.NumRabbits);
        deadRabbitNum = findViewById(R.id.deadRabbits);
        parentSpinner = findViewById(R.id.parentSpinner);
        matedWith = findViewById(R.id.matedWith);

        final ImageButton addPhoto = findViewById(R.id.takePhoto);
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
                allEntryNames.add(entry.getEntryName());
            }

            this.runOnUiThread(() -> {
                matedWithAdapter = new ArrayAdapter<>(addEntryActivity.this,android.R.layout.simple_spinner_dropdown_item,allEntryNames);
                matedWithSpinner.setAdapter(matedWithAdapter);
                parentSpinner.setAdapter(matedWithAdapter);
                setEditableEntryProps(getMode);
            });

        });

        //set today date for a datePickerDialog and set listeners
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

        numDeadRabTitle = findViewById(R.id.deadNumTextTitle);
        rabbitsNumText = findViewById(R.id.rabbitsNumText);
        TextView matingDateText = findViewById(R.id.matingDate);
        genderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(genderSpinner.getSelectedItem().toString().equals(getString(R.string.genderMale))){
                    setGenderSpecifVisib(View.GONE,getString(R.string.entryMatedWith));
                    //Temporary fix, would have to reconstraint views. will see.
                    /*addMatingDate.setVisibility(View.INVISIBLE);
                    addMatingDateCal.setVisibility(View.INVISIBLE);
                    matingDateText.setVisibility(View.INVISIBLE);*/
                }
                else if(genderSpinner.getSelectedItem().toString().equals("Group")){
                    setGenderSpecifVisib(View.VISIBLE,getString(R.string.setParents));
                }
                else{
                    setGenderSpecifVisib(View.GONE,getString(R.string.entryMatedWith));
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
                    lastMateDate = parseFormatter.parse(editable.getMatedDate());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                if(matedDate != null && !parseFormatter.format(matedDate).equals(editable.getMatedDate())){
                    createEvents(editable);
                }

                editable.setEntryName(addName.getText().toString());
                editable.setChooseGender(genderSpinner.getSelectedItem().toString());
                editable.setMatedWithOrParents(matedWithSpinner.getSelectedItem().toString());
                editable.setSecondParent(parentSpinner.getSelectedItem().toString());
                editable.setBirthDate(parseFormatter.format(birthDate));
                editable.setMatedDate(parseFormatter.format(matedDate));

                editable.setRabbitNumber(rabbitsNum.toString());
                editable.setRabbitDeadNumber(deadRabbitNum.getText().toString());

                if(lastMateDate != matedDate){
                    createEvents(editable);
                }
                editable.setEntryPhLoc(baseImageUri.toString());

                    // i check if the date is not the same, initialize new events based on those dates
                    // i do the same if the user is changing the gender from male to female or group
                if(lastDate != matedDate || (lastGender.equals(getString(R.string.genderMale)) && !editable.getChooseGender().equals(getString(R.string.genderMale)))){
                    editable.setMatedDate(parseFormatter.format(matedDate));
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
                rabbitEntry.setEntryID(UUID.randomUUID());
                rabbitEntry.setEntryName(addName.getText().toString());
                rabbitEntry.setEntryPhLoc(baseImageUri.toString());

                rabbitEntry.setChooseGender(genderSpinner.getSelectedItem().toString());
                rabbitEntry.setMatedWithOrParents(matedWithSpinner.getSelectedItem().toString());

                rabbitEntry.setBirthDate(parseFormatter.format(birthDate));
                rabbitEntry.setMatedDate(parseFormatter.format(matedDate));

                rabbitEntry.setRabbitNumber(rabbitsNum.getText().toString());
                rabbitEntry.setRabbitDeadNumber(deadRabbitNum.getText().toString());

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
            addBirthDate.setText(parseFormatter.format(birthDate));
        }
        else{
            matedDate = new GregorianCalendar(year,month,dayOfMonth).getTime();
            addMatingDate.setText(parseFormatter.format(matedDate));
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
                        "com.example.android.photoprovider12",
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

        if (rabbitEntry.getChooseGender().equals(getString(R.string.genderFemale))) {
            if(rabbitEntry.getMatedDate() != null) {

                Date UpcomingBirth = null;
                Date readyMateDate = null;

                try {
                    UpcomingBirth = parseFormatter.parse(rabbitEntry.getMatedDate());
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

                newEvent(rabbitEntry,getString(R.string.femaleGaveBirth,parseFormatter.format(UpcomingBirth),rabbitEntry.getEntryName()),UpcomingBirth,0);


                newEvent(rabbitEntry,getString(R.string.femaleReadyForMating,parseFormatter.format(readyMateDate),rabbitEntry.getEntryName()),readyMateDate,1);
            }
        } else if (rabbitEntry.getChooseGender().equals(getString(R.string.genderGroup))) {
            if(rabbitEntry.getBirthDate() != null) {
                Date moveDate = null;
                Date slaughterDate = null;
                try {
                    moveDate = parseFormatter.parse(rabbitEntry.getBirthDate());
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
                newEvent(rabbitEntry,getString(R.string.groupMovedIntoCage,parseFormatter.format(moveDate),rabbitEntry.getEntryName()),moveDate,2);

                rabbitEntry.setSecondParent(parentSpinner.getSelectedItem().toString());



                newEvent(rabbitEntry,getString(R.string.groupSlauhtered,parseFormatter.format(slaughterDate),rabbitEntry.getEntryName()),slaughterDate,3);

            }
        }
    }
    private void newEvent(Entry rabbitEntry, String eventString, Date dateOfEvent, int type){
        Events createEvent = new Events();
        createEvent.setEventUUID(UUID.randomUUID());
        createEvent.setName(rabbitEntry.getEntryName());
        createEvent.setEventString(eventString);
        createEvent.setDateOfEvent(parseFormatter.format(dateOfEvent));
        createEvent.setTypeOfEvent(type);
        createEvent.setNumDead(deadRabbitNum.getText().toString(),0);
        createEvent.setRabbitsNum(rabbitsNum.getText().toString(),0);
        /*if(type == 0 && !deadRabbitNum.getText().toString().isEmpty()){
            createEvent.numDead = Integer.parseInt(deadRabbitNum.getText().toString());

        }
        if(type == 0 && !rabbitsNum.getText().toString().isEmpty()){
            createEvent.rabbitsNum = Integer.parseInt(rabbitsNum.getText().toString());
        }*/


        Intent alertEventService = new Intent(this, NotifReciever.class);
        alertEventService.putExtra("eventUUID", createEvent.getEventUUID());
        createEvent.setId(randGen.nextInt());
        PendingIntent slaughterEventAlarm = PendingIntent.getBroadcast(this, createEvent.getId(), alertEventService,PendingIntent.FLAG_CANCEL_CURRENT);
        eventsManager.set(AlarmManager.RTC_WAKEUP, dateOfEvent.getTime(), slaughterEventAlarm);

        HttpManager.postRequest("createNewEvent", gson.toJson(createEvent), (response,bytes) -> {});
    }
    private void setEditableEntryProps(int getMode){

        if(getMode == EDIT_EXISTING_ENTRY){
            UUID entryUUID = (UUID) getIntent().getSerializableExtra("entryEdit");
            HttpManager.postRequest("seekSingleEntry", gson.toJson(entryUUID), (response,bytes) -> {
                editable = gson.fromJson(response,Entry[].class)[0];
                lastGender = editable.getChooseGender();

                this.runOnUiThread(() -> {
                    matedWithSpinner.setSelection(matedWithAdapter.getPosition(editable.getMatedWithOrParents()));
                    genderSpinner.setSelection(genderAdapter.getPosition(editable.getChooseGender()));
                    parentSpinner.setSelection(matedWithAdapter.getPosition(editable.getSecondParent()));

                    rabbitsNum.setText(String.valueOf(editable.getRabbitNumber()));
                    deadRabbitNum.setText(String.valueOf(editable.getRabbitDeadNumber()));
                    addName.setText(editable.getEntryName());

                    Glide.with(addEntryActivity.this).load(editable.getMergedEntryPhLoc()).into(baseImage);

                    try{
                        if (isNotNull(editable.getBirthDate())) {
                            birthDate = parseFormatter.parse(editable.getBirthDate());
                            addBirthDate.setText(textFormatter.format(birthDate));
                        }
                        if(isNotNull(editable.getMatedDate())){
                            matedDate = parseFormatter.parse(editable.getMatedDate());
                            lastDate = matedDate;
                            addMatingDate.setText(textFormatter.format(editable.getMatedDate()));
                        }
                    }
                    catch(ParseException e){
                        e.printStackTrace();
                    }
                });


            });
        }
        else if(getMode == AlertEventService.ADD_BIRTH_FROM_SERVICE){
            Intent intent = getIntent();
            HttpManager.postRequest("getAddBirthReq",gson.toJson(intent.getSerializableExtra("eventUUID")), (response,bytes) -> {
                Events addBirthEvent = gson.fromJson(response,Events[].class)[0];
                NotificationManager manager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
                manager.cancel(addBirthEvent.getId());

                this.runOnUiThread(() -> {
                    matedWithSpinner.setSelection(matedWithAdapter.getPosition(addBirthEvent.getName()));
                    parentSpinner.setSelection(matedWithAdapter.getPosition(addBirthEvent.getSecondParent()));

                    Intent processEventsIntent = new Intent(this,processEvents.class)
                            .putExtra("processEventUUID",addBirthEvent.getEventUUID())
                            .putExtra("happened",intent.getBooleanExtra("happened",false));
                    startService(processEventsIntent);
                });

            });
        }
        //public void setAdaptersToSpinners()
    }
    private String getRealPathContentUri(Uri contentUri){
        String images[] = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri,images,null,null,null);
        cursor.moveToFirst();
        int columnIndex = cursor.getColumnIndex(images[0]);
        String path = cursor.getString(columnIndex);
        cursor.close();
        return path;

    }
    private boolean isNotNull(Object obj){
        return obj != null;
    }
    private void setGenderSpecifVisib(int visibility,String text){
        matedWith.setText(text);
        parentSpinner.setVisibility(visibility);
        rabbitsNum.setVisibility(visibility);
        deadRabbitNum.setVisibility(visibility);
        numDeadRabTitle.setVisibility(visibility);
        rabbitsNumText.setVisibility(visibility);
    }
}
