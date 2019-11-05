package com.xuvjso.nfmovies.Fragment;

import android.app.ActivityOptions;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.preference.PreferenceManager;
import com.xuvjso.nfmovies.Activity.MovieDetailActivity;
import com.xuvjso.nfmovies.Listener.MovieItemClickListener;
import com.xuvjso.nfmovies.Entity.Movie;
import com.xuvjso.nfmovies.R;

import me.yokeyword.fragmentation.SupportFragment;

public class BaseFragment extends SupportFragment implements MovieItemClickListener {
    private static final String MXPLAYER_AD = "com.mxtech.videoplayer.ad";
    private static final String MXPLAYER_PRO = "com.mxtech.videoplayer.pro";
    private static final String NPLAYER = "com.newin.nplayer.pro";

    public void play(String url, int type) {
        switch (type) {
            case 1:
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse(url), "video/*");
                startActivity(intent.createChooser(intent, getString(R.string.select_player)));
                break;
            case 2:
                play(url, MXPLAYER_AD);
                break;
            case 3:
                play(url, MXPLAYER_PRO);
                break;
            case 4:
                play(url, NPLAYER);
                break;
        }
    }

    private void play(String url, String packageName) {
        if (url == null || !url.contains("http")) {
            Toast.makeText(getContext(), R.string.load_play_url_error, Toast.LENGTH_SHORT).show();
            return;
        }
        PackageInfo p = null;
        try {
            p = getContext().getPackageManager().getPackageInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            p = null;
        }
        if (p == null) {
            Toast.makeText(getContext(),R.string.not_found + packageName, Toast.LENGTH_SHORT).show();
        } else {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setPackage(packageName);
            //   String[] header = new String[]{"Referer", "https://youku.com-iqiyi.net/"};
            //    intent.putExtra("headers", header);
            intent.setDataAndType(Uri.parse(url), "video/*");
            startActivity(intent);
        }
    }


    @Override
    public void onMovieClick(Movie movie, ImageView img) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        String i = prefs.getString("player", "1");

        Intent intent = new Intent(getContext(), MovieDetailActivity.class);
        intent.putExtra("movie", movie);
        intent.putExtra("player", Integer.valueOf(i));
        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(
                getActivity(), img, "sharedImg"
        );

        startActivity(intent, options.toBundle());
    }
}
