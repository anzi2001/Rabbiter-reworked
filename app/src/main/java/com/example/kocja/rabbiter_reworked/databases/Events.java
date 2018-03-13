package com.example.kocja.rabbiter_reworked.databases;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

import java.util.Date;
import java.util.UUID;

/**
 * Created by kocja on 28/01/2018.
 */
@Table(database = appDatabase.class)
public class Events extends BaseModel {
    @PrimaryKey
    public UUID eventUUID;
    @Column
    public String name;
    @Column
    public String secondParent;
    @Column
    public String eventString;
    @Column
    public Date dateOfEvent;
    @Column
    public int rabbitsNum;
    @Column
    public int numDead;
    @Column
    public int id;
    @Column
    public int typeOfEvent;
    @Column
    public int timesNotified;
    @Column(defaultValue = "false")
    public boolean yesClicked;
}
