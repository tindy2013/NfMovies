package com.futuretech.nfmovies.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.futuretech.nfmovies.*
import com.futuretech.nfmovies.Entity.Category
import com.futuretech.nfmovies.Listener.CategoryMoreClickListener
import com.futuretech.nfmovies.Listener.MovieItemClickListener

class CategoryRecyclerViewAdapter : RecyclerView.Adapter<CategoryRecyclerViewAdapter.ViewHolder> {
    private var context: Context? = null
    private var categories: List<Category>? = null
    private var movieItemClicklistener: MovieItemClickListener
    private var moreClickListener: CategoryMoreClickListener? = null

    constructor(context: Context, categories: List<Category>, movieItemClicklistener: MovieItemClickListener, moreClickListener: CategoryMoreClickListener?) {
        this.context = context
        this.categories = categories
        this.movieItemClicklistener = movieItemClicklistener
        this.moreClickListener = moreClickListener
    }

    constructor(context: Context, movieItemClicklistener: MovieItemClickListener, moreClickListener: CategoryMoreClickListener) {
        this.context = context
        this.movieItemClicklistener = movieItemClicklistener
        this.moreClickListener = moreClickListener
    }

    fun setCategories(categories: List<Category>) {
        this.categories = categories
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.category_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.title.text = categories!![position].title
        val movies = categories!![position].movies
        val adapter = MovieRecyclerViewAdapter(context, movies, movieItemClicklistener)
        holder.moviesRecyclerView.adapter = adapter
        holder.moviesRecyclerView.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
    }

    override fun getItemCount(): Int {
        return if (categories == null) 0 else categories!!.size
    }


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.category_title)
        val moviesRecyclerView: RecyclerView = itemView.findViewById(R.id.movie_rv)
        private val more: TextView = itemView.findViewById(R.id.category_more)

        init {
            more.setOnClickListener { moreClickListener!!.onMoreClick(categories!![adapterPosition].url) }
        }
    }
}
