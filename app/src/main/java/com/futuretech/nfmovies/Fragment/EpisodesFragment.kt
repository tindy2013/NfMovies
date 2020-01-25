package com.futuretech.nfmovies.Fragment


import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.futuretech.nfmovies.Activity.MovieDetailActivity
import com.futuretech.nfmovies.Adapter.EpisodeRecyclerViewAdapter
import com.futuretech.nfmovies.Entity.Episode
import com.futuretech.nfmovies.Listener.EpisodeCastClickListener
import com.futuretech.nfmovies.Listener.EpisodeCopyClickListener
import com.futuretech.nfmovies.Listener.EpisodePlayClickListener
import com.futuretech.nfmovies.R
import me.yokeyword.fragmentation.SupportFragment

import java.io.Serializable
import java.util.ArrayList


class EpisodesFragment : SupportFragment(), EpisodeCopyClickListener, EpisodePlayClickListener, EpisodeCastClickListener {

    private var mEpisodeRv: RecyclerView? = null
    private var mData: List<Episode> = ArrayList()
    private var mListener: OnFragmentInteractionListener? = null

    fun getmData(): List<Episode> {
        return mData
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mData = arguments!!.getSerializable("ep") as List<Episode>
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_episodes, container, false)
        mEpisodeRv = view.findViewById(R.id.episode_recyclerview)
        val episodeRecyclerViewAdapter = EpisodeRecyclerViewAdapter(context!!,
                mData, this, this, this)
        mEpisodeRv!!.adapter = episodeRecyclerViewAdapter
        mEpisodeRv!!.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        return view
    }


    override fun onPlayClick(e: Episode) {
        mListener!!.onFragmentInteraction(e, MovieDetailActivity.TaskType.PLAY)
    }

    override fun onCopyClick(e: Episode) {
        mListener!!.onFragmentInteraction(e, MovieDetailActivity.TaskType.COPY)
    }

    override fun OnCastClick(e: Episode) {
        mListener!!.onFragmentInteraction(e, MovieDetailActivity.TaskType.CAST)
    }

    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        if (activity is OnFragmentInteractionListener) {
            mListener = activity
        } else {
            throw RuntimeException("$activity must implement OnFragmentInteractionListener")
        }
    }


    interface OnFragmentInteractionListener {
        fun onFragmentInteraction(e: Episode, taskType: MovieDetailActivity.TaskType)
    }

    companion object {

        fun newInstance(e: List<Episode>): EpisodesFragment {
            val fragment = EpisodesFragment()
            val args = Bundle()
            args.putSerializable("ep", e as Serializable)
            fragment.arguments = args
            return fragment
        }
    }

}// Required empty public constructor
