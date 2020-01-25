package com.futuretech.nfmovies.Fragment

import android.app.ActivityOptions
import android.content.Intent
import android.os.Build
import android.widget.ImageView

import com.futuretech.nfmovies.Activity.MovieDetailActivity
import com.futuretech.nfmovies.Listener.MovieItemClickListener
import com.futuretech.nfmovies.Entity.Movie

import me.yokeyword.fragmentation.SupportFragment

open class BaseFragment : SupportFragment(), MovieItemClickListener {

    override fun onMovieClick(movie: Movie, img: ImageView) {
        val intent = Intent(context, MovieDetailActivity::class.java)
        intent.putExtra("movie", movie)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val options = ActivityOptions.makeSceneTransitionAnimation(activity, img, "sharedImg")
            startActivity(intent, options.toBundle())
        } else {
            startActivity(intent)
        }
    }
}
