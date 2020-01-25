package com.futuretech.nfmovies.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.futuretech.nfmovies.Entity.Movie
import com.futuretech.nfmovies.Listener.MovieItemClickListener
import com.futuretech.nfmovies.R
import com.futuretech.nfmovies.Utils.ImageUtil

class MovieRecyclerViewAdapter(private val context: Context?, private val movies: List<Movie>, internal var movieItemClickListener: MovieItemClickListener) : RecyclerView.Adapter<MovieRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.movie_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.name.text = movies[position].name
        ImageUtil.display(context!!, movies[position].img, holder.img, null)
    }

    override fun getItemCount(): Int {
        return movies.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView
        val img: ImageView

        init {
            name = itemView.findViewById(R.id.movie_name)
            img = itemView.findViewById(R.id.movie_img)
            itemView.setOnClickListener { movieItemClickListener.onMovieClick(movies[adapterPosition], img) }
        }
    }
}
