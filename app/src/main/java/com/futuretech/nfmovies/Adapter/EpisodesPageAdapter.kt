package com.futuretech.nfmovies.Adapter

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class EpisodesPageAdapter(private val context: Context, manager: FragmentManager, private val fragments: List<Fragment>,
                          private val origins: List<String>) : FragmentPagerAdapter(manager) {


    override fun getItem(position: Int): Fragment {
        return fragments[position]
    }

    override fun getCount(): Int {
        return origins.size
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return origins[position]
    }
}
