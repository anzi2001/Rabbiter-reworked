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
    public static final int EVENT_FAILED = -1;
    public static final int NOT_YET_ALERTED = 0;
    public static final int EVENT_SUCCESSFUL = 1;
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
    @Column
    public int notificationState;

}
