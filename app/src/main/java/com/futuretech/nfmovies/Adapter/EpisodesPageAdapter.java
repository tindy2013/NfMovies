package com.futuretech.nfmovies.Adapter;

import android.content.Context;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.List;

public class EpisodesPageAdapter extends FragmentPagerAdapter {

    private List<Fragment> fragments;
    private List<String> origins;
    private Context context;

    public EpisodesPageAdapter(Context context, FragmentManager manager, List<Fragment> fragment,
                               List<String> title) {
        super(manager);
        this.context = context;
        this.fragments = fragment;
        this.origins = title;
    }


    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public int getCount() {
        return origins.size();
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return origins.get(position);
    }
}
