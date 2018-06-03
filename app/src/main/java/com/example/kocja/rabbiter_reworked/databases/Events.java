package com.example.kocja.rabbiter_reworked.databases;

import java.util.UUID;

/**
 * Created by kocja on 28/01/2018.
 */
public class Events {
    public static final int EVENT_FAILED = -1;
    public static final int NOT_YET_ALERTED = 0;
    public static final int EVENT_SUCCESSFUL = 1;

    public UUID eventUUID;

    public String name;
    public String secondParent;
    public String eventString;

    public String dateOfEvent;

    public int rabbitsNum;
    public int numDead;
    public int id;
    public int typeOfEvent;
    public int timesNotified;
    public int notificationState;

}
