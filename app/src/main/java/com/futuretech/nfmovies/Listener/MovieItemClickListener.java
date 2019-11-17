package com.futuretech.nfmovies.Listener;

import android.widget.ImageView;
import com.futuretech.nfmovies.Entity.Movie;

public interface MovieItemClickListener {
    void onMovieClick(Movie movie, ImageView img);
}
