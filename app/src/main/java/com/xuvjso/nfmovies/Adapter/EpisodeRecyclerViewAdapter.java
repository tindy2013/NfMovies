package com.xuvjso.nfmovies.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.xuvjso.nfmovies.Entity.Episode;
import com.xuvjso.nfmovies.Listener.EpisodeCopyClickListener;
import com.xuvjso.nfmovies.Listener.EpisodePlayClickListener;
import com.xuvjso.nfmovies.R;

import java.util.List;

public class EpisodeRecyclerViewAdapter extends RecyclerView.Adapter<EpisodeRecyclerViewAdapter.ViewHolder> {
    private List<Episode> episodes;
    private Context context;
    private EpisodePlayClickListener playListener;
    private EpisodeCopyClickListener copyListener;

    public EpisodeRecyclerViewAdapter(Context context, List<Episode> episodes,
                                      EpisodePlayClickListener playListener,
                                      EpisodeCopyClickListener copyListener) {
        this.episodes = episodes;
        this.context = context;
        this.playListener = playListener;
        this.copyListener = copyListener;
    }

    public void setEpisodes(List<Episode> episodes) {
        this.episodes = episodes;
    }

    @NonNull
    @Override
    public EpisodeRecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.episode_item, parent, false);
        return new ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull EpisodeRecyclerViewAdapter.ViewHolder holder, int position) {
        holder.name.setText(episodes.get(position).getName());
    }

    @Override
    public int getItemCount() {
        return episodes.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView name;
        private ImageView play;
        private ImageView copy;
        private ImageView upload;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.episode_name);
            play = itemView.findViewById(R.id.episode_play);
            copy = itemView.findViewById(R.id.episode_copy);
            upload = itemView.findViewById(R.id.episode_upload);
            upload.setVisibility(View.GONE);
            play.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    playListener.onPlayClick(episodes.get(getAdapterPosition()));
                }
            });

            copy.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    copyListener.onCopyClick(episodes.get(getAdapterPosition()));
                }
            });
        }
    }
}
