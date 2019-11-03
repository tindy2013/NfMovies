package com.xuvjso.nfmovies.Fragment;


import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.GridView;
import android.widget.TextView;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.preference.PreferenceManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.xuvjso.nfmovies.Adapter.ChannelAdapter;
import com.xuvjso.nfmovies.Entity.Channel;
import com.xuvjso.nfmovies.Listener.ChannelClickListener;
import com.xuvjso.nfmovies.Listener.PopupMenuItemClickListener;
import com.xuvjso.nfmovies.R;
import com.xuvjso.nfmovies.UI.PopupMenu;
import com.xuvjso.nfmovies.Utils.FileUtil;
import me.zhanghai.android.materialprogressbar.MaterialProgressBar;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class LiveFragment extends BaseFragment implements PopupMenuItemClickListener, ChannelClickListener {

    private GridView gridView;
    private PopupMenu popupMenu;
    private List<List<Channel>> channels;
    private List<String> menus;
    private MaterialProgressBar progressBar;
    private TextView liveMenu;
    private View popupMenuView;
    private ChannelAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    public LiveFragment() {
    }


    public static LiveFragment newInstance() {
        LiveFragment fragment = new LiveFragment();
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
        View view = inflater.inflate(R.layout.fragment_live, container, false);
        gridView = view.findViewById(R.id.live_grid);
        liveMenu = view.findViewById(R.id.live_menu);
        progressBar = view.findViewById(R.id.live_progress);
        swipeRefreshLayout = view.findViewById(R.id.live_swipe_refresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(false);
                new ParseTask().execute();
            }
        });
        popupMenuView = LayoutInflater.from(getContext()).inflate(R.layout.popup_menu, container, false);
        gridView.setNestedScrollingEnabled(true);
        return view;
    }

    @Override
    public void onLazyInitView(@Nullable Bundle savedInstanceState) {
        if (channels == null) new ParseTask().execute();
    }

    @Override
    public void onPopupMenuItemClick(int position) {
        adapter = new ChannelAdapter(getContext(), channels.get(position), LiveFragment.this);
        gridView.setAdapter(adapter);
        popupMenu.dismiss();
        liveMenu.setText(menus.get(position));
    }

    @Override
    public void OnChannelClick(int i) {
        String url = adapter.getChannels().get(i).getUrl();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        String player = prefs.getString("player", "1");
        play(url, Integer.valueOf(player));
    }



    private class ParseTask extends AsyncTask<String, String, Map<String, List<Channel>>> {

        @Override
        protected Map<String, List<Channel>> doInBackground(String... strings) {
            String liveData = FileUtil.getFromRaw(getContext(), R.raw.live);
            byte[] d = Base64.decode(liveData, Base64.DEFAULT);
            liveData = new String(d);
            Map<String, List<Channel>> lives = new LinkedHashMap<>();
            try {
                JSONObject json = new JSONObject(liveData);
                JSONArray data = json.getJSONArray("data");
                for (int i = 0; i < data.length(); i++) {
                    JSONObject o = data.getJSONObject(i);
                    Log.i("1", o.toString());
                    JSONArray cs = o.getJSONArray("channels");
                    List<Channel> channels = new ArrayList<>();
                    for (int j = 0; j < cs.length(); j++) {
                        JSONObject cur = cs.getJSONObject(j);
                        Log.i("2", cur.toString());
                        Channel c = new Channel(cur.getString("title").toString(),
                                cur.getString("icon").toString(), cur.get("url").toString());
                        channels.add(c);
                    }
                    lives.put(o.getString("group"), channels);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return lives;
        }

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(Map<String, List<Channel>> lives) {
            progressBar.setVisibility(View.INVISIBLE);
            liveMenu.setVisibility(View.VISIBLE);
            List<String> key = new ArrayList<>(lives.keySet());
            menus = key;
            popupMenu = new PopupMenu(getContext(), popupMenuView, key, LiveFragment.this);
            liveMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    popupMenu.show(v);
                }
            });
            liveMenu.setText(key.get(0));
            channels = new ArrayList<>();
            for (int i = 0; i < key.size(); i++) {
                Log.i("group:",key.get(i));
                Log.i("channels:", lives.get(key.get(i)).get(0).getTitle());
                channels.add(lives.get(key.get(i)));
            }

            adapter = new ChannelAdapter(getContext(), channels.get(0), LiveFragment.this);
            gridView.setAdapter(adapter);
        }
    }



}