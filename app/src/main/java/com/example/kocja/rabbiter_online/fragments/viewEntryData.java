package com.example.kocja.rabbiter_online.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.kocja.rabbiter_online.R;
import com.example.kocja.rabbiter_online.databases.Entry;

import java.text.SimpleDateFormat;
import java.util.Locale;


/**
 * Created by kocja on 29/01/2018.
 */

public class viewEntryData extends Fragment {
    private TextView entryName;
    private TextView entryGender;
    private TextView entryBirthDate;
    private TextView matedDateText;
    private TextView entryMatedWith;
    private TextView rabbitAge;
    private final SimpleDateFormat fragmentFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.GERMANY);
    private View mainView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mainView = inflater.inflate(R.layout.fragment_view_entry_data,container,false);
        entryName = mainView.findViewById(R.id.entryName);
        entryGender = mainView.findViewById(R.id.entryGender);
        entryBirthDate = mainView.findViewById(R.id.entryBirthDate);
        matedDateText = mainView.findViewById(R.id.entryMatedDateOrParents);
        entryMatedWith = mainView.findViewById(R.id.entryMatedWith);
        rabbitAge = mainView.findViewById(R.id.RabbitAge);
        //rabbitNumText = mainView.findViewById(R.id.groupRabbitNumText);
        //rabbitNum = mainView.findViewById(R.id.groupRabbitNum);
        return mainView;
    }
    public void setData(Entry entry){
        entryName.setText(entry.entryName);
        entryGender.setText(entry.chooseGender);
        if(entry.birthDate != null) {
            entryBirthDate.setText(entry.birthDate);
        }
        entryMatedWith.setText(entry.matedWithOrParents);

        TextView matedWithText = mainView.findViewById(R.id.matedWith);


        if(entry.matedDate != null && entry.chooseGender.equals("Group")){
            TextView parents = mainView.findViewById(R.id.MatedDateOrParents);
            parents.setText(getString(R.string.setParents));
            matedDateText.setText(getString(R.string.Parents,entry.matedWithOrParents, entry.secondParent));
            //entryMatedWith.setVisibility(View.GONE);

        }

        else if(entry.matedDate != null) {
            matedDateText.setText(entry.matedDate);
        }
        /*if(!entry.birthDate.equals("")) {
            long ageDate = 0;

            try {
                ageDate = TimeUnit.DAYS.convert((new Date().getTime() - fragmentFormat.parse(entry.birthDate).getTime()),TimeUnit.MILLISECONDS);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            long years = ageDate / 365;
            ageDate = ageDate % 365;
            long months = ageDate / 30;
            ageDate = ageDate %30;
            rabbitAge.setText(getString(R.string.setAge, years, months, ageDate));
        }*/
        if(entry.chooseGender.equals("Group")) {
            /*rabbitNumText.setVisibility(View.VISIBLE);
            rabbitNum.setVisibility(View.VISIBLE);
            rabbitNum.setText(Integer.toString(entry.rabbitNumber));*/
            entryMatedWith.setText(String.valueOf(entry.rabbitNumber));
            matedWithText.setText(getString(R.string.entryRabbitNum));
        }
    }
}
