package com.xuvjso.nfmovies.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.cardview.widget.CardView;
import com.xuvjso.nfmovies.Entity.Channel;
import com.xuvjso.nfmovies.Listener.ChannelClickListener;
import com.xuvjso.nfmovies.R;
import com.xuvjso.nfmovies.Utils.ImageUtil;

import java.util.List;

public class ChannelAdapter extends BaseAdapter {
    private List<Channel> channels;
    private Context context;
    private ChannelClickListener clickListener;

    public ChannelAdapter(Context context, List<Channel> channels, ChannelClickListener channelClickListener) {
        this.channels = channels;
        this.context = context;
        this.clickListener = channelClickListener;
    }

    public ChannelAdapter(Context context, ChannelClickListener channelClickListener) {
        this.context = context;
        this.channels = null;
        this.clickListener = channelClickListener;
    }

    public List<Channel> getChannels() {
        return channels;
    }

    @Override
    public int getCount() {
        return channels.size();
    }

    @Override
    public Object getItem(int position) {
        return channels.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.channel_item, null);
            viewHolder = new ViewHolder(convertView);
            viewHolder.channelTitle.setText(channels.get(position).getTitle());
            ImageUtil.display(context, channels.get(position).getIcon(), viewHolder.channelImg, null);
            viewHolder.channelImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clickListener.OnChannelClick(position);
                }
            });
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        return convertView;
    }

    class ViewHolder {
        private CardView cardView;
        private ImageView channelImg;
        private TextView channelTitle;
        public ViewHolder(View convertView) {
            channelImg = convertView.findViewById(R.id.channel_img);
            channelTitle = convertView.findViewById(R.id.channel_title);
            cardView = convertView.findViewById(R.id.channel_card);
        }
    }
}
