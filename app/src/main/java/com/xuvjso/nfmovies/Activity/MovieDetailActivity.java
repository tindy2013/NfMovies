package com.xuvjso.nfmovies.Activity;

import android.content.*;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Environment;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import com.google.android.material.tabs.TabLayout;
import com.phillipcalvin.iconbutton.IconButton;
import com.sackcentury.shinebuttonlib.ShineButton;
import com.xuvjso.nfmovies.API.ISite;
import com.xuvjso.nfmovies.Adapter.EpisodesPageAdapter;
import com.xuvjso.nfmovies.Helper.LikedHelper;
import com.xuvjso.nfmovies.UI.AutoHeightViewPager;
import com.xuvjso.nfmovies.Entity.Episode;
import com.xuvjso.nfmovies.Fragment.EpisodesFragment;
import com.xuvjso.nfmovies.Listener.EpisodeCopyClickListener;
import com.xuvjso.nfmovies.Listener.EpisodePlayClickListener;
import com.xuvjso.nfmovies.Listener.UploadClickListener;
import com.xuvjso.nfmovies.Entity.Movie;
import com.xuvjso.nfmovies.R;
import com.xuvjso.nfmovies.Utils.*;
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
    private TextView movieDescription;
    private ISite api;
    private Movie movie;
    private TabLayout tabLayout;
    private MaterialProgressBar progressBar;
    private AutoHeightViewPager viewPager;
    private ParseTask parseTask;
    private DetailTask detailTask;
    private Map<String, String> playUrlMap;
    private EpisodesPageAdapter episodesPageAdapter;
    private ShineButton likeButton;
    private SQLiteDatabase db;
    private LikedHelper dbHelper;

    private int player;
    public enum TaskType {
        COPY, PLAY
    }


    EpisodeRecyclerViewAdapter  adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);

        movie = (Movie) getIntent().getSerializableExtra("movie");
        player = getIntent().getIntExtra("player", 1);
        api = APIUtil.getClient(movie.getSite());
        dbHelper = new LikedHelper(this);
        db = dbHelper.getWritableDatabase();
        initView();
    }


    private void initView() {
        TextView movieName = findViewById(R.id.detail_name);
        movieName.setText(movie.getName());
        IconButton play = findViewById(R.id.detail_play);

        movieImg = findViewById(R.id.detail_img);
        movieDescription = findViewById(R.id.detail_description);
        bg = findViewById(R.id.detail_bg);
        bg.setAlpha(0.3f);

        tabLayout = findViewById(R.id.detail_tab);
        movieInfo = findViewById(R.id.detail_info);
        progressBar = findViewById(R.id.detail_progress);
        playUrlMap = new HashMap<String, String>();
        viewPager = findViewById(R.id.origin_view_pager);
        likeButton = findViewById(R.id.detail_like_button);


        if (dbHelper.query(db, movie) != null) {
            likeButton.setChecked(true);
        } else {
            likeButton.setChecked(false);
        }

        likeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (likeButton.isChecked()) {
                    dbHelper.add(db, movie);
                    Toast.makeText(getApplicationContext(), "收藏成功", Toast.LENGTH_SHORT).show();
                } else {
                    dbHelper.delete(db, movie);
                    Toast.makeText(getApplicationContext(), "取消收藏", Toast.LENGTH_SHORT).show();
                }
            }
        });


        if (movie.getImg() != null) {
            ImageUtil.display(getApplicationContext(), movie.getImg(), movieImg, null);
            ImageUtil.display(getApplicationContext(), movie.getImg(), bg, null);
        }

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EpisodesFragment f = (EpisodesFragment) episodesPageAdapter.getItem(viewPager.getCurrentItem());
                Episode e = f.getmData().get(0);
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
        Task t = new Task(TaskType.COPY, e);
        if (havePlayUrl(url)) copy(playUrlMap.get(url));
        else if (!haveTask()) {
            parseTask = new ParseTask();
            parseTask.execute(t);
        }
    }

    @Override
    public void onPlayClick(Episode e) {
        String url = e.getUrl();
        Task t = new Task(TaskType.PLAY, e);
        if (havePlayUrl(url)) play(playUrlMap.get(url), player);
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
    public void onFragmentInteraction(Episode e, TaskType t) {
        if (t == TaskType.PLAY) onPlayClick(e);
        if (t == TaskType.COPY) onCopyClick(e);
    }

    @Override
    public boolean upload(String str) {
        Toast.makeText(getApplicationContext(), "开发中...", Toast.LENGTH_SHORT).show();
        return false;
    }

    private class Task {
        public TaskType type;
        public Episode episode;
        public String playUrl;

        public Task(TaskType type, Episode e) {
            this.type = type;
            this.episode = e;
        }
    }

    private class ParseTask extends AsyncTask<Task, String, Task> {
        @Override
        protected Task doInBackground(Task... tasks) {
            String s = null;
            Task task = tasks[0];
            Episode ep = task.episode;
            try {
                s = api.getPlayURL(ep);
            } catch (Exception e) {
                e.printStackTrace();
            }

            task.playUrl = s;
            if (task.type == TaskType.COPY) return task;

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

            return task;
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
            if (t.type == TaskType.PLAY) {
                play(t.playUrl, player);
            } else if (t.type == TaskType.COPY) {
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
            movieDescription.setText(movie.getDescription());
            StringBuilder t = new StringBuilder();
            t.append(movie.getYear()).append(' ').append(movie.getType());
            movieInfo.setText(t.toString());
            Map<String, List<Episode>> episodes = movie.getEpisodes();
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


    @Override
    protected void onDestroy() {
        dbHelper.close();
        super.onDestroy();
    }
}
