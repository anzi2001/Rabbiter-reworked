package com.example.kocja.rabbiter_online.databases;

import android.graphics.Bitmap;

import java.util.Date;
import java.util.UUID;

/**
 * Created by kocja on 23/01/2018.
 */
public class Entry{

    private UUID entryID;
    private String entryName;
    private String entryPhLoc;
    private Bitmap entryBitmap;
    private String matedWithOrParents;
    private String secondParent;
    private String birthDate;
    private String matedDate;
    private String chooseGender;
    private boolean isMerged;
    private boolean isChildMerged;
    private String mergedEntryPhLoc;
    private Bitmap mergedEntryBitmap;
    private String mergedEntryName;
    private String mergedEntry;

    private  int rabbitNumber;
    private  int rabbitDeadNumber;

    private boolean isNotNull(Object object){
        return object != null;
    }
    private boolean isNotEmpty(Object object){
        return !object.toString().isEmpty();
    }

    public void setEntryID(UUID entryID) {
        if(entryID != null){
            this.entryID = entryID;
        }

    }

    public void setEntryName(String entryName) {
        this.entryName = entryName;
    }

    public void setEntryPhLoc(String entryPhLoc) {
        if(isNotNull(entryPhLoc)){
            this.entryPhLoc = entryPhLoc;
        }
    }

    public void setEntryBitmap(Bitmap entryBitmap) {
        this.entryBitmap = entryBitmap;
    }

    public void setMatedWithOrParents(String matedWithOrParents) {
        this.matedWithOrParents = matedWithOrParents;
    }

    public void setSecondParent(String secondParent) {
        this.secondParent = secondParent;
    }

    public void setBirthDate(String birthDate) {
        if(isNotNull(birthDate)){
            this.birthDate = birthDate;
        }

    }

    public void setMatedDate(String matedDate) {
        if(isNotNull(matedDate)){
            this.matedDate = matedDate;
        }

    }

    public void setChooseGender(String chooseGender) {
        this.chooseGender = chooseGender;
    }

    public void setMerged(boolean merged) {
        isMerged = merged;
    }

    public void setChildMerged(boolean childMerged) {
        isChildMerged = childMerged;
    }

    public void setMergedEntryPhLoc(String mergedEntryPhLoc) {
        this.mergedEntryPhLoc = mergedEntryPhLoc;
    }

    public void setMergedEntryBitmap(Bitmap mergedEntryBitmap) {
        this.mergedEntryBitmap = mergedEntryBitmap;
    }

    public void setMergedEntryName(String mergedEntryName) {
        this.mergedEntryName = mergedEntryName;
    }

    public void setMergedEntry(String mergedEntry) {
        this.mergedEntry = mergedEntry;
    }

    public void setRabbitNumber(String rabbitNumberText) {
        if(isNotEmpty(rabbitNumberText)){
            this.rabbitNumber = Integer.parseInt(rabbitNumberText);
        }
    }

    public void setRabbitDeadNumber(String rabbitDeadNumber) {
        if(isNotEmpty(rabbitDeadNumber)){
            this.rabbitDeadNumber = Integer.parseInt(rabbitDeadNumber);
        }
    }

    public UUID getEntryID() {
        return entryID;
    }

    public String getEntryName() {
        return entryName;
    }

    public String getEntryPhLoc() {
        return entryPhLoc;
    }

    public Bitmap getEntryBitmap() {
        return entryBitmap;
    }

    public String getMatedWithOrParents() {
        return matedWithOrParents;
    }

    public String getSecondParent() {
        return secondParent;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public String getMatedDate() {
        return matedDate;
    }

    public String getChooseGender() {
        return chooseGender;
    }

    public boolean isMerged() {
        return isMerged;
    }

    public boolean isChildMerged() {
        return isChildMerged;
    }

    public String getMergedEntryPhLoc() {
        return mergedEntryPhLoc;
    }

    public Bitmap getMergedEntryBitmap() {
        return mergedEntryBitmap;
    }

    public String getMergedEntryName() {
        return mergedEntryName;
    }

    public String getMergedEntry() {
        return mergedEntry;
    }

    public int getRabbitNumber() {
        return rabbitNumber;
    }

    public int getRabbitDeadNumber() {
        return rabbitDeadNumber;
    }
}

