package com.futuretech.nfmovies.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.futuretech.nfmovies.*;
import com.futuretech.nfmovies.Entity.Category;
import com.futuretech.nfmovies.Entity.Movie;
import com.futuretech.nfmovies.Listener.CategoryMoreClickListener;
import com.futuretech.nfmovies.Listener.MovieItemClickListener;

import java.util.List;

public class CategoryRecyclerViewAdapter extends RecyclerView.Adapter<CategoryRecyclerViewAdapter.ViewHolder> {
    private Context context;
    private List<Category> categories;
    private MovieItemClickListener movieItemClicklistener;
    private CategoryMoreClickListener moreClickListener;

    public CategoryRecyclerViewAdapter(Context context, List<Category> categories, MovieItemClickListener movieItemClicklistener, CategoryMoreClickListener moreClickListener) {
        this.context = context;
        this.categories = categories;
        this.movieItemClicklistener = movieItemClicklistener;
        this.moreClickListener = moreClickListener;
    }

    public CategoryRecyclerViewAdapter(Context context, MovieItemClickListener movieItemClicklistener, CategoryMoreClickListener moreClickListener) {
        this.context = context;
        this.movieItemClicklistener = movieItemClicklistener;
        this.moreClickListener = moreClickListener;
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
    }

    @NonNull
    @Override
    public CategoryRecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.category_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryRecyclerViewAdapter.ViewHolder holder, int position) {
        holder.title.setText(categories.get(position).getTitle());
        List<Movie> movies = categories.get(position).getMovies();
        MovieRecyclerViewAdapter adapter = new MovieRecyclerViewAdapter(context, movies, movieItemClicklistener);
        holder.moviesRecyclerView.setAdapter(adapter);
        holder.moviesRecyclerView.setLayoutManager(new LinearLayoutManager(context, RecyclerView.HORIZONTAL, false));
    }

    @Override
    public int getItemCount() {
        if (categories == null) return 0;
        return categories.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView title;
        private RecyclerView moviesRecyclerView;
        private TextView more;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.category_title);
            moviesRecyclerView = itemView.findViewById(R.id.movie_rv);
            more = itemView.findViewById(R.id.category_more);
            more.setVisibility(View.INVISIBLE);
            more.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    moreClickListener.onMoreClick(categories.get(getAdapterPosition()).getUrl());
                }
            });
        }
    }
}
