package com.xuvjso.nfmovies.Fragment;


import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
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
import com.xuvjso.nfmovies.API.DDRK;
import com.xuvjso.nfmovies.API.NfMovies;
import com.xuvjso.nfmovies.API.Tuanzhang;
import com.xuvjso.nfmovies.Adapter.CategoryRecyclerViewAdapter;
import com.xuvjso.nfmovies.Entity.Category;
import com.xuvjso.nfmovies.R;
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
    private Toolbar toolbar;
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
        toolbar = rootView.findViewById(R.id.search_toolbar);
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
                if (checkStatus()) {
                    searchView.onActionViewCollapsed();
                    Toast.makeText(getContext(), R.string.searching + query, Toast.LENGTH_SHORT).show();
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
            Category n = NfMovies.getInstance().search(strings[0]);
            if (n != null && n.getMovies().size() != 0) categories.add(n);
            Category t = Tuanzhang.getInstance().search(strings[0]);
            if (t != null && t.getMovies().size() != 0) categories.add(t);
            Category d = DDRK.getInstance().search(strings[0]);
            if (d != null && d.getMovies().size() != 0) categories.add(d);
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




    public void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(
                activity.getCurrentFocus().getWindowToken(), 0);
    }

}
