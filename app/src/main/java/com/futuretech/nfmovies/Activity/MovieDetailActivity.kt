package com.futuretech.nfmovies.Activity

import android.app.AlertDialog
import android.content.*
import android.database.sqlite.SQLiteDatabase
import android.os.AsyncTask
import android.os.Environment
import android.os.IBinder
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.*
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayout
import com.phillipcalvin.iconbutton.IconButton
import com.sackcentury.shinebuttonlib.ShineButton
import com.futuretech.nfmovies.API.ISite
import com.futuretech.nfmovies.Adapter.EpisodesPageAdapter
import com.futuretech.nfmovies.Entity.Episodes
import com.futuretech.nfmovies.Helper.LikedHelper
import com.futuretech.nfmovies.NFMoviesApplication
import com.futuretech.nfmovies.UI.AutoHeightViewPager
import com.futuretech.nfmovies.Entity.Episode
import com.futuretech.nfmovies.Fragment.EpisodesFragment
import com.futuretech.nfmovies.Entity.Movie
import com.futuretech.nfmovies.R
import com.futuretech.nfmovies.Utils.*
import me.zhanghai.android.materialprogressbar.MaterialProgressBar
import okhttp3.Response
import org.fourthline.cling.android.AndroidUpnpService
import org.fourthline.cling.android.AndroidUpnpServiceImpl
import org.fourthline.cling.controlpoint.ControlPoint
import org.fourthline.cling.model.action.ActionInvocation
import org.fourthline.cling.model.message.UpnpResponse
import org.fourthline.cling.model.meta.Device
import org.fourthline.cling.model.meta.LocalDevice
import org.fourthline.cling.model.meta.RemoteDevice
import org.fourthline.cling.model.meta.Service
import org.fourthline.cling.model.types.UDAServiceType
import org.fourthline.cling.registry.DefaultRegistryListener
import org.fourthline.cling.registry.Registry
import com.futuretech.nfmovies.Utils.DLNAUtil.DeviceDisplay
import org.fourthline.cling.support.avtransport.callback.SetAVTransportURI
import org.w3c.dom.Text
import java.lang.ref.WeakReference

import java.util.ArrayList
import java.util.HashMap

class MovieDetailActivity : BaseActivity(), EpisodesFragment.OnFragmentInteractionListener {
    private var movieImg: ImageView? = null
    private var bg: ImageView? = null
    private var movieInfo: TextView? = null
    private var movieDescription: TextView? = null
    private var api: ISite? = null
    private var movie: Movie? = null
    private var tabLayout: TabLayout? = null
    private var progressBar: MaterialProgressBar? = null
    private var viewPager: AutoHeightViewPager? = null
    private var parseTask: ParseTask? = null
    private var detailTask: DetailTask? = null
    private var playUrlMap: MutableMap<String, String?>? = null
    private var captionMap: MutableMap<String, String?>? = null
    private var episodesPageAdapter: EpisodesPageAdapter? = null
    private var likeButton: ShineButton? = null
    private var db: SQLiteDatabase? = null
    private var dbHelper: LikedHelper? = null
    private var castUrl: String? = null
    private var castName: String? = null
    private var dialog: AlertDialog? = null
    private var deviceListView: ListView? = null

    private val player: Int = 0

    private var listAdapter: ArrayAdapter<DeviceDisplay>? = null
    private val registryListener = BrowseRegistryListener()
    private var upnpService: AndroidUpnpService? = null
    private val serviceConnection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            upnpService = service as AndroidUpnpService
            // Clear the list
            listAdapter!!.clear()
            // Get ready for future device advertisements
            upnpService!!.registry.addListener(registryListener)

            // Now add all devices to the list we already know about
            for (device in upnpService!!.registry.devices) {
                registryListener.deviceAdded(device)
            }

            // Search asynchronously for all devices, they will respond soon
            upnpService!!.controlPoint.search()
        }

        override fun onServiceDisconnected(className: ComponentName) {
            upnpService = null
        }
    }

    enum class TaskType {
        COPY, PLAY, CAST
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_movie_detail)
        movie = intent.getSerializableExtra("movie") as Movie
        api = APIUtil.getClient(movie!!.site!!)
        dbHelper = LikedHelper(this)
        db = dbHelper!!.writableDatabase

        initView()
        loadContent()
        bindService()
    }


    private fun bindService() {
        applicationContext.bindService(
                Intent(this, AndroidUpnpServiceImpl::class.java),
                serviceConnection,
                Context.BIND_AUTO_CREATE
        )
    }


    private fun initView() {
        val movieName = findViewById<TextView>(R.id.detail_name)
        movieName.text = movie!!.name
        val play = findViewById<IconButton>(R.id.detail_play)

        movieImg = findViewById(R.id.detail_img)
        movieDescription = findViewById(R.id.detail_description)
        bg = findViewById(R.id.detail_bg)
        bg!!.alpha = 0.3f

        tabLayout = findViewById(R.id.detail_tab)
        movieInfo = findViewById(R.id.detail_info)
        progressBar = findViewById(R.id.detail_progress)
        playUrlMap = HashMap()
        captionMap = HashMap()
        viewPager = findViewById(R.id.origin_view_pager)
        likeButton = findViewById(R.id.detail_like_button)

        likeButton!!.setOnClickListener {
            if (likeButton!!.isChecked) {
                dbHelper!!.add(db!!, movie!!)
                Toast.makeText(applicationContext, R.string.like_success, Toast.LENGTH_SHORT).show()
            } else {
                dbHelper!!.delete(db!!, movie!!)
                Toast.makeText(applicationContext, R.string.dislike_success, Toast.LENGTH_SHORT).show()
            }
        }


        if (movie!!.img != null) {
            ImageUtil.display(applicationContext, movie!!.img, movieImg!!, null)
            ImageUtil.display(applicationContext, movie!!.img, bg!!, null)
        }

        play.setOnClickListener {
            if(viewPager == null || episodesPageAdapter == null)
            {
                Toast.makeText(applicationContext, R.string.not_loaded, Toast.LENGTH_SHORT).show()
            }
            else
            {
                val f = episodesPageAdapter!!.getItem(viewPager!!.currentItem) as EpisodesFragment
                val e = f.getmData()[0]
                Toast.makeText(applicationContext, getString(R.string.ready_to_play) + e.name, Toast.LENGTH_SHORT).show()
                onPlayClick(e)
            }
        }

        deviceListView = ListView(this)
        listAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1)
        deviceListView!!.adapter = listAdapter
        //deviceListView!!.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, position, l ->
        deviceListView!!.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val device = listAdapter!!.getItem(position)!!.device
            val service = device.findService(UDAServiceType("AVTransport"))
            val metadata = DLNAUtil.pushMediaToRender(castUrl!!, "id",
                    movie!!.name + ' '.toString() + castName, "0")
            val controlPoint = upnpService!!.controlPoint
            controlPoint.execute(object : SetAVTransportURI(service, castUrl, metadata) {
                override fun failure(invocation: ActionInvocation<*>, operation: UpnpResponse, defaultMsg: String) {
                    Log.e("CAST", "play error")
                }
            })
        }


    }

    private fun loadContent() {
        if (!haveTask()) {
            detailTask = DetailTask()
            detailTask!!.execute()
        }
        QueryLikedStatusTask().execute()
    }

    private inner class QueryLikedStatusTask : AsyncTask<Any, String, Any>() {

        override fun doInBackground(vararg params: Any): Any? {
            return dbHelper!!.query(db!!, movie!!)
        }

        override fun onPreExecute() {
            likeButton!!.visibility = View.INVISIBLE
        }

        override fun onPostExecute(o: Any?) {
            likeButton!!.visibility = View.VISIBLE
            likeButton!!.isChecked = o != null
        }
    }


    override fun onFragmentInteraction(e: Episode, t: TaskType) {
        if (t == TaskType.PLAY) onPlayClick(e)
        if (t == TaskType.COPY) onCopyClick(e)
        if (t == TaskType.CAST) onCastClick(e)
    }

    private fun showCastDialog() {
        if (dialog == null) {
            val builder = AlertDialog.Builder(this)
            builder.setCancelable(true)
            builder.setView(deviceListView)
            builder.setTitle(R.string.choose_your_device)
            builder.setIcon(R.drawable.ic_airplay_white_24dp)
            dialog = builder.create()
        }
        dialog!!.show()
    }

    private fun onCastClick(e: Episode) {
        val url = e.url
        val t = Task(TaskType.CAST, e)
        if (haveUrl(playUrlMap!!, url)) {
            castUrl = playUrlMap!![url]
            castName = e.name
            showCastDialog()
            return
        } else if (!haveTask()) {
            parseTask = ParseTask()
            parseTask!!.execute(t)
        }
    }


    private fun onCopyClick(e: Episode) {
        val url = e.url
        val t = Task(TaskType.COPY, e)
        if (haveUrl(playUrlMap!!, url))
            copy(playUrlMap!![url])
        else if (!haveTask()) {
            parseTask = ParseTask()
            parseTask!!.execute(t)
        }
    }

    private fun onPlayClick(e: Episode) {
        val url = e.url
        val t = Task(TaskType.PLAY, e)
        val pUrl: String?
        var cUrl: String? = null
        if (haveUrl(playUrlMap!!, url)) {
            pUrl = playUrlMap!![url]
            if (haveUrl(captionMap!!, url)) cUrl = captionMap!![url]
            play(pUrl, cUrl)
        } else if (!haveTask()) {
            parseTask = ParseTask()
            parseTask!!.execute(t)
        }
    }

    private fun haveUrl(map: Map<String, String?>, key: String): Boolean {
        if (!map.containsKey(key)) return false
        return map[key] != null
    }

    private fun haveTask(): Boolean {
        if (parseTask == null && detailTask == null) return false
        if (parseTask != null && parseTask!!.status != AsyncTask.Status.FINISHED) return true
        return detailTask != null && detailTask!!.status != AsyncTask.Status.FINISHED
    }


    private inner class Task(var type: TaskType, var episode: Episode) {
        var playUrl: String? = null
        var caption: String? = null

        init {
            this.caption = null
            this.playUrl = null
        }
    }

    private inner class ParseTask : AsyncTask<Task, String, Task>() {
        override fun doInBackground(vararg tasks: Task): Task {
            var s: String? = null
            val task = tasks[0]
            val ep = task.episode
            try {
                s = api!!.getPlayURL(ep)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            if (s == null) return task
            task.playUrl = s
            if (task.type == TaskType.COPY) return task
            if (task.type == TaskType.CAST) return task
            if (ep.caption != "") {
                try {
                    val response = OkHttpUtil.instance[ep.caption, "http://ddrk.me"]
                    if (response!!.code == 404) return task
                    var html = response.body!!.string()
                    val path = Environment.getExternalStorageDirectory().absolutePath + "/Subtitles/"
                    val str = ep.caption.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    val filename = str[str.size - 1]
                    html = html.replace("NOTE.*".toRegex(), "")
                    if (FileUtil.write(path, filename, html)) {
                        task.caption = path + filename
                        publishProgress(getString(R.string.download_sub_success))
                    } else
                        publishProgress(getString(R.string.download_sub_failed))
                } catch (e: Exception) {
                    e.printStackTrace()
                    publishProgress(getString(R.string.download_sub_failed))
                }

            }
            return task
        }

        override fun onProgressUpdate(vararg values: String) {
            Toast.makeText(applicationContext, values[0], Toast.LENGTH_SHORT).show()
        }

        override fun onPreExecute() {
            super.onPreExecute()
            progressBar!!.visibility = ProgressBar.VISIBLE
        }

        override fun onPostExecute(t: Task) {
            progressBar!!.visibility = ProgressBar.INVISIBLE
            if (t.playUrl != null) {
                playUrlMap!![t.episode.url] = t.playUrl
                if (t.caption != null) captionMap!![t.episode.url] = t.caption
            } else {
                Toast.makeText(applicationContext, R.string.load_play_url_error, Toast.LENGTH_SHORT).show()
                return
            }
            when (t.type) {
                TaskType.PLAY -> play(t.playUrl, t.caption)
                TaskType.COPY -> copy(t.playUrl)
                TaskType.CAST -> {
                    castUrl = t.playUrl
                    castName = t.episode.name
                    showCastDialog()
                }
            }
        }
    }

    private fun copy(url: String?) {
        val clipManager = applicationContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(movie!!.name, url)
        clipManager.setPrimaryClip(clip)
        Toast.makeText(applicationContext, R.string.copy_link_success, Toast.LENGTH_SHORT).show()
    }

    private inner class DetailTask : AsyncTask<Any, String, Any>() {
        override fun onPreExecute() {
            progressBar!!.visibility = ProgressBar.VISIBLE
        }

        override fun doInBackground(objects: Array<Any>): Any? {
            try {
                movie = api!!.getMovieDetail(movie!!)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return movie
        }

        override fun onPostExecute(o: Any?) {
            progressBar!!.visibility = ProgressBar.INVISIBLE
            val playbtn = findViewById<IconButton>(R.id.detail_play)
            playbtn.isEnabled = false
            playbtn.setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.disabled_grey))
            if (o == null) {
                Toast.makeText(applicationContext, R.string.load_failed, Toast.LENGTH_LONG).show()
                return
            }

            movie = o as Movie?
            movieDescription!!.text = movie!!.description
            val t = StringBuilder()
            val info = StringBuilder()
            t.append(movie!!.year).append(' ').append(movie!!.type)
            info.append(getString(R.string.source)).append(api!!.name)
            movieInfo!!.text = info
            val episodesList = movie!!.episodes
            if (episodesList == null || episodesList.isEmpty()) {
                Toast.makeText(applicationContext, R.string.no_resource, Toast.LENGTH_LONG).show()
                return
            }
            val fragments = ArrayList<Fragment>()
            val keys = ArrayList<String>()
            for (eps in episodesList) {
                keys.add(eps.name)
                fragments.add(EpisodesFragment.newInstance(eps.episodes))
            }

            episodesPageAdapter = EpisodesPageAdapter(applicationContext,
                    supportFragmentManager, fragments, keys)
            viewPager!!.adapter = episodesPageAdapter
            tabLayout!!.setupWithViewPager(viewPager)

            ImageUtil.display(applicationContext, movie!!.img, movieImg!!, null)
            ImageUtil.display(applicationContext, movie!!.img, bg!!, null)

            if(episodesPageAdapter != null)
            {
                playbtn.isEnabled = true
                playbtn.setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.netflixRed))
            }

        }

    }


    override fun onDestroy() {
        dbHelper!!.close()
        if (upnpService != null) {
            upnpService!!.registry.removeListener(registryListener)
        }
        applicationContext.unbindService(serviceConnection)
        super.onDestroy()
    }


    private inner class BrowseRegistryListener : DefaultRegistryListener() {

        /* Discovery performance optimization for very slow Android devices! */
        override fun remoteDeviceDiscoveryStarted(registry: Registry?, device: RemoteDevice?) {
            deviceAdded(device)
        }

        override fun remoteDeviceDiscoveryFailed(registry: Registry?, device: RemoteDevice?, ex: Exception?) {
            runOnUiThread {
                Toast.makeText(
                        this@MovieDetailActivity,
                        "Discovery failed of '" + device!!.displayString + "': "
                                + (ex?.toString()
                                ?: "Couldn't retrieve device/service descriptors"),
                        Toast.LENGTH_LONG
                ).show()
            }
            deviceRemoved(device)
        }
        /* End of optimization, you can remove the whole block if your Android handset is fast (>= 600 Mhz) */

        override fun remoteDeviceAdded(registry: Registry, device: RemoteDevice) {
            deviceAdded(device)
        }

        override fun remoteDeviceRemoved(registry: Registry, device: RemoteDevice) {
            deviceRemoved(device)
        }

        override fun localDeviceAdded(registry: Registry, device: LocalDevice) {
            deviceAdded(device)
        }

        override fun localDeviceRemoved(registry: Registry, device: LocalDevice) {
            deviceRemoved(device)
        }

        fun deviceAdded(device: Device<*, *, *>?) {
            runOnUiThread {
                val d = DeviceDisplay(device!!)
                val position = listAdapter!!.getPosition(d)
                if (position >= 0) {
                    // Device already in the list, re-set new value at same position
                    listAdapter!!.remove(d)
                    listAdapter!!.insert(d, position)
                } else {
                    listAdapter!!.add(d)
                }
            }
        }

        fun deviceRemoved(device: Device<*, *, *>?) {
            runOnUiThread { listAdapter!!.remove(DeviceDisplay(device!!)) }
        }
    }

}
