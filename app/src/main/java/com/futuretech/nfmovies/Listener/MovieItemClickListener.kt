package com.futuretech.nfmovies.Listener

import android.widget.ImageView
import com.futuretech.nfmovies.Entity.Movie

interface MovieItemClickListener {
    fun onMovieClick(movie: Movie, img: ImageView)
}
