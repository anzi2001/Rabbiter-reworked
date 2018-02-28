package com.example.kocja.rabbiter_reworked.activities;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
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
import com.example.kocja.rabbiter_reworked.R;
import com.example.kocja.rabbiter_reworked.databases.Entry;
import com.example.kocja.rabbiter_reworked.databases.Entry_Table;
import com.example.kocja.rabbiter_reworked.databases.Events;
import com.example.kocja.rabbiter_reworked.databases.Events_Table;
import com.example.kocja.rabbiter_reworked.databases.appDatabase;
import com.example.kocja.rabbiter_reworked.services.AlertEventService;
import com.example.kocja.rabbiter_reworked.services.processEvents;
import com.raizlabs.android.dbflow.config.DatabaseDefinition;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.structure.database.transaction.Transaction;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
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
    private static boolean takeBirthDateCal = false;
    private SimpleDateFormat defaultFormatter;
    private EditText addBirthDate;
    private EditText addMatingDate;
    private ImageView baseImage;
    private Date birthDate;
    private Date matingDate;
    private Uri baseImageUri;

    //NOTE Female events: firstEvent = birth, secondEvent = ready
    //NOTE Group events: firstEvent = move, secondEvent = slaughter

    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_entry);
        setTitle("Add Entry");
        defaultFormatter = new SimpleDateFormat("dd/MM/yyyy", Locale.GERMANY);
        addBirthDate = findViewById(R.id.addBirthDate);
        addMatingDate = findViewById(R.id.addMatingDate);
        baseImage = findViewById(R.id.mainImage);
        final ImageButton addPhoto = findViewById(R.id.takePhoto);
        final EditText addName = findViewById(R.id.addName);
        final EditText addMatedWith = findViewById(R.id.addMatedWith);
        final TextView matedWith = findViewById(R.id.matedWith);
        final Spinner genderSpinner = findViewById(R.id.addGender);
        final ImageButton addBirthDateCal = findViewById(R.id.addBirthDateCal);
        final ImageButton addMatingDateCal = findViewById(R.id.addMatingDateCal);
        final ImageButton addEntry = findViewById(R.id.addEntry);

        int getMode = getIntent().getIntExtra("getMode",-1);

        addPhoto.setOnClickListener(view -> {
            AlertDialog.Builder chooseMethod = new AlertDialog.Builder(this)
                    .setTitle("How do you want to get your photo?")
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



        DatePickerDialog pickDate = new DatePickerDialog(this,addEntryActivity.this,2018,1,24);
        addBirthDateCal.setOnClickListener(view -> {
            takeBirthDateCal = true;
            pickDate.show();
        });

        addMatingDateCal.setOnClickListener(view -> {
            takeBirthDateCal = false;
            pickDate.show();
        });

        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,new String[]{"Male","Female","Group"});
        genderSpinner.setAdapter(genderAdapter);
        genderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(genderSpinner.getSelectedItem().toString().equals("Group")){
                    matedWith.setText("Parents: ");
                }
                else{
                    matedWith.setText("Mated with: ");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        if(getMode == EDIT_EXISTING_ENTRY){
            UUID entryUUID = (UUID) getIntent().getSerializableExtra("entryEdit");
            SQLite.select()
                    .from(Entry.class)
                    .where(Entry_Table.entryID.eq(entryUUID))
                    .async()
                    .querySingleResultCallback((transaction, editable) -> {
                        addName.setText(editable.entryName);
                        addMatedWith.setText(editable.matedWithOrParents);
                        genderSpinner.setSelection(genderAdapter.getPosition(editable.chooseGender));
                        if (editable.birthDate != null) {
                            addBirthDate.setText(defaultFormatter.format(editable.birthDate));
                            addMatingDate.setText(defaultFormatter.format(editable.matedDate));
                        }
                        Glide.with(addEntryActivity.this).load(editable.mergedEntryPhLoc).into(baseImage);
                    }).execute();
        }
        else if(getMode == AlertEventService.ADD_BIRTH_FROM_SERVICE){
            Intent intent = getIntent();
            SQLite.select()
                    .from(Events.class)
                    .where(Events_Table.eventUUID.eq((UUID)intent.getSerializableExtra("eventUUID")))
                    .async()
                    .querySingleResultCallback((transaction, events) -> {
                        addMatedWith.setText(events.name + ", " + events.secondParent);

                        Intent processEventsIntent = new Intent(this,processEvents.class);
                        processEventsIntent.putExtra("processEventUUID",events.eventUUID);
                        processEventsIntent.putExtra("happened",intent.getBooleanExtra("happened",false));
                        startService(processEventsIntent);
                    }).execute();
        }

        addEntry.setOnClickListener(view ->{
            AlarmManager manager =(AlarmManager) getSystemService(Context.ALARM_SERVICE);



            DatabaseDefinition database = FlowManager.getDatabase(appDatabase.class);
            Transaction transaction = database.beginTransactionAsync(databaseWrapper -> {
                Entry rabbitEntry = new Entry();
                rabbitEntry.entryID = UUID.randomUUID();
                rabbitEntry.entryName = addName.getText().toString();
                if(baseImageUri != null) {
                    rabbitEntry.entryPhLoc = baseImageUri.toString();
                }
                String spinnerSelectedItem = genderSpinner.getSelectedItem().toString();
                rabbitEntry.chooseGender = spinnerSelectedItem;
                rabbitEntry.matedWithOrParents = addMatedWith.getText().toString();
                rabbitEntry.birthDate = birthDate;
                rabbitEntry.matedDate = matingDate;

                Intent alertEventService = new Intent(this, AlertEventService.class);
                PendingIntent readyMatingAlarm = PendingIntent.getBroadcast(this, new Random().nextInt(),alertEventService,0);


                if(getMode != EDIT_EXISTING_ENTRY) {
                    if (spinnerSelectedItem.equals("Female")) {
                        if(rabbitEntry.matedDate != null) {
                            Events giveBirth = new Events();
                            Date UpcomingBirth = new Date(rabbitEntry.matedDate.getTime() + (1000L * 60 * 60 * 24 * 31));
                            giveBirth.eventUUID = UUID.randomUUID();
                            giveBirth.name = rabbitEntry.entryName;
                            giveBirth.secondParent = rabbitEntry.matedWithOrParents;
                            giveBirth.eventString = defaultFormatter.format(UpcomingBirth) + ": " + "Did " + rabbitEntry.entryName + " give birth?";
                            giveBirth.dateOfEvent = UpcomingBirth;
                            giveBirth.typeOfEvent = 0;
                            giveBirth.save();
                            rabbitEntry.firstEvent = giveBirth.eventUUID;

                            alertEventService.putExtra("eventUUID", giveBirth.eventUUID);
                            manager.set(AlarmManager.RTC_WAKEUP, UpcomingBirth.getTime(), readyMatingAlarm);


                            Events readyMating = new Events();
                            Date readyMateDate = new Date(UpcomingBirth.getTime() + (1000L * 60 * 60 * 24 * 66));
                            readyMating.eventUUID = UUID.randomUUID();
                            readyMating.name = rabbitEntry.entryName;
                            readyMating.eventString = defaultFormatter.format(readyMateDate) + ": " + rabbitEntry.entryName + " is ready for mating";
                            readyMating.dateOfEvent = readyMateDate;
                            readyMating.typeOfEvent = 1;
                            readyMating.save();
                            rabbitEntry.secondEvent = readyMating.eventUUID;

                            alertEventService.putExtra("eventUUID", readyMating.eventUUID);
                            manager.set(AlarmManager.RTC_WAKEUP, readyMateDate.getTime(), readyMatingAlarm);
                        }

                    } else if (spinnerSelectedItem.equals("Group")) {
                        if(rabbitEntry.birthDate != null) {
                            Events moveEvent = new Events();
                            Date moveDate = new Date(rabbitEntry.birthDate.getTime() + (1000L * 60 * 60 * 24 * 62));
                            moveEvent.eventUUID = UUID.randomUUID();
                            moveEvent.name = rabbitEntry.entryName;
                            moveEvent.eventString = defaultFormatter.format(moveDate) + ": Was the group " + rabbitEntry.entryName + " moved into another cage?";
                            moveEvent.dateOfEvent = moveDate;
                            moveEvent.typeOfEvent = 2;
                            moveEvent.save();
                            rabbitEntry.firstEvent = moveEvent.eventUUID;

                            alertEventService.putExtra("eventUUID", moveEvent.eventUUID);
                            manager.set(AlarmManager.RTC_WAKEUP, moveDate.getTime(), readyMatingAlarm);


                            Events slaughterEvent = new Events();
                            Date slaughterDate = new Date(rabbitEntry.birthDate.getTime() + (1000L * 60 * 60 * 24 * 124));
                            slaughterEvent.eventUUID = UUID.randomUUID();
                            slaughterEvent.name = rabbitEntry.entryName;
                            slaughterEvent.eventString = defaultFormatter.format(slaughterDate) + ": Was the group " + rabbitEntry.entryName + " slaughtered?";
                            slaughterEvent.dateOfEvent = slaughterDate;
                            slaughterEvent.typeOfEvent = 3;
                            slaughterEvent.save();
                            rabbitEntry.secondEvent = slaughterEvent.eventUUID;

                            alertEventService.putExtra("eventUUID", slaughterEvent.eventUUID);
                            manager.set(AlarmManager.RTC_WAKEUP, slaughterDate.getTime(), readyMatingAlarm);
                        }
                    }
                }

                rabbitEntry.save(databaseWrapper);

            }).build();
            transaction.execute();
            setResult(RESULT_OK);
            finish();
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
            // Create the File where the pho
            // to should go

            File photoFile = null;
            try {
                photoFile = createImageFile(this);
            } catch (IOException ex) {
                android.support.v7.app.AlertDialog.Builder dialog = new android.support.v7.app.AlertDialog.Builder(this);
                dialog.setTitle("Error occurred");
                dialog.show();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.photoprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                this.startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
            //display a picture in intent. Save the uri as a string so i can save it later.
            Glide.with(this).load(photoURI).into(baseImage);


        }
        return photoURI;
    }
}
