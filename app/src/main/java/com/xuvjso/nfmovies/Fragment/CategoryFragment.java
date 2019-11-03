package com.xuvjso.nfmovies.Fragment;


import android.app.ActivityOptions;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.xuvjso.nfmovies.API.*;
import com.xuvjso.nfmovies.Activity.MovieDetailActivity;
import com.xuvjso.nfmovies.Adapter.CategoryRecyclerViewAdapter;
import com.xuvjso.nfmovies.Entity.Category;
import com.xuvjso.nfmovies.Entity.Movie;
import com.xuvjso.nfmovies.Listener.CategoryMoreClickListener;
import com.xuvjso.nfmovies.Listener.MovieItemClickListener;
import com.xuvjso.nfmovies.Utils.APIUtil;
import me.yokeyword.fragmentation.SupportFragment;
import me.zhanghai.android.materialprogressbar.MaterialProgressBar;

import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CategoryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CategoryFragment extends SupportFragment implements MovieItemClickListener, CategoryMoreClickListener {

    private RecyclerView content;
    private SwipeRefreshLayout refreshLayout;
    private Type type;
    private ISite api;
    private String name;
    private CategoryRecyclerViewAdapter adapter;
    private MaterialProgressBar progressBar;
    private boolean isRefreshing;
    public CategoryFragment() {
        // Required empty public constructor
    }

    public Type getType() {
        return type;
    }


    public static CategoryFragment newInstance() {
        CategoryFragment fragment = new CategoryFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            type = (Type) getArguments().getSerializable("type");
            api = APIUtil.getClient(type);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return null;
    }

    @Override
    public void onLazyInitView(@Nullable Bundle savedInstanceState) {
        super.onLazyInitView(savedInstanceState);
        if (!isRefreshing) new RefreshTask().execute();
    }

    @Override
    public void onMovieClick(Movie movie, ImageView img) {
        Intent intent = new Intent(getContext(), MovieDetailActivity.class);
        intent.putExtra("name", movie.getName());
        intent.putExtra("img", movie.getImg());
        intent.putExtra("url", movie.getUrl());
        intent.putExtra("type", type);
        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(
                getActivity(), img, "sharedImg"
        );
        startActivity(intent, options.toBundle());
    }

    @Override
    public void onMoreClick(String url) {

    }

    private class RefreshTask extends AsyncTask {
        @Override
        protected Object doInBackground(Object[] objects) {
            List<Category> categories = null;
            try {
                categories = api.getCategories();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return categories;
        }

        @Override
        protected void onPreExecute(){
            isRefreshing = true;
            progressBar.setVisibility(ProgressBar.VISIBLE);
        }

        @Override
        protected void onPostExecute(Object o) {
            isRefreshing = false;
            progressBar.setVisibility(ProgressBar.INVISIBLE);
            if (o == null) {
                Toast.makeText(getContext(), "加载失败", Toast.LENGTH_SHORT).show();
                return;
            }
            List<Category> categories = (List<Category>) o;
            if (categories.size() == 0) {
                Toast.makeText(getContext(), "加载失败", Toast.LENGTH_SHORT).show();
            }
            if (adapter == null) {
                Log.d("CategoryFragment", "new adapter");
                adapter = new CategoryRecyclerViewAdapter(getContext(), categories, CategoryFragment.this, CategoryFragment.this);
                content.setAdapter(adapter);
                LinearLayoutManager manager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);
                content.setLayoutManager(manager);
            } else {
                Log.d("CategoryFragment", "dataSetChanged");
                adapter.setCategories(categories);
                adapter.notifyDataSetChanged();
            }

            return;

        }
    }
}
