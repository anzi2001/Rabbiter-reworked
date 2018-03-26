package com.example.kocja.rabbiter_reworked.fragments;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.kocja.rabbiter_reworked.R;
import com.example.kocja.rabbiter_reworked.databases.Entry_Table;
import com.example.kocja.rabbiter_reworked.databases.Events;
import com.example.kocja.rabbiter_reworked.databases.Events_Table;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.raizlabs.android.dbflow.sql.language.SQLite;

import java.util.UUID;

/**
 * Created by kocja on 06/03/2018.
 */

public class statsBirthFragment extends Fragment {
    private int failedBirths = 0;
    private int successBirths = 0;
    public static statsBirthFragment createNewFragment(String entryID,int page){
        Bundle passBundle = new Bundle();
        passBundle.putString("UUID",entryID);
        passBundle.putInt("type",page);
        statsBirthFragment newFragment = new statsBirthFragment();
        newFragment.setArguments(passBundle);

        return newFragment;

    }
    @SuppressLint("StaticFieldLeak")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View birthStats = inflater.inflate(R.layout.stats_birth_fragment,container,false);
        String entryID = getArguments().getString("UUID");
        SQLite.select()
                .from(com.example.kocja.rabbiter_reworked.databases.Entry.class)
                .where(Entry_Table.entryID.eq(UUID.fromString(entryID)))
                .async()
                .querySingleResultCallback((transaction, entry) -> {
                    final GraphView birthGraph = birthStats.findViewById(R.id.BirthChart);
                    final GraphView deadRabbitsGraph = birthStats.findViewById(R.id.deadChart);
                    final TextView successfulBirths = birthStats.findViewById(R.id.successBirths);
                    final TextView failedBirthsText = birthStats.findViewById(R.id.failedBirthsText);

                    final LineGraphSeries<DataPoint> numBirthsSeries = new LineGraphSeries<>();
                    final LineGraphSeries<DataPoint> averageBirthSeries = new LineGraphSeries<>();
                    final LineGraphSeries<DataPoint> numDeathSeries = new LineGraphSeries<>();
                    final LineGraphSeries<DataPoint> avgDeadRabbits = new LineGraphSeries<>();

                    SQLite.select()
                            .from(Events.class)
                            .where(Events_Table.name.eq(entry.entryName))
                            .and(Events_Table.typeOfEvent.eq(0))
                            .or(Events_Table.secondParent.eq(entry.entryName))
                            .and(Events_Table.typeOfEvent.eq(0))
                            .orderBy(Events_Table.dateOfEvent,true)
                            .async()
                            .queryListResultCallback((transaction1, tResult) -> new AsyncTask<Void,Void,Void>(){
                                @Override
                                protected Void doInBackground(Void... voids) {

                                    float avgRabbitsNum = 0;
                                    float deadRabbitsNum = 0;

                                    for(Events singleEvent : tResult){
                                        if(singleEvent.notificationState == Events.EVENT_SUCCESSFUL){
                                            successBirths++;
                                        }
                                        else{
                                            failedBirths++;
                                        }
                                        avgRabbitsNum += singleEvent.rabbitsNum;
                                        deadRabbitsNum += singleEvent.numDead;

                                        numBirthsSeries.appendData(new DataPoint(singleEvent.dateOfEvent.getTime(),singleEvent.rabbitsNum),true,50);
                                    }
                                    avgRabbitsNum /= tResult.size();
                                    deadRabbitsNum /= tResult.size();

                                    for(Events singleEvent : tResult) {
                                        averageBirthSeries.appendData(new DataPoint(singleEvent.dateOfEvent.getTime(),avgRabbitsNum),true,50);
                                    }


                                    for(Events deadNumEvent : tResult){
                                        numDeathSeries.appendData(new DataPoint(deadNumEvent.dateOfEvent.getTime(),deadNumEvent.numDead),true,50);
                                        avgDeadRabbits.appendData(new DataPoint(deadNumEvent.dateOfEvent.getTime(),deadRabbitsNum),true,50);
                                    }

                                    numBirthsSeries.setColor(Color.BLUE);

                                    averageBirthSeries.setColor(Color.YELLOW);


                                    numDeathSeries.setColor(Color.BLUE);
                                    avgDeadRabbits.setColor(Color.YELLOW);


                                    return null;
                                }

                                @Override
                                protected void onPostExecute(Void aVoid) {
                                    deadRabbitsGraph.addSeries(numDeathSeries);
                                    deadRabbitsGraph.addSeries(avgDeadRabbits);

                                    birthGraph.addSeries(averageBirthSeries);
                                    birthGraph.addSeries(numBirthsSeries);

                                    birthGraph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(getActivity()));
                                    birthGraph.getGridLabelRenderer().setNumHorizontalLabels(3);
                                    deadRabbitsGraph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(getActivity()));
                                    deadRabbitsGraph.getGridLabelRenderer().setNumHorizontalLabels(3);
                                    failedBirthsText.setText(String.valueOf(failedBirths));
                                    successfulBirths.setText(String.valueOf(successBirths));
                                    super.onPostExecute(aVoid);
                                }
                            }.execute()).execute();

                }).execute();



        return birthStats;

    }
}
