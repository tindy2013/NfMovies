package com.futuretech.nfmovies.Fragment


import android.app.DownloadManager
import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.os.Environment
import android.text.method.LinkMovementMethod
import android.view.Gravity
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.bumptech.glide.Glide
import com.futuretech.nfmovies.NFMoviesApplication
import com.futuretech.nfmovies.R
import com.futuretech.nfmovies.Utils.FileUtil
import com.futuretech.nfmovies.Utils.OkHttpUtil

import org.json.JSONObject

import java.io.File

/**
 * A simple [Fragment] subclass.
 */
class SettingFragment : PreferenceFragmentCompat() {
    private var application: NFMoviesApplication? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
        val about = preferenceScreen.findPreference<Preference>("about")
        val clear = preferenceScreen.findPreference<Preference>("clear")
        val update = preferenceScreen.findPreference<Preference>("update")
        val player = preferenceScreen.findPreference<Preference>("player")
        application = activity?.application as NFMoviesApplication

        player!!.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { preference, newValue ->
            val p = newValue as String
            application!!.player = Integer.valueOf(p)
            true
        }

        /*
        update!!.summary = getString(R.string.current_version_name) + application!!.versionName
        update.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            CheckUpdateTask().execute()
            true
        }
        */
        // override the content for now
        update!!.title = getString(R.string.current_version_name)
        update.summary = application!!.versionName

        clear!!.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            FileUtil.deleteDirectory(Glide.getPhotoCacheDir(context!!)!!)
            FileUtil.deleteDirectory(activity!!.cacheDir)
            val subtitlePath = Environment.getExternalStorageDirectory().absoluteFile.toString() + "/Subtitles/"
            FileUtil.deleteDirectory(File(subtitlePath))
            Toast.makeText(context, R.string.clear_cache_success, Toast.LENGTH_SHORT).show()
            true
        }
        about!!.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            val builder = AlertDialog.Builder(context!!)
            val text = TextView(context)
            text.setText(R.string.about_string)
            text.movementMethod = LinkMovementMethod.getInstance()
            text.setBackgroundColor(Color.BLACK)
            text.gravity = Gravity.CENTER
            text.textSize = 16f
            builder.setTitle(R.string.about)
            builder.setIcon(R.drawable.ic_info_white_24dp)
            builder.setView(text)
            builder.create().show()
            true
        }

    }

    // remove version check for now
    /*
    private inner class CheckUpdateTask : AsyncTask<String, String, String>() {

        override fun doInBackground(vararg strings: String): String? {
            publishProgress(getString(R.string.updating))
            val html = OkHttpUtil.instance.getHtml("http://97.64.32.215/status", "")
            var name: String? = null
            try {
                val j = JSONObject(html!!)
                val versionName = j.getString("version_name")
                val newVersion = j.getLong("version")
                if (newVersion > application!!.versionCode) {
                    publishProgress(getString(R.string.have_new_version))
                    name = versionName
                } else {
                    publishProgress(getString(R.string.already_the_newest_version))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                publishProgress(getString(R.string.check_new_version_failed))
            }

            return name
        }

        override fun onProgressUpdate(vararg values: String) {
            Toast.makeText(context, values[0], Toast.LENGTH_SHORT).show()
        }

        override fun onPostExecute(name: String?) {
            if (name == null) return
            val downloadManager = activity!!.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val request = DownloadManager.Request(Uri.parse("http://97.64.32.215/nfmovies.apk"))
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,
                    "nfmovies $name.apk")
            request.setVisibleInDownloadsUi(true)
            downloadManager.enqueue(request)
        }
    }
    */

}// Required empty public constructor
