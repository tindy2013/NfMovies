package com.futuretech.nfmovies.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.futuretech.nfmovies.Entity.Episode
import com.futuretech.nfmovies.Listener.EpisodeCastClickListener
import com.futuretech.nfmovies.Listener.EpisodeCopyClickListener
import com.futuretech.nfmovies.Listener.EpisodePlayClickListener
import com.futuretech.nfmovies.R

class EpisodeRecyclerViewAdapter(private val context: Context, private var episodes: List<Episode>?,
                                 private val playListener: EpisodePlayClickListener,
                                 private val copyListener: EpisodeCopyClickListener,
                                 private val castListener: EpisodeCastClickListener) : RecyclerView.Adapter<EpisodeRecyclerViewAdapter.ViewHolder>() {

    fun setEpisodes(episodes: List<Episode>) {
        this.episodes = episodes
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.episode_item, parent, false)
        return ViewHolder(view)

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.name.text = episodes!![position].name
    }

    override fun getItemCount(): Int {
        return episodes!!.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView
        private val play: ImageView
        private val copy: ImageView
        private val cast: ImageView

        init {
            name = itemView.findViewById(R.id.episode_name)
            play = itemView.findViewById(R.id.episode_play)
            copy = itemView.findViewById(R.id.episode_copy)
            cast = itemView.findViewById(R.id.episode_cast)
            play.setOnClickListener { playListener.onPlayClick(episodes!![adapterPosition]) }

            copy.setOnClickListener { copyListener.onCopyClick(episodes!![adapterPosition]) }

            cast.setOnClickListener { castListener.OnCastClick(episodes!![adapterPosition]) }

        }
    }
}
