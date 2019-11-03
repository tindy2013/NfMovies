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
import me.yokeyword.fragmentation.SupportActivity;

public class BaseActivity extends SupportActivity {
    private static final String MXPLAYER_AD = "com.mxtech.videoplayer.ad";
    private static final String MXPLAYER_PRO = "com.mxtech.videoplayer.pro";
    private static final String NPLAYER = "com.newin.nplayer.pro";
    protected static final int REQUEST_READ_AND_WRITE_PERMISSION = 1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED)
            requestPermissions(new String[] {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_READ_AND_WRITE_PERMISSION);

    }

    public void play(String url, int type) {
        switch (type) {
            case 1:
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse(url), "video/*");
                startActivity(intent.createChooser(intent, "请选择播放器"));
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
            Toast.makeText(getApplicationContext(), "加载播放链接失败", Toast.LENGTH_SHORT).show();
            return;
        }
        PackageInfo p = null;
        try {
            p = getApplicationContext().getPackageManager().getPackageInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            p = null;
        }
        if (p == null) {
            Toast.makeText(getApplicationContext(),"未找到" + packageName, Toast.LENGTH_SHORT).show();
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
