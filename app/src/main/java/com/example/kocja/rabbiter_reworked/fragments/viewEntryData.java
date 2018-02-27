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

import java.text.SimpleDateFormat;
import java.util.Date;
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
    private SimpleDateFormat fragmentFormat;
    View mainView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mainView = inflater.inflate(R.layout.fragment_view_entry_data,container,false);
        fragmentFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.GERMANY);
        entryName = mainView.findViewById(R.id.entryName);
        entryGender = mainView.findViewById(R.id.entryGender);
        entryBirthDate = mainView.findViewById(R.id.entryBirthDate);
        matedDateText = mainView.findViewById(R.id.entryMatedDateOrParents);
        matedWithText = mainView.findViewById(R.id.entryMatedWith);
        return mainView;
    }
    public void setData(String name, String gender, Date birthDate, Date matedDate, String matedWith){
        entryName.setText(name);
        entryGender.setText(gender);
        if(birthDate != null) {
            entryBirthDate.setText(fragmentFormat.format(birthDate));
        }
        matedWithText.setText(matedWith);
        if(matedDate != null && gender.equals("Group")){
            TextView parents = mainView.findViewById(R.id.MatedDateOrParents);
            parents.setText("Parents: ");
            matedDateText.setText(fragmentFormat.format(matedDate));
        }
        else if(matedDate != null) {
            matedDateText.setText(fragmentFormat.format(matedDate));
        }

    }
}
