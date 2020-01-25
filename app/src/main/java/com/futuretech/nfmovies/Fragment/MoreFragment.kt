package com.futuretech.nfmovies.Fragment


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.futuretech.nfmovies.R
import me.yokeyword.fragmentation.SupportFragment

/**
 * A simple [Fragment] subclass.
 * Use the [MoreFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MoreFragment : SupportFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_more, container, false)
        childFragmentManager.beginTransaction().replace(R.id.setting_container, SettingFragment()).commit()
        return view
    }

    companion object {

        fun newInstance(): MoreFragment {
            return MoreFragment()
        }
    }
}// Required empty public constructor
