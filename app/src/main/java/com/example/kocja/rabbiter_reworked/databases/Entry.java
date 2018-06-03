package com.example.kocja.rabbiter_reworked.databases;

import java.util.UUID;

/**
 * Created by kocja on 23/01/2018.
 */
public class Entry{

    public UUID entryID;

    public String entryName;
    public String entryPhLoc;
    public String matedWithOrParents;
    public String secondParent;

    public String birthDate;
    public String matedDate;
    public String chooseGender;

    public boolean isMerged;
    public boolean isChildMerged;

    public String mergedEntryPhLoc;
    public String mergedEntryName;
    public Entry mergedEntry;

    public int rabbitNumber;
    public int rabbitDeadNumber;
}

