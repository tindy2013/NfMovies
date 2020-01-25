package com.futuretech.nfmovies.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.futuretech.nfmovies.Entity.Movie
import com.futuretech.nfmovies.Listener.MovieItemClickListener
import com.futuretech.nfmovies.R
import com.futuretech.nfmovies.Utils.ImageUtil

class MovieGridViewAdapter : BaseAdapter {
    var movies: List<Movie>? = null
        private set
    private var context: Context? = null
    private var clickListener: MovieItemClickListener? = null

    constructor(context: Context, movies: List<Movie>, clickListener: MovieItemClickListener) {
        this.movies = movies
        this.context = context
        this.clickListener = clickListener
    }

    constructor(context: Context, clickListener: MovieItemClickListener) {
        this.context = context
        this.movies = null
        this.clickListener = clickListener
    }

    override fun getCount(): Int {
        return movies!!.size
    }

    override fun getItem(position: Int): Any {
        return movies!![position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertview: View?, parent: ViewGroup): View {
        var convertView = convertview
        val viewHolder: ViewHolder
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.movie_item, null)
            viewHolder = ViewHolder(convertView!!)
        } else {
            viewHolder = convertView.tag as ViewHolder
        }
        viewHolder.movieName.text = movies!![position].name
        ImageUtil.display(context!!, movies!![position].img, viewHolder.movieImg, null)
        viewHolder.movieImg.setOnClickListener { clickListener!!.onMovieClick(movies!![position], viewHolder.movieImg) }
        convertView.tag = viewHolder

        return convertView
    }

    internal inner class ViewHolder(convertView: View) {
        val movieImg: ImageView
        val movieName: TextView

        init {
            movieImg = convertView.findViewById(R.id.movie_img)
            movieName = convertView.findViewById(R.id.movie_name)
        }
    }
}
