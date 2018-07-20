package com.sunrisers.hp.mychatapp;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

class TabsPagerAdapter extends FragmentPagerAdapter {

    public TabsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {
        switch (i)
        {
            case 0 : requestsFragment rf=new requestsFragment();
                     return rf;
            case 1 : chatsFragment cf=new chatsFragment();
                     return cf;
            case 2 : friendsFragment ff = new friendsFragment();
                     return ff;
            default : return null;

        }
    }

    @Override
    public int getCount() {
        return 3;
    }

    public CharSequence getPageTitle(int position)
    {
        switch(position)
        {
            case 0 : return "Requests";
            case 1 : return "Chats";
            case 2 : return "Friends";
            default : return null;
        }
    }
}
