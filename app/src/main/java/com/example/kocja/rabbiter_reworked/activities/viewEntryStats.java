package com.example.kocja.rabbiter_reworked.activities;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.example.kocja.rabbiter_reworked.GsonManager;
import com.example.kocja.rabbiter_reworked.R;
import com.example.kocja.rabbiter_reworked.SocketIOManager;
import com.example.kocja.rabbiter_reworked.databases.Entry;
import com.example.kocja.rabbiter_reworked.databases.Events;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.UUID;

import io.socket.client.Socket;

/**
 * Created by kocja on 06/03/2018.
 */

public class viewEntryStats extends AppCompatActivity {
    private int failedBirths = 0;
    private int successBirths = 0;
    SimpleDateFormat formatter;

    @SuppressLint("StaticFieldLeak")
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stats_birth_fragment);
        UUID entryID =(UUID) getIntent().getSerializableExtra("entryUUID");

        formatter = new SimpleDateFormat("dd/MM/yyyy", Locale.GERMAN);

        Socket socket = SocketIOManager.getSocket();
        socket.emit("seekSingleReq",entryID);
        socket.on("seekSingleRes", args -> {
            Entry entry = GsonManager.getGson().fromJson((JsonObject)args[0],Entry.class);
            final GraphView birthGraph = findViewById(R.id.BirthChart);
            final GraphView deadRabbitsGraph = findViewById(R.id.deadChart);
            final TextView successfulBirths = findViewById(R.id.successBirths);
            final TextView failedBirthsText = findViewById(R.id.failedBirthsText);

            final LineGraphSeries<DataPoint> numBirthsSeries = new LineGraphSeries<>();
            final LineGraphSeries<DataPoint> averageBirthSeries = new LineGraphSeries<>();
            final LineGraphSeries<DataPoint> numDeathSeries = new LineGraphSeries<>();
            final LineGraphSeries<DataPoint> avgDeadRabbits = new LineGraphSeries<>();

            socket.emit("seekEventsByNameTypeReq",entry.entryName);
            socket.on("seekEventsByNameTypeRes", args1 -> {
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... voids) {
                        ArrayList<Events> result = new ArrayList<>();

                        result.add(GsonManager.getGson().fromJson((JsonObject)args1[0],Events.class));
                        float avgRabbitsNum = 0;
                        float deadRabbitsNum = 0;

                        for (Events singleEvent : result) {
                            if (singleEvent.notificationState == Events.EVENT_SUCCESSFUL) {
                                successBirths++;
                            } else {
                                failedBirths++;
                            }
                            avgRabbitsNum += singleEvent.rabbitsNum;
                            deadRabbitsNum += singleEvent.numDead;

                            try {
                                numBirthsSeries.appendData(new DataPoint(formatter.parse(singleEvent.dateOfEvent).getTime(), singleEvent.rabbitsNum), true, 50);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }

                        }
                        avgRabbitsNum /= result.size();
                        deadRabbitsNum /= result.size();

                        for (Events singleEvent : result) {
                            try {
                                averageBirthSeries.appendData(new DataPoint(formatter.parse(singleEvent.dateOfEvent).getTime(), avgRabbitsNum), true, 50);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }

                        }


                        for (Events deadNumEvent : result) {
                            try {
                                numDeathSeries.appendData(new DataPoint(formatter.parse(deadNumEvent.dateOfEvent).getTime(), deadNumEvent.numDead), true, 50);
                                avgDeadRabbits.appendData(new DataPoint(formatter.parse(deadNumEvent.dateOfEvent).getTime(), deadRabbitsNum), true, 50);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }

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

                        birthGraph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(viewEntryStats.this));
                        birthGraph.getGridLabelRenderer().setNumHorizontalLabels(3);
                        deadRabbitsGraph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(viewEntryStats.this));
                        deadRabbitsGraph.getGridLabelRenderer().setNumHorizontalLabels(3);
                        failedBirthsText.setText(String.valueOf(failedBirths));
                        successfulBirths.setText(String.valueOf(successBirths));
                        super.onPostExecute(aVoid);
                    }
                }.execute();
            });
        });
        /*
        SQLite.select()
                .from(com.example.kocja.rabbiter_reworked.databases.Entry.class)
                .where(Entry_Table.entryID.eq(entryID))
                .async()
                .querySingleResultCallback((transaction, entry) -> {


                            SQLite.select()
                                    .from(Events.class)
                                    .where(Events_Table.name.eq(entry.entryName))
                                    .and(Events_Table.typeOfEvent.eq(0))
                                    .or(Events_Table.secondParent.eq(entry.entryName))
                                    .and(Events_Table.typeOfEvent.eq(0))
                                    .orderBy(Events_Table.dateOfEvent, true)
                                    .async()
                                    .queryListResultCallback((transaction1, tResult) ->
                     *
                }).execute();
          */
    }
}
