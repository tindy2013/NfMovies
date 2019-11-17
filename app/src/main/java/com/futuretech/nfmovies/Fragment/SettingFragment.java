package com.futuretech.nfmovies.Fragment;


import android.app.DownloadManager;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.text.method.LinkMovementMethod;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import com.bumptech.glide.Glide;
import com.futuretech.nfmovies.NFMoviesApplication;
import com.futuretech.nfmovies.R;
import com.futuretech.nfmovies.Utils.FileUtil;
import com.futuretech.nfmovies.Utils.OkHttpUtil;

import org.json.JSONObject;

import java.io.File;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingFragment extends PreferenceFragmentCompat{
    private NFMoviesApplication application;

    public SettingFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
        Preference about = getPreferenceScreen().findPreference("about");
        Preference clear = getPreferenceScreen().findPreference("clear");
        Preference update = getPreferenceScreen().findPreference("update");
        Preference player = getPreferenceScreen().findPreference("player");
        application = (NFMoviesApplication) getActivity().getApplication();

        player.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                String p = (String) newValue;
                application.setPlayer(Integer.valueOf(p));
                return true;
            }
        });

        update.setSummary(getString(R.string.current_version_name) + application.getVersionName());
        update.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                new CheckUpdateTask().execute();
                return true;
            }
        });
        clear.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                FileUtil.deleteDirectory(Glide.getPhotoCacheDir(getContext()));
                FileUtil.deleteDirectory(getActivity().getCacheDir());
                String subtitlePath = Environment.getExternalStorageDirectory().getAbsoluteFile() + "/Subtitles/";
                FileUtil.deleteDirectory(new File(subtitlePath));
                Toast.makeText(getContext(), R.string.clear_cache_success, Toast.LENGTH_SHORT).show();
                return true;
            }
        });
        about.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                TextView text = new TextView(getContext());
                text.setText(R.string.about_string);
                text.setMovementMethod(LinkMovementMethod.getInstance());
                text.setBackgroundColor(Color.BLACK);
                text.setGravity(Gravity.CENTER);
                text.setTextSize(16);
                builder.setTitle(R.string.about);
                builder.setIcon(R.drawable.ic_info_white_24dp);
                builder.setView(text);
                builder.create().show();
                return true;
            }
        });

    }

    private class CheckUpdateTask extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... strings) {
            publishProgress(getString(R.string.updating));
            String html = OkHttpUtil.getInstance().getHtml("http://97.64.32.215/status", "");
            String name = null;
            try {
                JSONObject j = new JSONObject(html);
                String versionName = j.getString("version_name");
                long newVersion = j.getLong("version");
                if (newVersion > application.getVersionCode()) {
                    publishProgress(getString(R.string.have_new_version));
                    name = versionName;
                } else {
                    publishProgress(getString(R.string.already_the_newest_version));
                }
            } catch (Exception e) {
                e.printStackTrace();
                publishProgress(getString(R.string.check_new_version_failed));
            }
            return name;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            Toast.makeText(getContext(), values[0], Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onPostExecute(String name) {
            if (name == null) return;
            DownloadManager downloadManager = (DownloadManager) getActivity().getSystemService(Context.DOWNLOAD_SERVICE);
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse("http://97.64.32.215/nfmovies.apk"));
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,
                    "nfmovies " + name + ".apk");
            request.setVisibleInDownloadsUi(true);
            downloadManager.enqueue(request);
        }
    }

}
