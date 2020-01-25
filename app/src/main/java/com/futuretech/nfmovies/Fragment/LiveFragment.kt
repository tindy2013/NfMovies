package com.futuretech.nfmovies.Fragment


import android.app.Activity
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.GridView
import android.widget.TextView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.futuretech.nfmovies.Adapter.ChannelGridViewAdapter
import com.futuretech.nfmovies.Entity.Channel
import com.futuretech.nfmovies.Listener.ChannelClickListener
import com.futuretech.nfmovies.Listener.PopupMenuItemClickListener
import com.futuretech.nfmovies.R
import com.futuretech.nfmovies.UI.PopupMenu
import com.futuretech.nfmovies.Utils.FileUtil
import me.zhanghai.android.materialprogressbar.MaterialProgressBar
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

import java.util.ArrayList
import java.util.LinkedHashMap


class LiveFragment : BaseFragment(), PopupMenuItemClickListener {

    private var gridView: GridView? = null
    private var popupMenu: PopupMenu? = null
    private var channels: MutableList<List<Channel>>? = null
    private var menus: List<String>? = null
    private var progressBar: MaterialProgressBar? = null
    private var liveMenu: TextView? = null
    private var popupMenuView: View? = null
    private var adapter: ChannelGridViewAdapter? = null
    private var swipeRefreshLayout: SwipeRefreshLayout? = null
    private var channelClickListener: ChannelClickListener? = null

    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        if (activity is ChannelClickListener) {
            channelClickListener = activity
        } else {
            throw RuntimeException("$activity must implement ChannelClickListener")
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_live, container, false)
        gridView = view.findViewById(R.id.live_grid)
        liveMenu = view.findViewById(R.id.toolbar_menu)
        progressBar = view.findViewById(R.id.progressbar)
        swipeRefreshLayout = view.findViewById(R.id.live_swipe_refresh)
        swipeRefreshLayout!!.setOnRefreshListener {
            swipeRefreshLayout!!.isRefreshing = false
            ParseTask().execute()
        }
        popupMenuView = LayoutInflater.from(context).inflate(R.layout.popup_menu, container, false)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            gridView!!.isNestedScrollingEnabled = true
        }
        return view
    }

    override fun onLazyInitView(savedInstanceState: Bundle?) {
        if (channels == null) ParseTask().execute()
    }

    override fun onPopupMenuItemClick(position: Int) {
        adapter = ChannelGridViewAdapter(context!!, channels!![position], channelClickListener!!)
        gridView!!.adapter = adapter
        popupMenu!!.dismiss()
        liveMenu!!.text = menus!![position]
    }

    private inner class ParseTask : AsyncTask<String, String, Map<String, List<Channel>>>() {

        override fun doInBackground(vararg strings: String): Map<String, List<Channel>> {
            var liveData = FileUtil.getFromRaw(context!!, R.raw.live)
            val d = Base64.decode(liveData, Base64.DEFAULT)
            liveData = String(d)
            val lives = LinkedHashMap<String, List<Channel>>()
            try {
                val json = JSONObject(liveData)
                val data = json.getJSONArray("data")
                for (i in 0 until data.length()) {
                    val o = data.getJSONObject(i)
                    Log.i("1", o.toString())
                    val cs = o.getJSONArray("channels")
                    val channels = ArrayList<Channel>()
                    for (j in 0 until cs.length()) {
                        val cur = cs.getJSONObject(j)
                        Log.i("2", cur.toString())
                        val c = Channel(cur.getString("title").toString(),
                                cur.getString("icon").toString(), cur.get("url").toString())
                        channels.add(c)
                    }
                    lives[o.getString("group")] = channels
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }

            return lives
        }

        override fun onPreExecute() {
            progressBar!!.visibility = View.VISIBLE
        }

        override fun onPostExecute(lives: Map<String, List<Channel>>) {
            progressBar!!.visibility = View.INVISIBLE
            liveMenu!!.visibility = View.VISIBLE
            val key = ArrayList(lives.keys)
            menus = key
            popupMenu = PopupMenu(context!!, popupMenuView!!, key, this@LiveFragment)
            liveMenu!!.setOnClickListener { v -> popupMenu!!.show(v) }
            liveMenu!!.text = key[0]
            channels = ArrayList()
            for (i in key.indices) {
                Log.i("group:", key[i])
                Log.i("channels:", lives[key[i]]?.get(0)!!.title!!)
                channels!!.add(lives.getValue(key[i]))
            }

            adapter = ChannelGridViewAdapter(context!!, channels!![0], channelClickListener!!)
            gridView!!.adapter = adapter
        }
    }

    companion object {


        fun newInstance(): LiveFragment {
            val fragment = LiveFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }


}
