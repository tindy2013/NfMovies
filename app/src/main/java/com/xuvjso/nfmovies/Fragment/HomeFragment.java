package com.xuvjso.nfmovies.Fragment;


import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.xuvjso.nfmovies.Adapter.CategoryRecyclerViewAdapter;
import com.xuvjso.nfmovies.Adapter.MovieRecyclerViewAdapter;
import com.xuvjso.nfmovies.Contract.LikedContract;
import com.xuvjso.nfmovies.Entity.Category;
import com.xuvjso.nfmovies.Entity.Movie;
import com.xuvjso.nfmovies.Helper.LikedHelper;
import com.xuvjso.nfmovies.Listener.CategoryMoreClickListener;
import com.xuvjso.nfmovies.Listener.PopupMenuItemClickListener;
import com.xuvjso.nfmovies.UI.PopupMenu;
import com.xuvjso.nfmovies.R;
import com.xuvjso.nfmovies.Utils.APIUtil;
import me.zhanghai.android.materialprogressbar.MaterialProgressBar;

import java.util.ArrayList;
import java.util.List;


public class HomeFragment extends BaseFragment implements CategoryMoreClickListener, PopupMenuItemClickListener {
    private TextView siteMenu;
    private View rootView;
    private CategoryRecyclerViewAdapter adapter;
    private MaterialProgressBar progressBar;
    private SwipeRefreshLayout refreshLayout;
    private RefreshTask refreshTask;
    private RecyclerView categoryRv;
    private List<String> menus;
    private List<List<Category>> categories;
    private PopupMenu popupMenu;
    private int current;

    @Override
    public void onSupportInvisible() {
        if (refreshTask != null) refreshTask.cancel(true);
        progressBar.setVisibility(View.INVISIBLE);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_home, container, false);
        current = 0;
        initView();
        return rootView;
    }

    @Override
    public void onLazyInitView(@Nullable Bundle savedInstanceState) {
        super.onLazyInitView(savedInstanceState);
        show(0);
    }

    private void initView() {
        menus = new ArrayList<>();
        menus.add(getString(R.string.nfmovies));
        menus.add(getString(R.string.apkgm));
        menus.add(getString(R.string.ddrk));
        menus.add(getString(R.string.duboku));
        categories = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            categories.add(new ArrayList<Category>());
        }

        siteMenu = rootView.findViewById(R.id.toolbar_menu);
        siteMenu.setVisibility(View.VISIBLE);
        progressBar = rootView.findViewById(R.id.progressbar);

        categoryRv = rootView.findViewById(R.id.content_rv);

        siteMenu.setText(menus.get(current));
        siteMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupMenu.show(v);
            }
        });

        adapter = new CategoryRecyclerViewAdapter(getContext(), this, this);
        categoryRv.setAdapter(adapter);
        LinearLayoutManager manager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);
        categoryRv.setLayoutManager(manager);



        View menuView = LayoutInflater.from(getContext()).inflate(R.layout.popup_menu, null, false);
        popupMenu = new PopupMenu(getContext(), menuView, menus, this);

        refreshLayout = rootView.findViewById(R.id.home_swipe_refresh);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshLayout.setRefreshing(false);
                newTask(current);
            }
        });
    }


    public HomeFragment() {
    }

    public static HomeFragment newInstance() {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    private boolean checkStatus() {
        if (refreshTask == null) return true;
        if (refreshTask.getStatus() == AsyncTask.Status.FINISHED) return true;
        return false;
    }

    private void show(int position) {
        if (categories.size() == 0) {
            newTask(position);
            return;
        }

        if (position < categories.size() && categories.get(position) != null) {
            adapter.setCategories(categories.get(position));
            adapter.notifyDataSetChanged();
        }

        if (categories.get(position).size() == 0) {
            newTask(position);
        }

        siteMenu.setText(menus.get(position));
        current = position;

    }

    private void newTask(int position) {
        if (checkStatus()) {
            refreshTask = new RefreshTask();
            refreshTask.execute(new TaskType(position));
        }
        return;
    }

    @Override
    public void onMoreClick(String url) {

    }

    @Override
    public void onPopupMenuItemClick(int position) {
        if (position != current) {
            if (refreshTask != null) refreshTask.cancel(true);
            siteMenu.setText(menus.get(position));
            refreshTask = null;
        }
        popupMenu.dismiss();
        show(position);
    }

    private class RefreshTask extends AsyncTask<TaskType, String, TaskType> {
        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(TaskType taskType) {
            progressBar.setVisibility(View.INVISIBLE);
            if (taskType.categories == null) {
                Toast.makeText(getContext(), R.string.load_failed, Toast.LENGTH_SHORT).show();
                return;
            }
            categories.set(taskType.position, taskType.categories) ;
            adapter.setCategories(taskType.categories);
            adapter.notifyDataSetChanged();
        }
        @Override
        protected void onCancelled() {
            progressBar.setVisibility(View.INVISIBLE);
            super.onCancelled();
        }

        @Override
        protected void onCancelled(TaskType taskType) {
            if (taskType.categories != null) categories.set(taskType.position, taskType.categories);
            progressBar.setVisibility(View.INVISIBLE);
        }

        @Override
        protected TaskType doInBackground(TaskType... taskTypes) {
            List<Category> cs = null;
            try {
                cs = APIUtil.getClient(menus.get(taskTypes[0].position)).getCategories();
            } catch (Exception e) {
                e.printStackTrace();
            }
            taskTypes[0].categories = cs;
            return taskTypes[0];
        }
    }

    private class TaskType {
        public int position;
        public List<Category> categories;

        public TaskType(int position) {
            this.position = position;
            this.categories = null;
        }
    }

    @Override
    public boolean onBackPressedSupport() {
        if (refreshTask != null && refreshTask.getStatus() != AsyncTask.Status.FINISHED) {
            refreshTask.cancel(true);
            Toast.makeText(getContext(), R.string.load_cancelling, Toast.LENGTH_SHORT).show();
            return true;
        }

        return false;

    }
}
