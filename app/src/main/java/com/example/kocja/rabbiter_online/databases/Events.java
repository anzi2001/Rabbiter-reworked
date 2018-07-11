package com.example.kocja.rabbiter_online.databases;

import java.util.UUID;

/**
 * Created by kocja on 28/01/2018.
 */
public class Events {
    public static final int EVENT_FAILED = -1;
    public static final int NOT_YET_ALERTED = 0;
    public static final int EVENT_SUCCESSFUL = 1;
    public static final int BIRTH_EVENT = 0;
    public static final int READY_MATING_EVENT = 1;
    public static final int MOVE_GROUP_EVENT = 2;
    public static final int SLAUGHTER_EVENT = 3;
    private UUID eventUUID;
    private String name;
    private String secondParent;
    private String eventString;
    private String dateOfEvent;
    private long dateOfEventMilis;
    private int rabbitsNum;
    private int numDead;
    private int id;
    private int typeOfEvent;
    private int timesNotified;
    private int notificationState;
    private boolean isNotNull(Object object){
        return object != null;
    }
    private boolean isNotEmpty(Object object){
        return !object.toString().isEmpty();
    }

    public void setEventUUID(UUID eventUUID) {
        this.eventUUID = eventUUID;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSecondParent(String secondParent) {
        this.secondParent = secondParent;
    }

    public void setEventString(String eventString) {
        this.eventString = eventString;
    }

    public void setDateOfEvent(String dateOfEvent) {
        this.dateOfEvent = dateOfEvent;
    }

    public void setDateOfEventMilis(long dateOfEventMilis){
        this.dateOfEventMilis = dateOfEventMilis;
    }

    public void setRabbitsNum(String rabbitsString,int type) {
        if(type == 0 && isNotEmpty(rabbitsString)){
            this.rabbitsNum = Integer.parseInt(rabbitsString);
        }

    }

    public void setNumDead(String numDeadString,int type) {
        if(type == 0 && isNotEmpty(numDeadString)){
            this.numDead = Integer.parseInt(numDeadString);
        }

    }

    public void setId(int id) {
        this.id = id;
    }

    public void setTypeOfEvent(int typeOfEvent) {
        this.typeOfEvent = typeOfEvent;
    }

    public void setTimesNotified(int timesNotified) {
        this.timesNotified = timesNotified;
    }

    public void setNotificationState(int notificationState) {
        this.notificationState = notificationState;
    }

    public UUID getEventUUID() {
        return eventUUID;
    }

    public String getName() {
        return name;
    }

    public String getSecondParent() {
        return secondParent;
    }

    public String getEventString() {
        return eventString;
    }

    public String getDateOfEvent() {
        return dateOfEvent;
    }

    public long getDateOfEventMilis(){
        return dateOfEventMilis;
    }

    public int getRabbitsNum() {
        return rabbitsNum;
    }

    public int getNumDead() {
        return numDead;
    }

    public int getId() {
        return id;
    }

    public int getTypeOfEvent() {
        return typeOfEvent;
    }

    public int getTimesNotified() {
        return timesNotified;
    }

    public int getNotificationState() {
        return notificationState;
    }

}
