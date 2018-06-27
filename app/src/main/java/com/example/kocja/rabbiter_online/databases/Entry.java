package com.example.kocja.rabbiter_online.databases;

import android.graphics.Bitmap;

import java.util.UUID;

/**
 * Created by kocja on 23/01/2018.
 */
public class Entry{

    public UUID entryID;

    public String entryName;
    public String entryPhLoc;
    public Bitmap entryBitmap;
    public String matedWithOrParents;
    public String secondParent;

    public String birthDate;
    public String matedDate;
    public String chooseGender;

    public boolean isMerged;
    public boolean isChildMerged;

    public String mergedEntryPhLoc;
    public Bitmap mergedEntryBitmap;
    public String mergedEntryName;
    public String mergedEntry;

    public int rabbitNumber;
    public int rabbitDeadNumber;
}

