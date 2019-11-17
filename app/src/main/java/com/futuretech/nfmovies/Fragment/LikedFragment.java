package com.futuretech.nfmovies.Fragment;


import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.GridView;
import androidx.annotation.Nullable;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.futuretech.nfmovies.Adapter.MovieGridViewAdapter;
import com.futuretech.nfmovies.Entity.Movie;
import com.futuretech.nfmovies.Helper.LikedHelper;
import com.futuretech.nfmovies.R;
import me.zhanghai.android.materialprogressbar.MaterialProgressBar;

import java.util.ArrayList;
import java.util.List;


public class LikedFragment extends BaseFragment {

    private GridView likedGridView;
    private LikedHelper likedHelper;
    private SQLiteDatabase db;
    private MaterialProgressBar progressBar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Task task;
    private MovieGridViewAdapter adapter;
    private QueryTask queryTask;
    private List<Movie> movies;

    public LikedFragment() {
        // Required empty public constructor
    }


    public static LikedFragment newInstance() {
        LikedFragment fragment = new LikedFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_liked, container, false);
        likedGridView = view.findViewById(R.id.liked_grid);
        progressBar = view.findViewById(R.id.progressbar);
        swipeRefreshLayout = view.findViewById(R.id.liked_swipe_refresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(false);
                if (queryTask == null || queryTask.getStatus() == AsyncTask.Status.FINISHED) {
                    queryTask = new QueryTask();
                    queryTask.execute(task);
                }
            }
        });
        return view;
    }

    @Override
    public void onSupportVisible() {
        if (likedHelper == null) likedHelper = new LikedHelper(getContext());
        if (db == null) db = likedHelper.getReadableDatabase();
        if (task == null) task = new Task(likedHelper, db);
        if (movies == null) movies = new ArrayList<>();
        if (adapter == null) adapter = new MovieGridViewAdapter(getContext(), movies, this);

        queryTask = new QueryTask();
        queryTask.execute(task);
    }

    @Override
    public void onLazyInitView(@Nullable Bundle savedInstanceState) {
        super.onLazyInitView(savedInstanceState);


    }

    @Override
    public void onDestroy() {
        if (likedHelper != null) likedHelper.close();
        super.onDestroy();
    }

    private class Task {
        public LikedHelper helper;
        public SQLiteDatabase db;

        public Task(LikedHelper helper, SQLiteDatabase db) {
            this.helper = helper;
            this.db = db;
        }
    }

    private class QueryTask extends AsyncTask<Task, String, List<Movie>> {

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected List<Movie> doInBackground(Task... tasks) {
            Task task = tasks[0];
            List<Movie> movies = task.helper.queryAll(task.db);
            return movies;
        }

        @Override
        protected void onPostExecute(List<Movie> m) {
            progressBar.setVisibility(View.INVISIBLE);
            if (movies == null) return;
            movies = new ArrayList<>(m);
            adapter = new MovieGridViewAdapter(getContext(), movies, LikedFragment.this);
            likedGridView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        }
    }
}
