package com.example.kocja.rabbiter_reworked.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.kocja.rabbiter_reworked.R;
import com.example.kocja.rabbiter_reworked.databases.Entry;

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
    private TextView matedWithText;
    private final SimpleDateFormat fragmentFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.GERMANY);
    private View mainView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mainView = inflater.inflate(R.layout.fragment_view_entry_data,container,false);
        entryName = mainView.findViewById(R.id.entryName);
        entryGender = mainView.findViewById(R.id.entryGender);
        entryBirthDate = mainView.findViewById(R.id.entryBirthDate);
        matedDateText = mainView.findViewById(R.id.entryMatedDateOrParents);
        matedWithText = mainView.findViewById(R.id.entryMatedWith);
        return mainView;
    }
    public void setData(Entry entry){
        entryName.setText(entry.entryName);
        entryGender.setText(entry.chooseGender);
        if(entry.birthDate != null) {
            entryBirthDate.setText(fragmentFormat.format(entry.birthDate));
        }
        matedWithText.setText(entry.matedWithOrParents);
        if(entry.matedDate != null && entry.chooseGender.equals("Group")){
            TextView parents = mainView.findViewById(R.id.MatedDateOrParents);
            parents.setText("Parents: ");
            matedDateText.setText(entry.matedWithOrParents + entry.secondParent);
            TextView matedWithString = mainView.findViewById(R.id.matedWith);
            matedWithString.setVisibility(View.GONE);
            matedWithText.setVisibility(View.GONE);

        }
        else if(entry.matedDate != null) {
            matedDateText.setText(fragmentFormat.format(entry.matedDate));
        }

    }
}
