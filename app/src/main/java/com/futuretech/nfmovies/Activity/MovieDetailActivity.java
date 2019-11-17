package com.futuretech.nfmovies.Activity;

import android.app.AlertDialog;
import android.content.*;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.*;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import com.google.android.material.tabs.TabLayout;
import com.phillipcalvin.iconbutton.IconButton;
import com.sackcentury.shinebuttonlib.ShineButton;
import com.futuretech.nfmovies.API.ISite;
import com.futuretech.nfmovies.Adapter.EpisodesPageAdapter;
import com.futuretech.nfmovies.Entity.Episodes;
import com.futuretech.nfmovies.Helper.LikedHelper;
import com.futuretech.nfmovies.NFMoviesApplication;
import com.futuretech.nfmovies.UI.AutoHeightViewPager;
import com.futuretech.nfmovies.Entity.Episode;
import com.futuretech.nfmovies.Fragment.EpisodesFragment;
import com.futuretech.nfmovies.Entity.Movie;
import com.futuretech.nfmovies.R;
import com.futuretech.nfmovies.Utils.*;
import me.zhanghai.android.materialprogressbar.MaterialProgressBar;
import okhttp3.Response;
import org.fourthline.cling.android.AndroidUpnpService;
import org.fourthline.cling.android.AndroidUpnpServiceImpl;
import org.fourthline.cling.controlpoint.ControlPoint;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.types.UDAServiceType;
import org.fourthline.cling.registry.DefaultRegistryListener;
import org.fourthline.cling.registry.Registry;
import com.futuretech.nfmovies.Utils.DLNAUtil.DeviceDisplay;
import org.fourthline.cling.support.avtransport.callback.SetAVTransportURI;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MovieDetailActivity extends BaseActivity implements EpisodesFragment.OnFragmentInteractionListener{
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
    private Map<String, String> captionMap;
    private EpisodesPageAdapter episodesPageAdapter;
    private ShineButton likeButton;
    private SQLiteDatabase db;
    private LikedHelper dbHelper;
    private String castUrl;
    private String castName;
    private AlertDialog dialog;
    private ListView deviceListView;

    private int player;
    public enum TaskType {
        COPY, PLAY, CAST
    }

    private ArrayAdapter<DeviceDisplay> listAdapter;
    private BrowseRegistryListener registryListener = new BrowseRegistryListener();
    private AndroidUpnpService upnpService;
    private ServiceConnection serviceConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder service) {
            upnpService = (AndroidUpnpService) service;
            // Clear the list
            listAdapter.clear();
            // Get ready for future device advertisements
            upnpService.getRegistry().addListener(registryListener);

            // Now add all devices to the list we already know about
            for (Device device : upnpService.getRegistry().getDevices()) {
                registryListener.deviceAdded(device);
            }

            // Search asynchronously for all devices, they will respond soon
            upnpService.getControlPoint().search();
        }

        public void onServiceDisconnected(ComponentName className) {
            upnpService = null;
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_movie_detail);
        NFMoviesApplication app = (NFMoviesApplication) getApplication();
        movie = (Movie) getIntent().getSerializableExtra("movie");
        api = APIUtil.getClient(movie.getSite());
        dbHelper = new LikedHelper(this);
        db = dbHelper.getWritableDatabase();

        initView();
        loadContent();
        bindService();
    }



    private void bindService() {
        getApplicationContext().bindService(
                new Intent(this, AndroidUpnpServiceImpl.class),
                serviceConnection,
                Context.BIND_AUTO_CREATE
        );
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
        captionMap = new HashMap<String, String>();
        viewPager = findViewById(R.id.origin_view_pager);
        likeButton = findViewById(R.id.detail_like_button);


        likeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (likeButton.isChecked()) {
                    dbHelper.add(db, movie);
                    Toast.makeText(getApplicationContext(), R.string.like_success, Toast.LENGTH_SHORT).show();
                } else {
                    dbHelper.delete(db, movie);
                    Toast.makeText(getApplicationContext(), R.string.dislike_success, Toast.LENGTH_SHORT).show();
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
                Toast.makeText(getApplicationContext(), getString(R.string.ready_to_play) + e.getName(), Toast.LENGTH_SHORT).show();
                onPlayClick(e);
            }
        });

        deviceListView = new ListView(this);
        listAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1);
        deviceListView.setAdapter(listAdapter);
        deviceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                DeviceDisplay deviceDisplay = (DeviceDisplay) listAdapter.getItem(position);
                Device device = deviceDisplay.getDevice();
                Service service = device.findService(new UDAServiceType("AVTransport"));
                String metadata = DLNAUtil.pushMediaToRender(castUrl, "id",
                        movie.getName() + ' ' + castName, "0");
                ControlPoint controlPoint = upnpService.getControlPoint();
                controlPoint.execute(new SetAVTransportURI(service, castUrl, metadata) {
                    @Override
                    public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
                        Log.e("CAST", "play error");
                    }
                });
            }
        });


    }

    private void loadContent() {
        if (!haveTask()) {
            detailTask = new DetailTask();
            detailTask.execute();
        }
        new QueryLikedStatusTask().execute();
    }

    private class QueryLikedStatusTask extends AsyncTask {

        @Override
        protected Object doInBackground(Object[] objects) {
            return dbHelper.query(db, movie);
        }

        @Override
        protected void onPreExecute() {
            likeButton.setVisibility(View.INVISIBLE);
        }

        @Override
        protected void onPostExecute(Object o) {
            likeButton.setVisibility(View.VISIBLE);
            if (o == null) {
                likeButton.setChecked(false);
            } else {
                likeButton.setChecked(true);
            }
        }
    }


    @Override
    public void onFragmentInteraction(Episode e, TaskType t) {
        if (t == TaskType.PLAY) onPlayClick(e);
        if (t == TaskType.COPY) onCopyClick(e);
        if (t == TaskType.CAST) onCastClick(e);
    }

    private void showCastDialog() {
        if (dialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(true);
            builder.setView(deviceListView);
            builder.setTitle(R.string.choose_your_device);
            builder.setIcon(R.drawable.ic_airplay_white_24dp);
            dialog = builder.create();
        }
        dialog.show();
    }

    private void onCastClick(Episode e) {
        String url = e.getUrl();
        Task t = new Task(TaskType.CAST, e);
        if (haveUrl(playUrlMap, url)) {
            castUrl = playUrlMap.get(url);
            castName = e.getName();
            showCastDialog();
            return;
        } else if(!haveTask()) {
            parseTask = new ParseTask();
            parseTask.execute(t);
        }
    }


    private void onCopyClick(Episode e) {
        String url = e.getUrl();
        Task t = new Task(TaskType.COPY, e);
        if (haveUrl(playUrlMap, url)) copy(playUrlMap.get(url));
        else if (!haveTask()) {
            parseTask = new ParseTask();
            parseTask.execute(t);
        }
    }

    private void onPlayClick(Episode e) {
        String url = e.getUrl();
        Task t = new Task(TaskType.PLAY, e);
        String pUrl = null, cUrl = null;
        if (haveUrl(playUrlMap, url)) {
            pUrl = playUrlMap.get(url);
            if (haveUrl(captionMap, url)) cUrl = captionMap.get(url);
            play(pUrl, cUrl);
        }
        else if (!haveTask()) {
            parseTask = new ParseTask();
            parseTask.execute(t);
        }
    }

    private boolean haveUrl(Map<String, String> map, String key) {
        if (!map.containsKey(key)) return false;
        if (map.get(key) == null) return false;
        return true;
    }

    private boolean haveTask() {
        if (parseTask == null && detailTask == null) return false;
        if (parseTask != null && parseTask.getStatus() != AsyncTask.Status.FINISHED) return true;
        if (detailTask != null && detailTask.getStatus() != AsyncTask.Status.FINISHED) return true;
        return false;
    }



    private class Task {
        public TaskType type;
        public Episode episode;
        public String playUrl;
        public String caption;

        public Task(TaskType type, Episode e) {
            this.type = type;
            this.episode = e;
            this.caption = null;
            this.playUrl = null;
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
            if (s == null) return task;
            task.playUrl = s;
            if (task.type == TaskType.COPY) return task;
            if (task.type == TaskType.CAST) return task;
            if (ep.getCaption() != null) {
                try {

                    Response response = OkHttpUtil.getInstance().get(ep.getCaption(), "http://ddrk.me");
                    if (response.code() == 404) return task;
                    String html = response.body().string();
                    String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Subtitles/";
                    String[] str = ep.getCaption().split("/");
                    String filename = str[str.length - 1];
                    html = html.replaceAll("NOTE.*", "");
                    if (FileUtil.write(path, filename, html)) {
                        task.caption = path + filename;
                        publishProgress(getString(R.string.download_sub_success));
                    }
                    else publishProgress(getString(R.string.download_sub_failed));
                } catch (Exception e) {
                    e.printStackTrace();
                    publishProgress(getString(R.string.download_sub_failed));
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
            if (t.playUrl != null) {
                playUrlMap.put(t.episode.getUrl(), t.playUrl);
                if (t.caption != null) captionMap.put(t.episode.getUrl(), t.caption);
            } else {
                Toast.makeText(getApplicationContext(),R.string.load_play_url_error, Toast.LENGTH_SHORT).show();
                return;
            }
            switch (t.type) {
                case PLAY:
                    play(t.playUrl, t.caption);
                    break;
                case COPY:
                    copy(t.playUrl);
                    break;
                case CAST:
                    castUrl = t.playUrl;
                    castName = t.episode.getName();
                    showCastDialog();
                    break;
            }
        }
    }

    private void copy(String url) {
        ClipboardManager clipManager = (ClipboardManager) getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(movie.getName(), url);
        clipManager.setPrimaryClip(clip);
        Toast.makeText(getApplicationContext(), R.string.copy_link_success, Toast.LENGTH_SHORT).show();
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
                Toast.makeText(getApplicationContext(), R.string.load_failed, Toast.LENGTH_SHORT).show();
                return;
            }
            movie = (Movie) o;
            movieDescription.setText(movie.getDescription());
            StringBuilder t = new StringBuilder();
            t.append(movie.getYear()).append(' ').append(movie.getType());
            movieInfo.setText("来源: " + api.getName());
            List<Episodes> episodesList = movie.getEpisodes();
            if (episodesList == null || episodesList.size() == 0) {
                Toast.makeText(getApplicationContext(), R.string.load_failed, Toast.LENGTH_SHORT).show();
                return;
            }
            List<Fragment> fragments = new ArrayList<>();
            List<String> keys = new ArrayList<>();
            for (Episodes eps : episodesList) {
                keys.add(eps.getName());
                fragments.add(EpisodesFragment.newInstance(eps.getEpisodes()));
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
        if (upnpService != null) {
            upnpService.getRegistry().removeListener(registryListener);
        }
        getApplicationContext().unbindService(serviceConnection);
        super.onDestroy();
    }


    protected class BrowseRegistryListener extends DefaultRegistryListener {

        /* Discovery performance optimization for very slow Android devices! */
        @Override
        public void remoteDeviceDiscoveryStarted(Registry registry, RemoteDevice device) {
            deviceAdded(device);
        }

        @Override
        public void remoteDeviceDiscoveryFailed(Registry registry, final RemoteDevice device, final Exception ex) {
            runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(
                            MovieDetailActivity.this,
                            "Discovery failed of '" + device.getDisplayString() + "': "
                                    + (ex != null ? ex.toString() : "Couldn't retrieve device/service descriptors"),
                            Toast.LENGTH_LONG
                    ).show();
                }
            });
            deviceRemoved(device);
        }
        /* End of optimization, you can remove the whole block if your Android handset is fast (>= 600 Mhz) */

        @Override
        public void remoteDeviceAdded(Registry registry, RemoteDevice device) {
            deviceAdded(device);
        }

        @Override
        public void remoteDeviceRemoved(Registry registry, RemoteDevice device) {
            deviceRemoved(device);
        }

        @Override
        public void localDeviceAdded(Registry registry, LocalDevice device) {
            deviceAdded(device);
        }

        @Override
        public void localDeviceRemoved(Registry registry, LocalDevice device) {
            deviceRemoved(device);
        }

        public void deviceAdded(final Device device) {
            runOnUiThread(new Runnable() {
                public void run() {
                    DLNAUtil.DeviceDisplay d = new DLNAUtil.DeviceDisplay(device);
                    int position = listAdapter.getPosition(d);
                    if (position >= 0) {
                        // Device already in the list, re-set new value at same position
                        listAdapter.remove(d);
                        listAdapter.insert(d, position);
                    } else {
                        listAdapter.add(d);
                    }
                }
            });
        }

        public void deviceRemoved(final Device device) {
            runOnUiThread(new Runnable() {
                public void run() {
                    listAdapter.remove(new DLNAUtil.DeviceDisplay(device));
                }
            });
        }
    }

}
