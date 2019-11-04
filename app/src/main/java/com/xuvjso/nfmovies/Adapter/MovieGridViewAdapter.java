package com.xuvjso.nfmovies.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.xuvjso.nfmovies.Entity.Movie;
import com.xuvjso.nfmovies.Listener.MovieItemClickListener;
import com.xuvjso.nfmovies.R;
import com.xuvjso.nfmovies.Utils.ImageUtil;

import java.util.List;

public class MovieGridViewAdapter extends BaseAdapter {
    private List<Movie> movies;
    private Context context;
    private MovieItemClickListener clickListener;

    public MovieGridViewAdapter(Context context, List<Movie> movies, MovieItemClickListener clickListener) {
        this.movies = movies;
        this.context = context;
        this.clickListener = clickListener;
    }

    public MovieGridViewAdapter(Context context, MovieItemClickListener clickListener) {
        this.context = context;
        this.movies = null;
        this.clickListener = clickListener;
    }

    public List<Movie> getMovies() {
        return movies;
    }

    @Override
    public int getCount() {
        return movies.size();
    }

    @Override
    public Object getItem(int position) {
        return movies.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final MovieGridViewAdapter.ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.movie_item, null);
            viewHolder = new MovieGridViewAdapter.ViewHolder(convertView);
        } else {
            viewHolder = (MovieGridViewAdapter.ViewHolder) convertView.getTag();
        }
        viewHolder.movieName.setText(movies.get(position).getName());
        ImageUtil.display(context, movies.get(position).getImg(), viewHolder.movieImg, null);
        viewHolder.movieImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickListener.onMovieClick(movies.get(position), viewHolder.movieImg);
            }
        });
        convertView.setTag(viewHolder);

        return convertView;
    }

    class ViewHolder {
        private ImageView movieImg;
        private TextView movieName;
        public ViewHolder(View convertView) {
            movieImg = convertView.findViewById(R.id.movie_img);
            movieName = convertView.findViewById(R.id.movie_name);
        }
    }
}
