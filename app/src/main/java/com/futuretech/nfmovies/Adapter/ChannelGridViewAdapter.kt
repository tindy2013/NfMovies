package com.futuretech.nfmovies.Adapter

import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Transformation
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.futuretech.nfmovies.Entity.Channel
import com.futuretech.nfmovies.Listener.ChannelClickListener
import com.futuretech.nfmovies.R
import com.futuretech.nfmovies.Utils.ImageUtil

class ChannelGridViewAdapter : BaseAdapter {
    var channels: List<Channel>? = null
        private set
    private var context: Context? = null
    private var clickListener: ChannelClickListener? = null

    constructor(context: Context, channels: List<Channel>, channelClickListener: ChannelClickListener) {
        this.channels = channels
        this.context = context
        this.clickListener = channelClickListener
    }

    constructor(context: Context, channelClickListener: ChannelClickListener) {
        this.context = context
        this.channels = null
        this.clickListener = channelClickListener
    }

    override fun getCount(): Int {
        return channels!!.size
    }

    override fun getItem(position: Int): Any {
        return channels!![position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertview: View?, parent: ViewGroup): View {
        var convertView = convertview
        val viewHolder: ViewHolder
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.channel_item, null)
            viewHolder = ViewHolder(convertView!!)
        } else {
            viewHolder = convertView.tag as ViewHolder
        }
        viewHolder.channelTitle.text = channels!![position].title
        ImageUtil.display(context!!, channels!![position].icon, viewHolder.channelImg, null)
        viewHolder.channelImg.setOnClickListener { clickListener!!.OnChannelClick(channels!![position]) }
        convertView.tag = viewHolder
        return convertView
    }

    internal inner class ViewHolder(convertView: View) {
        private val cardView: CardView
        val channelImg: ImageView
        val channelTitle: TextView

        init {
            channelImg = convertView.findViewById(R.id.channel_img)
            channelTitle = convertView.findViewById(R.id.channel_title)
            cardView = convertView.findViewById(R.id.channel_card)
        }
    }
}
