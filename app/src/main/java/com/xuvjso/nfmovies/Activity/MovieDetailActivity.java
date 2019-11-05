package com.xuvjso.nfmovies.Activity;

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
import com.xuvjso.nfmovies.API.ISite;
import com.xuvjso.nfmovies.Adapter.EpisodesPageAdapter;
import com.xuvjso.nfmovies.Helper.LikedHelper;
import com.xuvjso.nfmovies.UI.AutoHeightViewPager;
import com.xuvjso.nfmovies.Entity.Episode;
import com.xuvjso.nfmovies.Fragment.EpisodesFragment;
import com.xuvjso.nfmovies.Entity.Movie;
import com.xuvjso.nfmovies.R;
import com.xuvjso.nfmovies.Utils.*;
import me.zhanghai.android.materialprogressbar.MaterialProgressBar;
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
import com.xuvjso.nfmovies.Utils.DLNAUtil.DeviceDisplay;
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
    private EpisodesPageAdapter episodesPageAdapter;
    private ShineButton likeButton;
    private SQLiteDatabase db;
    private LikedHelper dbHelper;
    private String castUrl;
    private String castName;
    private AlertDialog dialog;
    private ListView listView;

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

        movie = (Movie) getIntent().getSerializableExtra("movie");
        player = getIntent().getIntExtra("player", 1);
        api = APIUtil.getClient(movie.getSite());
        dbHelper = new LikedHelper(this);
        db = dbHelper.getWritableDatabase();
        initView();

        listView = new ListView(this);
        listAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1);
        listView.setAdapter(listAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
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
                Toast.makeText(getApplicationContext(), R.string.ready_to_play + e.getName(), Toast.LENGTH_SHORT).show();
                onPlayClick(e);
            }
        });

        if (!haveTask()) {
            detailTask = new DetailTask();
            detailTask.execute();
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
            builder.setView(listView);
            builder.setTitle("请选择播放设备");
            builder.setIcon(R.drawable.ic_airplay_white_24dp);
            dialog = builder.create();
        }

        dialog.show();
    }

    public void onCastClick(Episode e) {
        String url = e.getUrl();
        Task t = new Task(TaskType.CAST, e);
        if (havePlayUrl(url)) {
            castUrl = playUrlMap.get(url);
            castName = e.getName();
            showCastDialog();
            return;
        } else if(!haveTask()) {
            parseTask = new ParseTask();
            parseTask.execute(t);
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
            if (task.type == TaskType.CAST) return task;
            if (ep.getCaption() != null) {
                try {
                    publishProgress(getString(R.string.downloading_sub));
                    String html = OkHttpUtil.getInstance().getHtml(ep.getCaption(), "http://ddrk.me");
                    String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Subtitles/";
                    String[] str = ep.getCaption().split("/");
                    String filename = str[str.length - 1];
                    filename = filename.replace("vtt", "srt");
                    html = html.replace("WEBVTT", "");
                    html = html.replaceAll("NOTE.*", "");
                    html = html.replaceAll("&lrm;", "");
                    html = html.replaceAll("<.*?>", "");
                    if (FileUtil.write(path, filename, html)) publishProgress(getString(R.string.download_sub_success));
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
            playUrlMap.put(t.episode.getUrl(), t.playUrl);
            switch (t.type) {
                case PLAY:
                    play(t.playUrl, player);
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
            movieInfo.setText(t.toString());
            Map<String, List<Episode>> episodes = movie.getEpisodes();
            if (episodes == null) {
                Toast.makeText(getApplicationContext(), R.string.load_failed, Toast.LENGTH_SHORT).show();
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
        if (upnpService != null) {
            upnpService.getRegistry().removeListener(registryListener);
        }
        // This will stop the UPnP service if nobody else is bound to it
        getApplicationContext().unbindService(serviceConnection);
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
