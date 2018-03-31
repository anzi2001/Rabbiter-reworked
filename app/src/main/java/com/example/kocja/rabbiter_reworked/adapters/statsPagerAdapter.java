package com.example.kocja.rabbiter_reworked.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import com.example.kocja.rabbiter_reworked.fragments.statsBirthFragment;

/**
 * Created by kocja on 06/03/2018.
 */

public class statsPagerAdapter extends FragmentPagerAdapter {
    private final String id;
    public statsPagerAdapter(FragmentManager manager,String entryID){
        super(manager);
        id = entryID;
    }
    @Override
    public int getCount() {
        return 3;
    }
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return statsBirthFragment.createNewFragment(id, 0);
            case 1:
                return statsBirthFragment.createNewFragment(id, 1);

            case 2:
                return statsBirthFragment.createNewFragment(id, 2);
        }
        return null;
    }



    @Override
    public CharSequence getPageTitle(int position){
        return "Page" + position;
    }

}
