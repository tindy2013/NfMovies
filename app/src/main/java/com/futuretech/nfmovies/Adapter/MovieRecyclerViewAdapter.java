package com.futuretech.nfmovies.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.futuretech.nfmovies.Entity.Movie;
import com.futuretech.nfmovies.Listener.MovieItemClickListener;
import com.futuretech.nfmovies.R;
import com.futuretech.nfmovies.Utils.ImageUtil;

import java.util.List;

public class MovieRecyclerViewAdapter extends RecyclerView.Adapter<MovieRecyclerViewAdapter.ViewHolder> {
    private Context context;
    private List<Movie> movies;
    MovieItemClickListener movieItemClickListener;
    public MovieRecyclerViewAdapter(Context context, List<Movie> movies, MovieItemClickListener listener) {
        this.context = context;
        this.movies = movies;
        this.movieItemClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.movie_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.name.setText(movies.get(position).getName());
        ImageUtil.display(context, movies.get(position).getImg(), holder.img, null);
    }

    @Override
    public int getItemCount() {
        return movies.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView name;
        private ImageView img;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.movie_name);
            img = itemView.findViewById(R.id.movie_img);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    movieItemClickListener.onMovieClick(movies.get(getAdapterPosition()), img);
                }
            });
        }
    }
}
