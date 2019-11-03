package com.xuvjso.nfmovies.Listener;

import android.widget.ImageView;
import com.xuvjso.nfmovies.Entity.Movie;

public interface MovieItemClickListener {
    void onMovieClick(Movie movie, ImageView img);
}
