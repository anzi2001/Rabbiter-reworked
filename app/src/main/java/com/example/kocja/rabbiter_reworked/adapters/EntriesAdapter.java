package com.example.kocja.rabbiter_reworked.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.kocja.rabbiter_reworked.R;
import com.example.kocja.rabbiter_reworked.databases.Entry;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * Created by kocja on 04/02/2018.
 */
public class EntriesAdapter extends BaseAdapter {
    private final List<Entry> displayedEntries;
    private final Context mainContext;
    private final LayoutInflater inflater;
    public EntriesAdapter(Context context, List<Entry> entries){
        displayedEntries = entries;
        mainContext = context;
        inflater =(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    @Override
    public int getCount() {
        return displayedEntries.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View mainView;
        if(view == null) {
            Entry singleEntry = displayedEntries.get(i);
            mainView = inflater.inflate(R.layout.entries_adapter_entry, viewGroup, false);
            CircleImageView entryImage = mainView.findViewById(R.id.entryImage);
            CircleImageView markedOrNot = mainView.findViewById(R.id.MarkedOrNot);
            TextView textName = mainView.findViewById(R.id.textName);

            Glide.with(mainContext)
                    .load(R.drawable.ic_markedornot)
                    .into(markedOrNot);

            Glide.with(mainContext)
                    .load(singleEntry.entryPhLoc)
                    .into(entryImage);

            entryImage.setBorderWidth(6);
            switch (singleEntry.chooseGender) {
                case "Female":
                    entryImage.setBorderColor(Color.parseColor("#EC407A"));
                    break;
                case "Male":
                    entryImage.setBorderColor(Color.BLUE);
                    break;
                default:
                    entryImage.setBorderColor(Color.WHITE);
                    break;
            }


            if(singleEntry.isMerged) {
                CircleImageView mergedImage = mainView.findViewById(R.id.mergedImage);
                mergedImage.setVisibility(View.VISIBLE);

                mergedImage.setBorderWidth(4);
                mergedImage.setBorderColor(Color.WHITE);

                Glide.with(mainContext)
                        .load(singleEntry.mergedEntryPhLoc)
                        .into(mergedImage);

                textName.setText(singleEntry.entryName + ", " + singleEntry.mergedEntryName);
            }
            else{
                textName.setText(singleEntry.entryName);
            }

            mainView.setTag(singleEntry.entryID);
        }
        else{
            mainView = view;
        }

        return mainView;
    }
}
