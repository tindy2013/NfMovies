package com.xuvjso.nfmovies.Activity;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import android.view.MenuItem;
import com.xuvjso.nfmovies.Entity.Channel;
import com.xuvjso.nfmovies.Fragment.*;
import com.xuvjso.nfmovies.Listener.ChannelClickListener;
import com.xuvjso.nfmovies.R;
import com.xuvjso.nfmovies.Utils.OkHttpUtil;
import me.yokeyword.fragmentation.*;
import org.json.JSONObject;

public class MainActivity extends BaseActivity implements ChannelClickListener {

    private SupportFragment[] fragments;
    private static final int HOME_FRAGMENT = 0;
    private static final int LIVE_FRAGMENT = 1;
    private static final int SEARCH_FRAGMENT = 2;
    private static final int MORE_FRAGMENT = 3;
    private static final int LIKED_FRAGMENT = 4;
    private static final int FRAGMENT_NUM = 5;

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

        if (application.isAuth() == true) return;
        if (moreNum > 50) return;

        long currentTime = System.currentTimeMillis();
        if (moreNum == 50) {
            application.setAuth(true);
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

        initView();

        new AuTask().execute();

    }

    @Override
    protected void onDestroy() {
        application.setFirst(false);
        super.onDestroy();
    }

    private long exitTime;
    @Override
    public void onBackPressedSupport() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - exitTime > 1500) {
            Toast.makeText(this, R.string.press_again_to_exit, Toast.LENGTH_SHORT).show();
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
        if (application.isAuth()) {
            navigation_h.setVisibility(View.VISIBLE);
            navigation.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void OnChannelClick(Channel channel) {
        String url = channel.getUrl();
        play(url, null, true);
    }

    private void showNewVersionMessage(String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(msg);
        builder.setTitle(R.string.new_version_msg);
        builder.setPositiveButton(R.string.confirm, null);
        builder.setIcon(R.drawable.ic_info_white_24dp);
        builder.create().show();
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
                if (s == null) finish();
                JSONObject j = new JSONObject(s);
                int status = j.getInt("status");
                long version = j.getLong("version");
                String msg = j.getString("msg");
                if (version == application.getVersionCode() && application.isFirst()) showNewVersionMessage(msg);
                if (application.getVersionCode() < version)  Toast.makeText(getApplicationContext(), R.string.have_new_version, Toast.LENGTH_SHORT).show();
                if (status != 1) finish();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), R.string.error, Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

}
