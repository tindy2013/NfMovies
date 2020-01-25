package com.futuretech.nfmovies.Activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.futuretech.nfmovies.NFMoviesApplication
import com.futuretech.nfmovies.R
import me.yokeyword.fragmentation.SupportActivity

open class BaseActivity : SupportActivity() {
    protected var application: NFMoviesApplication = NFMoviesApplication()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        application = getApplication() as NFMoviesApplication
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_READ_AND_WRITE_PERMISSION)
        }

    }

    @JvmOverloads
    fun play(url: String?, caption: String?, isLive: Boolean = false) {
        if (url == null || !url.contains("http")) {
            Toast.makeText(applicationContext, R.string.load_play_url_error, Toast.LENGTH_SHORT).show()
            return
        }
        if (caption != null) Log.i("CAPTION", caption)
        Log.i("PLAY", url)
        val app = getApplication() as NFMoviesApplication
        when (app.player) {
            0 -> {
                if(Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN)
                {
                    Toast.makeText(applicationContext, R.string.system_too_old, Toast.LENGTH_LONG).show()
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.setDataAndType(Uri.parse(url), "video/*")
                    startActivity(Intent.createChooser(intent, getString(R.string.select_player)))
                }
                else
                {
                    val playerIntent = Intent(this, PlayerActivity::class.java)
                    playerIntent.putExtra("url", url)
                    playerIntent.putExtra("caption", caption)
                    playerIntent.putExtra("isLive", isLive)
                    startActivity(playerIntent)
                }
            }
            1 -> {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.setDataAndType(Uri.parse(url), "video/*")
                startActivity(Intent.createChooser(intent, getString(R.string.select_player)))
            }
            2 -> play(url, caption, MXPLAYER_AD)
            3 -> play(url, caption, MXPLAYER_PRO)
            4 -> play(url, caption, NPLAYER)
            5 -> play(url, caption, VLC)
        }
    }

    private fun play(url: String, caption: String?, packageName: String) {
        var p: PackageInfo?
        p = try {
            applicationContext.packageManager.getPackageInfo(packageName, 0)
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            null
        }

        if (p == null) {
            Toast.makeText(applicationContext, getString(R.string.not_found) + packageName, Toast.LENGTH_SHORT).show()
        } else {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setPackage(packageName)
            intent.setDataAndType(Uri.parse(url), "video/*")
            startActivity(intent)
        }
    }

    companion object {
        private const val MXPLAYER_AD = "com.mxtech.videoplayer.ad"
        private const val MXPLAYER_PRO = "com.mxtech.videoplayer.pro"
        private const val NPLAYER = "com.newin.nplayer.pro"
        private const val VLC = "org.videolan.vlc"
        protected const val REQUEST_READ_AND_WRITE_PERMISSION = 1
    }

}
