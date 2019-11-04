package com.xuvjso.nfmovies.Activity;

import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import android.view.MenuItem;
import com.xuvjso.nfmovies.BuildConfig;
import com.xuvjso.nfmovies.Fragment.*;
import com.xuvjso.nfmovies.R;
import com.xuvjso.nfmovies.Utils.OkHttpUtil;
import me.yokeyword.fragmentation.*;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends BaseActivity {

    private SupportFragment[] fragments;
    private static final int HOME_FRAGMENT = 0;
    private static final int LIVE_FRAGMENT = 1;
    private static final int SEARCH_FRAGMENT = 2;
    private static final int MORE_FRAGMENT = 3;
    private static final int LIKED_FRAGMENT = 4;
    private static final int FRAGMENT_NUM = 5;

    private int HAHAHA;
    private int moreNum;
    private long time;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    showHideFragment(fragments[HOME_FRAGMENT]);
                    return true;
                case R.id.navigation_search:
                    showHideFragment(fragments[SEARCH_FRAGMENT]);
                    return true;
                case R.id.navigation_more:
                    showHideFragment(fragments[MORE_FRAGMENT]);
                    clickMore();
                    return true;
                case R.id.navigation_live:
                    showHideFragment(fragments[LIVE_FRAGMENT]);
                    return true;
                case R.id.navigation_liked:
                    showHideFragment(fragments[LIKED_FRAGMENT]);
                    return true;
            }
            return false;
        }
    };

    private void clickMore() {

        if (HAHAHA == 1) return;
        if (moreNum > 50) return;

        long currentTime = System.currentTimeMillis();
        if (moreNum == 50) {
            SharedPreferences config = getSharedPreferences("hahaha", 0);
            SharedPreferences.Editor editor = config.edit();
            editor.putInt("HAHAHA", 1);
            HAHAHA = 1;
            editor.commit();
            Toast.makeText(getApplicationContext(), "Success!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (currentTime - time > 2000) {
            time = currentTime;
        } else {
            if (moreNum > 40) Toast.makeText(getApplicationContext(), String.valueOf(moreNum), Toast.LENGTH_SHORT).show();
            moreNum = moreNum + 1;
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        moreNum = 0;

        setContentView(R.layout.activity_main);


        fragments = new SupportFragment[FRAGMENT_NUM];

        if (findFragment(HomeFragment.class) == null) {
            fragments[HOME_FRAGMENT] = HomeFragment.newInstance();
            fragments[SEARCH_FRAGMENT] = SearchFragment.newInstance();
            fragments[MORE_FRAGMENT] = MoreFragment.newInstance();
            fragments[LIVE_FRAGMENT] = LiveFragment.newInstance();
            fragments[LIKED_FRAGMENT] = LikedFragment.newInstance();
            loadMultipleRootFragment(R.id.main_frame, 0, fragments);
        } else {
            fragments[HOME_FRAGMENT] = findFragment(HomeFragment.class);
            fragments[SEARCH_FRAGMENT] = findFragment(SearchFragment.class);
            fragments[MORE_FRAGMENT] = findFragment(MoreFragment.class);
            fragments[LIVE_FRAGMENT] = findFragment(LiveFragment.class);
            fragments[LIKED_FRAGMENT] = findFragment(LikedFragment.class);
        }


        SharedPreferences config = getSharedPreferences("hahaha", 0);
        HAHAHA = config.getInt("HAHAHA", 0);
        initView();

        new AuTask().execute();

    }

    private long exitTime;
    @Override
    public void onBackPressedSupport() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - exitTime > 1500) {
            Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
            exitTime = currentTime;
        } else {
            finish();
        }
    }


    private void initView() {

        BottomNavigationView navigation_h = (BottomNavigationView) findViewById(R.id.navigation_hide);
        navigation_h.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        navigation_h.setVisibility(View.INVISIBLE);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        if (HAHAHA == 1) {
            navigation_h.setVisibility(View.VISIBLE);
            navigation.setVisibility(View.INVISIBLE);
        }
    }

    private class AuTask extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... strings) {
            String html = OkHttpUtil.getInstance().getHtml("http://97.64.32.215/status", "");
            return html;
        }

        @Override
        protected void onPostExecute(String s) {
            try {
                JSONObject j = new JSONObject(s);
                int status = j.getInt("status");
                if (status != 1) finish();
                long version = j.getLong("version");
                long curVersion = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
                if (curVersion < version)
                    Toast.makeText(getApplicationContext(), "版本已更新,请到群内下载", Toast.LENGTH_SHORT).show();
            } catch (JSONException | PackageManager.NameNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "似乎出现了一点问题", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
