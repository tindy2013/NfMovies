package com.futuretech.nfmovies.Fragment;


import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.SearchView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.futuretech.nfmovies.API.DDRK;
import com.futuretech.nfmovies.API.Duboku;
import com.futuretech.nfmovies.API.NfMovies;
import com.futuretech.nfmovies.API.Tuanzhang;
import com.futuretech.nfmovies.Adapter.CategoryRecyclerViewAdapter;
import com.futuretech.nfmovies.Entity.Category;
import com.futuretech.nfmovies.NFMoviesApplication;
import com.futuretech.nfmovies.R;
import me.zhanghai.android.materialprogressbar.MaterialProgressBar;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SearchFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SearchFragment extends BaseFragment {

    private SearchView searchView;
    private RecyclerView resultRv;
    private SearchTask searchTask;
    private MaterialProgressBar progressBar;
    private SwipeRefreshLayout swipe;
    private String str;
    private CategoryRecyclerViewAdapter adapter;


    public SearchFragment() {
        // Required empty public constructor
    }


    public static SearchFragment newInstance() {
        SearchFragment fragment = new SearchFragment();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable final Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_search, container, false);
        searchView = rootView.findViewById(R.id.search_view);
        Toolbar toolbar = rootView.findViewById(R.id.search_toolbar);
        resultRv = rootView.findViewById(R.id.result_rv);
        progressBar = rootView.findViewById(R.id.progressbar);
        swipe = rootView.findViewById(R.id.search_swipe_refresh);

        swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipe.setRefreshing(false);
                if (checkStatus() && str != null) {
                    searchTask = new SearchTask();
                    searchTask.execute(str);
                }
            }
        });

        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchView.onActionViewExpanded();
            }
        });


        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (query.equals(R.string.live_keyword)) {
                    NFMoviesApplication app = (NFMoviesApplication) getActivity().getApplication();
                    app.setAuth(true);
                    return true;
                }
                if (checkStatus()) {
                    searchView.onActionViewCollapsed();
                    Toast.makeText(getContext(), getString(R.string.searching) + query, Toast.LENGTH_SHORT).show();
                    str = query;
                    searchTask = new SearchTask();
                    searchTask.execute(query);
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        return rootView;
    }

    private boolean checkStatus() {
        if (searchTask == null) return true;
        if (searchTask.getStatus() == AsyncTask.Status.FINISHED) return true;
        if (str == null && str.length() == 0) return false;
        return false;
    }

    @Override
    public void onSupportVisible() {
        showSoftInput(searchView);
        searchView.onActionViewExpanded();
    }

    private class SearchTask extends AsyncTask<String, String, List<Category>> {

        @Override
        protected List<Category> doInBackground(String... strings) {
            List<Category> categories = new ArrayList<>();
            Category nf = NfMovies.getInstance().search(strings[0]);
            if (nf != null && nf.getMovies().size() != 0) categories.add(nf);
            Category tz = Tuanzhang.getInstance().search(strings[0]);
            if (tz != null && tz.getMovies().size() != 0) categories.add(tz);
            Category dd = DDRK.getInstance().search(strings[0]);
            if (dd != null && dd.getMovies().size() != 0) categories.add(dd);
            Category db = Duboku.getInstance().search(strings[0]);
            if (db != null && db.getMovies().size() != 0) categories.add(db);
            return categories;
        }

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(List<Category> categories) {
            progressBar.setVisibility(View.INVISIBLE);
            if (categories.size() == 0) return;
            if (adapter == null) {
                adapter = new CategoryRecyclerViewAdapter(getContext(), categories, SearchFragment.this, null);
                resultRv.setAdapter(adapter);
                LinearLayoutManager manager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);
                resultRv.setLayoutManager(manager);
            } else {
                adapter.setCategories(categories);
                adapter.notifyDataSetChanged();
            }

        }
    }

}