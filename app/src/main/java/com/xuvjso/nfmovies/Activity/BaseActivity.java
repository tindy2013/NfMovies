package com.xuvjso.nfmovies.Activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import com.xuvjso.nfmovies.NFMoviesApplication;
import com.xuvjso.nfmovies.R;
import me.yokeyword.fragmentation.SupportActivity;

public class BaseActivity extends SupportActivity {
    private static final String MXPLAYER_AD = "com.mxtech.videoplayer.ad";
    private static final String MXPLAYER_PRO = "com.mxtech.videoplayer.pro";
    private static final String NPLAYER = "com.newin.nplayer.pro";
    private static final String VLC = "org.videolan.vlc";
    protected static final int REQUEST_READ_AND_WRITE_PERMISSION = 1;
    protected NFMoviesApplication application;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        application = (NFMoviesApplication) getApplication();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED)
            requestPermissions(new String[] {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_READ_AND_WRITE_PERMISSION);

    }

    public void play(String url, String caption) {
        play(url, caption, false);
    }

    public void play(String url, String caption, boolean isLive) {
        if (url == null || !url.contains("http")) {
            Toast.makeText(getApplicationContext(), R.string.load_play_url_error, Toast.LENGTH_SHORT).show();
            return;
        }
        NFMoviesApplication app = (NFMoviesApplication) getApplication();
        int player = app.getPlayer();
        switch (player) {
            case 0:
                Intent playerIntent = new Intent(this, PlayerActivity.class);
                playerIntent.putExtra("url", url);
                playerIntent.putExtra("caption", caption);
                playerIntent.putExtra("isLive", isLive);
                startActivity(playerIntent);
                break;
            case 1:
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse(url), "video/*");
                startActivity(intent.createChooser(intent, getString(R.string.select_player)));
                break;
            case 2:
                play(url, caption, MXPLAYER_AD);
                break;
            case 3:
                play(url, caption, MXPLAYER_PRO);
                break;
            case 4:
                play(url, caption, NPLAYER);
                break;
            case 5:
                play(url, caption, VLC);

        }
    }

    private void play(String url, String caption, String packageName) {
        PackageInfo p = null;
        try {
            p = getApplicationContext().getPackageManager().getPackageInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            p = null;
        }
        if (p == null) {
            Toast.makeText(getApplicationContext(),getString(R.string.not_found) + packageName, Toast.LENGTH_SHORT).show();
        } else {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setPackage(packageName);
            //   String[] header = new String[]{"Referer", "https://youku.com-iqiyi.net/"};
            //    intent.putExtra("headers", header);
            intent.setDataAndType(Uri.parse(url), "video/*");
            startActivity(intent);
        }
    }

}
