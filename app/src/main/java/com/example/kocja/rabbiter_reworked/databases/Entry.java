package com.example.kocja.rabbiter_reworked.databases;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

import java.util.Date;
import java.util.UUID;

/**
 * Created by kocja on 23/01/2018.
 */
@Table(database = appDatabase.class)
public class Entry extends BaseModel{
    @PrimaryKey
    public UUID entryID;
    @Column
    public String entryName;
    @Column
    public String entryPhLoc;
    @Column
    public String matedWithOrParents;
    @Column
    public Date birthDate;
    @Column
    public Date matedDate;
    @Column
    public String chooseGender;
    @Column
    public boolean isMerged;
    @Column
    public boolean isChildMerged;
    @Column
    public String mergedEntryPhLoc;
    @Column
    public String mergedEntryName;
    @ForeignKey(stubbedRelationship = true)
    public Entry mergedEntry;
    @Column
    public UUID firstEvent;
    @Column
    public UUID secondEvent;
}

