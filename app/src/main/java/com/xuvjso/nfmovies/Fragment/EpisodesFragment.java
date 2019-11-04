package com.xuvjso.nfmovies.Fragment;


import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.xuvjso.nfmovies.Activity.MovieDetailActivity;
import com.xuvjso.nfmovies.Adapter.EpisodeRecyclerViewAdapter;
import com.xuvjso.nfmovies.Entity.Episode;
import com.xuvjso.nfmovies.Listener.EpisodeCopyClickListener;
import com.xuvjso.nfmovies.Listener.EpisodePlayClickListener;
import com.xuvjso.nfmovies.R;
import me.yokeyword.fragmentation.SupportFragment;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class EpisodesFragment extends SupportFragment implements EpisodeCopyClickListener, EpisodePlayClickListener {

    private RecyclerView mEpisodeRv;
    private List<Episode> mData = new ArrayList<>();
    private OnFragmentInteractionListener mListener;

    public EpisodesFragment() {
        // Required empty public constructor
    }

    public List<Episode> getmData() {
        return mData;
    }

    // TODO: Rename and change types and number of parameters
    public static EpisodesFragment newInstance(List<Episode> e) {
        EpisodesFragment fragment = new EpisodesFragment();
        Bundle args = new Bundle();
        args.putSerializable("ep", (Serializable) e);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mData = (List<Episode>) getArguments().getSerializable("ep");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_episodes, container, false);
        mEpisodeRv = view.findViewById(R.id.episode_recyclerview);
        EpisodeRecyclerViewAdapter episodeRecyclerViewAdapter = new EpisodeRecyclerViewAdapter(getContext(),
                mData, this, this);
        mEpisodeRv.setAdapter(episodeRecyclerViewAdapter);
        mEpisodeRv.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL,false));
        return view;
    }



    @Override
    public void onPlayClick(Episode e) {
        mListener.onFragmentInteraction(e, MovieDetailActivity.TaskType.PLAY);
    }

    @Override
    public void onCopyClick(Episode e) {
        mListener.onFragmentInteraction(e, MovieDetailActivity.TaskType.COPY);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) activity;
        } else {
            throw new RuntimeException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Episode e, MovieDetailActivity.TaskType taskType);
    }

}
