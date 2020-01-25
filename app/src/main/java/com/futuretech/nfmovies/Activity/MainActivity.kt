package com.futuretech.nfmovies.Activity

import android.os.AsyncTask
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.material.bottomnavigation.BottomNavigationView
import android.view.MenuItem
import com.futuretech.nfmovies.Entity.*
import com.futuretech.nfmovies.Fragment.*
import com.futuretech.nfmovies.Listener.*
import com.futuretech.nfmovies.R
import com.futuretech.nfmovies.Utils.OkHttpUtil
import me.yokeyword.fragmentation.*
import org.json.JSONObject

class MainActivity : BaseActivity(), ChannelClickListener {

    private var fragments: Array<SupportFragment?> = arrayOfNulls(FRAGMENT_NUM)

    private var moreNum: Int = 0
    private var time: Long = 0

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {
                showHideFragment(fragments[HOME_FRAGMENT])
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_search -> {
                showHideFragment(fragments[SEARCH_FRAGMENT])
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_more -> {
                showHideFragment(fragments[MORE_FRAGMENT])
                clickMore()
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_live -> {
                showHideFragment(fragments[LIVE_FRAGMENT])
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_liked -> {
                showHideFragment(fragments[LIKED_FRAGMENT])
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    private var exitTime: Long = 0

    private fun clickMore() {

        if (application.isAuth) return
        if (moreNum > 50) return

        val currentTime = System.currentTimeMillis()
        if (moreNum == 50) {
            application.isAuth = true
            Toast.makeText(applicationContext, "Success!", Toast.LENGTH_SHORT).show()
            return
        }
        if (currentTime - time > 2000) {
            time = currentTime
        } else {
            if (moreNum > 40) Toast.makeText(applicationContext, moreNum.toString(), Toast.LENGTH_SHORT).show()
            moreNum ++
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        moreNum = 0

        setContentView(R.layout.activity_main)

        if (findFragment(HomeFragment::class.java) == null) {
            fragments[HOME_FRAGMENT] = HomeFragment.newInstance()
            fragments[SEARCH_FRAGMENT] = SearchFragment.newInstance()
            fragments[MORE_FRAGMENT] = MoreFragment.newInstance()
            fragments[LIVE_FRAGMENT] = LiveFragment.newInstance()
            fragments[LIKED_FRAGMENT] = LikedFragment.newInstance()
            loadMultipleRootFragment(R.id.main_frame, 0, *fragments)
        } else {
            fragments[HOME_FRAGMENT] = findFragment(HomeFragment::class.java)
            fragments[SEARCH_FRAGMENT] = findFragment(SearchFragment::class.java)
            fragments[MORE_FRAGMENT] = findFragment(MoreFragment::class.java)
            fragments[LIVE_FRAGMENT] = findFragment(LiveFragment::class.java)
            fragments[LIKED_FRAGMENT] = findFragment(LikedFragment::class.java)
        }

        initView()

        //new AuTask().execute();

    }

    override fun onDestroy() {
        application.isFirst = false
        super.onDestroy()
    }

    override fun onBackPressedSupport() {
        val currentTime = System.currentTimeMillis()
        if (currentTime - exitTime > 1500) {
            Toast.makeText(this, R.string.press_again_to_exit, Toast.LENGTH_SHORT).show()
            exitTime = currentTime
        } else {
            finish()
        }
    }


    private fun initView() {

        val navigation_h = findViewById<View>(R.id.navigation_hide) as BottomNavigationView
        navigation_h.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        navigation_h.visibility = View.INVISIBLE

        val navigation = findViewById<View>(R.id.navigation) as BottomNavigationView
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        if (application.isAuth) {
            navigation_h.visibility = View.VISIBLE
            navigation.visibility = View.INVISIBLE
        }
    }

    override fun OnChannelClick(channel: Channel) {
        val url = channel.url
        play(url, null, true)
    }

    companion object {
        private const val HOME_FRAGMENT = 0
        private const val LIVE_FRAGMENT = 1
        private const val SEARCH_FRAGMENT = 2
        private const val MORE_FRAGMENT = 3
        private const val LIKED_FRAGMENT = 4
        private const val FRAGMENT_NUM = 5
    }

    /*
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
    */
}
