package com.futuretech.nfmovies

import android.app.Application
import android.content.SharedPreferences
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import androidx.preference.PreferenceManager
import com.umeng.analytics.MobclickAgent
import com.umeng.commonsdk.UMConfigure

class NFMoviesApplication : Application() {

    var versionName: String? = null
        private set
    var versionCode: Long = 0
        private set
    var player: Int = 0

    var isFirst: Boolean
        get() {
            val config = getSharedPreferences(VERSION_MESSAGE_CONFIG, MODE_PRIVATE)
            return config.getBoolean(versionName, true)
        }
        set(isFirst) {
            val config = getSharedPreferences(VERSION_MESSAGE_CONFIG, MODE_PRIVATE)
            val editor = config.edit()
            editor.putBoolean(versionName, isFirst)
            editor.apply()
        }

    var isAuth: Boolean
        get() {
            val config = getSharedPreferences(AUTH_CONFIG, MODE_PRIVATE)
            return config.getBoolean("status", false)
        }
        set(auth) {
            val config = getSharedPreferences(AUTH_CONFIG, MODE_PRIVATE)
            val editor = config.edit()
            editor.putBoolean("status", auth)
            editor.apply()
        }

    override fun onCreate() {
        super.onCreate()
        UMConfigure.init(this, "5db6fc1d570df36b7f000635", "Android",
                UMConfigure.DEVICE_TYPE_PHONE, null)
        MobclickAgent.setPageCollectionMode(MobclickAgent.PageMode.AUTO)

        val packageName = packageName
        val pm = packageManager
        var info: PackageInfo? = null
        try {
            info = pm.getPackageInfo(packageName, 0)
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

        versionName = info!!.versionName
        versionCode = info.versionCode.toLong()
        val p = PreferenceManager.getDefaultSharedPreferences(this).getString("player", "0")
        player = Integer.valueOf(p!!)

    }

    companion object {
        private const val AUTH_CONFIG = "auth"
        private const val VERSION_MESSAGE_CONFIG = "versionMessage"
    }

}
