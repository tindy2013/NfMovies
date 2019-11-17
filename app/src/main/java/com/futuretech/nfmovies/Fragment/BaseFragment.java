package com.futuretech.nfmovies.Fragment;

import android.app.ActivityOptions;
import android.content.Intent;
import android.widget.ImageView;

import com.futuretech.nfmovies.Activity.MovieDetailActivity;
import com.futuretech.nfmovies.Listener.MovieItemClickListener;
import com.futuretech.nfmovies.Entity.Movie;

import me.yokeyword.fragmentation.SupportFragment;

public class BaseFragment extends SupportFragment implements MovieItemClickListener {

    @Override
    public void onMovieClick(Movie movie, ImageView img) {
        Intent intent = new Intent(getContext(), MovieDetailActivity.class);
        intent.putExtra("movie", movie);
        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(
                getActivity(), img, "sharedImg"
        );
        startActivity(intent, options.toBundle());
    }
}
