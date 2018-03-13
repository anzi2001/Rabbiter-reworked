package com.example.kocja.rabbiter_reworked.fragments;

import android.graphics.Color;
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
    public static statsBirthFragment createNewFragment(String entryID,int page){
        Bundle passBundle = new Bundle();
        passBundle.putString("UUID",entryID);
        passBundle.putInt("type",page);
        statsBirthFragment newFragment = new statsBirthFragment();
        newFragment.setArguments(passBundle);

        return newFragment;

    }
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View birthStats = inflater.inflate(R.layout.stats_birth_fragment,container,false);
        String entryID = getArguments().getString("UUID");
        SQLite.select()
                .from(com.example.kocja.rabbiter_reworked.databases.Entry.class)
                .where(Entry_Table.entryID.eq(UUID.fromString(entryID)))
                .async()
                .querySingleResultCallback((transaction, entry) -> {
                    GraphView birthGraph = birthStats.findViewById(R.id.BirthChart);
                    TextView birthNum = birthStats.findViewById(R.id.birthNum);
                    TextView deadNum = birthStats.findViewById(R.id.deadNum);

                    SQLite.select()
                            .from(Events.class)
                            .where(Events_Table.name.eq(entry.entryName))
                            .or(Events_Table.secondParent.eq(entry.entryName))
                            .orderBy(Events_Table.dateOfEvent,true)
                            .async()
                            .queryListResultCallback((transaction1, tResult) -> {

                                birthNum.setText(tResult.get(0).rabbitsNum);
                                deadNum.setText(tResult.get(0).numDead);

                                LineGraphSeries NumbirthsSeries = new LineGraphSeries<>();
                                LineGraphSeries averageBirthSeries = new LineGraphSeries();

                                float avgRabbitsNum = 0;
                                for(Events singleEvent : tResult){
                                    avgRabbitsNum += singleEvent.rabbitsNum;
                                    NumbirthsSeries.appendData(new DataPoint(singleEvent.rabbitsNum,singleEvent.dateOfEvent.getTime()),true,50);
                                }
                                avgRabbitsNum /= tResult.size();
                                for(Events singleEvent : tResult) {
                                    averageBirthSeries.appendData(new DataPoint(avgRabbitsNum,singleEvent.dateOfEvent.getTime()),true,50);
                                }
                                NumbirthsSeries.setColor(Color.BLUE);
                                birthGraph.addSeries(NumbirthsSeries);
                                averageBirthSeries.setColor(Color.YELLOW);
                                birthGraph.addSeries(averageBirthSeries);
                                birthGraph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(getActivity()));
                            }).execute();

                }).execute();



        return birthStats;


    }
}
