package com.xuvjso.nfmovies.Activity;

import android.content.*;
import android.os.AsyncTask;
import android.os.Environment;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.tabs.TabLayout;
import com.phillipcalvin.iconbutton.IconButton;
import com.xuvjso.nfmovies.API.ISite;
import com.xuvjso.nfmovies.Adapter.EpisodesPageAdapter;
import com.xuvjso.nfmovies.UI.AutoHeightViewPager;
import com.xuvjso.nfmovies.Entity.Episode;
import com.xuvjso.nfmovies.Fragment.EpisodesFragment;
import com.xuvjso.nfmovies.Listener.EpisodeCopyClickListener;
import com.xuvjso.nfmovies.Listener.EpisodePlayClickListener;
import com.xuvjso.nfmovies.Listener.UploadClickListener;
import com.xuvjso.nfmovies.Entity.Movie;
import com.xuvjso.nfmovies.R;
import com.xuvjso.nfmovies.Utils.*;
import com.xuvjso.nfmovies.API.Type;
import com.xuvjso.nfmovies.Adapter.EpisodeRecyclerViewAdapter;
import me.zhanghai.android.materialprogressbar.MaterialProgressBar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MovieDetailActivity extends BaseActivity implements EpisodePlayClickListener, EpisodeCopyClickListener,
        EpisodesFragment.OnFragmentInteractionListener, UploadClickListener {
    private ImageView movieImg;
    private ImageView bg;
    private TextView movieInfo;
    private TextView movieName;
    private TextView movieDescription;
    private ISite api;
    private Movie movie;
    private RecyclerView episodesRv;
    private TabLayout tabLayout;
    private MaterialProgressBar progressBar;
    private IconButton play;
    private Map<String, List<Episode>> episodes;
    private AutoHeightViewPager viewPager;
    private ParseTask parseTask;
    private DetailTask detailTask;
    private Map<String, String> playUrlMap;
    private EpisodesPageAdapter episodesPageAdapter;

    private int player;


    EpisodeRecyclerViewAdapter  adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);
        initView();
    }


    private void initView() {
        String name = getIntent().getExtras().getString("name");
        String img = getIntent().getExtras().getString("img");
        String url = getIntent().getExtras().getString("url");
        player = getIntent().getIntExtra("player", 1);
        Type type = (Type) getIntent().getExtras().getSerializable("type");
        movie = new Movie(name, img, url);
        api = APIUtil.getClient(type);
        movieImg = findViewById(R.id.detail_img);
        movieName = findViewById(R.id.detail_name);
        movieDescription = findViewById(R.id.detail_description);
        bg = findViewById(R.id.detail_bg);
        episodesRv = findViewById(R.id.episode_recyclerview);
        tabLayout = findViewById(R.id.detail_tab);
        movieInfo = findViewById(R.id.detail_info);
        progressBar = findViewById(R.id.detail_progress);
        play = findViewById(R.id.detail_play);
        playUrlMap = new HashMap<String, String>();
        viewPager =(AutoHeightViewPager) findViewById(R.id.origin_view_pager);
        movieName.setText(movie.getName());
        bg.setAlpha(0.3f);
        if (!movie.getImg().equals("none")) {
            ImageUtil.display(getApplicationContext(), movie.getImg(), movieImg, null);
            ImageUtil.display(getApplicationContext(), movie.getImg(), bg, null);
        }


        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EpisodesFragment f = (EpisodesFragment) episodesPageAdapter.getItem(viewPager.getCurrentItem());
                Episode e = f.getEpisodeList().get(0);
                Toast.makeText(getApplicationContext(), "准备播放"+e.getName(), Toast.LENGTH_SHORT).show();
                onPlayClick(e);
            }
        });

        if (!haveTask()) {
            detailTask = new DetailTask();
            detailTask.execute();
        }


    }

    public void onCopyClick(Episode e) {
        String url = e.getUrl();
        Task t = new Task(2, e);
        if (havePlayUrl(url)) copy(playUrlMap.get(url));
        else if (!haveTask()) {
            parseTask = new ParseTask();
            parseTask.execute(t);
        }

    }

    private boolean havePlayUrl(String url) {
        if (playUrlMap.get(url) == null) return false;
        else return true;
    }

    private boolean haveTask() {
        if (parseTask == null && detailTask == null) return false;
        if (parseTask != null && parseTask.getStatus() != AsyncTask.Status.FINISHED) return true;
        if (detailTask != null && detailTask.getStatus() != AsyncTask.Status.FINISHED) return true;
        return false;
    }

    @Override
    public void onFragmentInteraction(Episode e, int t) {
        if (t == 1) onPlayClick(e);
        if (t == 2) onCopyClick(e);
    }

    @Override
    public boolean upload(String str) {
        Toast.makeText(getApplicationContext(), "开发中...", Toast.LENGTH_SHORT).show();
        return false;
    }

    @Override
    public void onPlayClick(Episode episode) {
        String url = episode.getUrl();
        Task t = new Task(1, episode);
        if (havePlayUrl(url)) play(playUrlMap.get(url), player);
        else if (!haveTask()) {
            parseTask = new ParseTask();
            parseTask.execute(t);
        }
    }

    private class Task {
        public int type;
        public Episode episode;
        public String playUrl;

        public Task(int type, Episode e) {
            this.type = type;
            this.episode = e;
        }
    }

    private class ParseTask extends AsyncTask<Task, String, Task> {
        @Override
        protected Task doInBackground(Task... tasks) {
            String s = null;
            Episode ep = tasks[0].episode;
            try {
                s = api.getPlayURL(ep);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (ep.getCaption() != null) {
                try {
                    publishProgress("正在下载字幕");
                    String html = OkHttpUtil.getInstance().getHtml(ep.getCaption(), "http://ddrk.me");
                    String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Subtitles/";
                    String[] str = ep.getCaption().split("/");
                    String filename = str[str.length - 1];
                    filename = filename.replace("vtt", "srt");
                    html = html.replace("WEBVTT", "");
                    html = html.replaceAll("NOTE.*", "");
                    html = html.replaceAll("&lrm;", "");
                    html = html.replaceAll("<.*?>", "");
                    if (FileUtil.write(path, filename, html)) publishProgress("下载字幕成功");
                    else publishProgress("下载字幕失败");
                } catch (Exception e) {
                    e.printStackTrace();
                    publishProgress("下载字幕失败");
                }
            }
            tasks[0].playUrl = s;
            return tasks[0];
        }

        @Override
        protected void onProgressUpdate(String... values) {
            Toast.makeText(getApplicationContext(), values[0], Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(ProgressBar.VISIBLE);
        }

        @Override
        protected void onPostExecute(Task t) {
            progressBar.setVisibility(ProgressBar.INVISIBLE);
            playUrlMap.put(t.episode.getUrl(), t.playUrl);
            if (t.type == 1) {
                play(t.playUrl, player);
            } else if (t.type == 2) {
                copy(t.playUrl);
            }
        }
    }

    private void copy(String url) {
        ClipboardManager clipManager = (ClipboardManager) getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(movie.getName(), url);
        clipManager.setPrimaryClip(clip);
        Toast.makeText(getApplicationContext(), "复制播放链接成功", Toast.LENGTH_SHORT).show();
    }




    private class DetailTask extends AsyncTask {
        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(ProgressBar.VISIBLE);
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            try {
                movie = api.getMovieDetail(movie);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return movie;
        }

        @Override
        protected void onPostExecute(Object o) {
            progressBar.setVisibility(ProgressBar.INVISIBLE);
            if (o == null) {
                Toast.makeText(getApplicationContext(), "加载失败", Toast.LENGTH_SHORT).show();
                return;
            }
            movie = (Movie) o;
            movieDescription.setText(movie.getDescription().trim());
            StringBuilder t = new StringBuilder();
            t.append(movie.getYear()).append(' ').append(movie.getType());
            movieInfo.setText(t.toString());
            episodes = movie.getEpisodes();
            if (episodes == null) {
                Toast.makeText(getApplicationContext(), "加载失败", Toast.LENGTH_SHORT).show();
                return;
            }
            List<Fragment> fragments = new ArrayList<>();
            List<String> keys = new ArrayList<>(episodes.keySet());
            for (String key : episodes.keySet()) {
                fragments.add(EpisodesFragment.newInstance(episodes.get(key)));
            }

            episodesPageAdapter = new EpisodesPageAdapter(getApplicationContext(),
                    getSupportFragmentManager(), fragments, keys);
            viewPager.setAdapter(episodesPageAdapter);
            tabLayout.setupWithViewPager(viewPager);

            ImageUtil.display(getApplicationContext(), movie.getImg(), movieImg, null);
            ImageUtil.display(getApplicationContext(), movie.getImg(), bg, null);

        }

    }

}
