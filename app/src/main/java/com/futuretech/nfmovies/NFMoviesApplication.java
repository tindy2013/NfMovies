package com.futuretech.nfmovies;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import com.umeng.analytics.MobclickAgent;
import com.umeng.commonsdk.UMConfigure;

public class NFMoviesApplication extends Application {

    private String versionName;
    private long versionCode;
    private int player;
    private static final String AUTH_CONFIG = "auth";
    private static final String VERSION_MESSAGE_CONFIG = "versionMessage";
    @Override
    public void onCreate() {
        super.onCreate();
        UMConfigure.init(this, "5db6fc1d570df36b7f000635", "Android",
                UMConfigure.DEVICE_TYPE_PHONE, null);
        MobclickAgent.setPageCollectionMode(MobclickAgent.PageMode.AUTO);

        String packageName = getPackageName();
        PackageManager pm = getPackageManager();
        PackageInfo info = null;
        try {
            info = pm.getPackageInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        versionName = info.versionName;
        versionCode = info.versionCode;
        String p = PreferenceManager.getDefaultSharedPreferences(this).getString("player", "0");
        player = Integer.valueOf(p);

    }

    public void setFirst(boolean isFirst) {
        SharedPreferences config = getSharedPreferences(VERSION_MESSAGE_CONFIG, MODE_PRIVATE);
        SharedPreferences.Editor editor = config.edit();
        editor.putBoolean(versionName, isFirst);
        editor.commit();
    }

    public boolean isFirst() {
        SharedPreferences config = getSharedPreferences(VERSION_MESSAGE_CONFIG, MODE_PRIVATE);
        return config.getBoolean(versionName, true);
    }

    public boolean isAuth() {
        SharedPreferences config = getSharedPreferences(AUTH_CONFIG, MODE_PRIVATE);
        return config.getBoolean("status", false);
    }

    public void setAuth(boolean auth) {
        SharedPreferences config = getSharedPreferences(AUTH_CONFIG, MODE_PRIVATE);
        SharedPreferences.Editor editor = config.edit();
        editor.putBoolean("status", auth);
        editor.commit();
    }

    public int getPlayer() {
        return player;
    }

    public void setPlayer(int player) {
        this.player = player;
    }

    public String getVersionName() {
        return versionName;
    }

    public long getVersionCode() {
        return versionCode;
    }

}
