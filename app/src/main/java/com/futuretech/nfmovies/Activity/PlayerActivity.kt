package com.futuretech.nfmovies.Activity

import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.view.View
import android.view.WindowManager
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.Format
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.MergingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.SingleSampleMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.text.CaptionStyleCompat
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.MimeTypes
import com.futuretech.nfmovies.R
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import java.io.File


class PlayerActivity : AppCompatActivity() {

    private var playerView: PlayerView? = null
    private var simpleExoPlayer: SimpleExoPlayer? = null
    private var url: String? = null
    private var caption: String? = null
    private var isLive: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setImmersive()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(R.layout.activity_player)
        url = intent.getStringExtra("url")
        caption = intent.getStringExtra("caption")
        isLive = intent.getBooleanExtra("isLive", false)
        initView()
        configureSubtitleView()
        play()
    }

    override fun onPause() {
        super.onPause()
        simpleExoPlayer!!.playWhenReady = false
    }

    override fun onResume() {
        super.onResume()
        simpleExoPlayer!!.playWhenReady = true
        setImmersive()
    }

    private fun setImmersive() {
        val view = window.decorView

        var visibilityVal = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION

        /*
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            view.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        }
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            view.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
        }
        else
        {
            view.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        }
        */

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            visibilityVal = (visibilityVal or View.SYSTEM_UI_FLAG_FULLSCREEN)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            visibilityVal = (visibilityVal or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        }

        view.systemUiVisibility = visibilityVal
    }

    private fun initView() {
        playerView = findViewById(R.id.player_view)
        simpleExoPlayer = ExoPlayerFactory.newSimpleInstance(this)
        playerView!!.player = simpleExoPlayer
        playerView!!.setFastForwardIncrementMs(10000)
        playerView!!.setRewindIncrementMs(10000)
        playerView!!.setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING)
        if (isLive) playerView!!.useController = false
    }

    private fun configureSubtitleView() {
        val subtitleView = playerView!!.subtitleView
        subtitleView.setFractionalTextSize(0.06f)
        val defaultSubtitleColor = Color.argb(255, 218, 218, 218)
        val outlineColor = Color.argb(255, 43, 43, 43)
        val style = CaptionStyleCompat(defaultSubtitleColor,
                Color.TRANSPARENT, Color.TRANSPARENT,
                CaptionStyleCompat.EDGE_TYPE_OUTLINE,
                outlineColor, null)
        subtitleView.setStyle(style)
        subtitleView.setBottomPaddingFraction(0.05f)
    }

    private fun play() {
        val dataSource = DefaultDataSourceFactory(this, UA)
        val videoSource: MediaSource?
        val subtitleSource: MediaSource?
        videoSource = if (url!!.contains("m3u8")) {
            HlsMediaSource.Factory(dataSource)
                    .createMediaSource(Uri.parse(url))
        } else {
            ProgressiveMediaSource.Factory(dataSource)
                    .createMediaSource(Uri.parse(url))
        }

        if (caption != null) {
            val subtitleFormat = Format.createTextSampleFormat(null, MimeTypes.TEXT_VTT, null, Format.NO_VALUE, Format.NO_VALUE, null, null, Format.OFFSET_SAMPLE_RELATIVE
            )
            val file = File(caption!!)
            subtitleSource = SingleSampleMediaSource.Factory(dataSource)
                    .createMediaSource(Uri.fromFile(file), subtitleFormat, C.TIME_UNSET)
            val mergingMediaSource = MergingMediaSource(videoSource, subtitleSource)
            simpleExoPlayer!!.prepare(mergingMediaSource)
        } else {
            simpleExoPlayer!!.prepare(videoSource)
        }

        simpleExoPlayer!!.playWhenReady = true
    }

    override fun onDestroy() {
        super.onDestroy()
        simpleExoPlayer!!.release()
    }

    companion object {
        private const val UA = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/78.0.3904.87 Safari/537.36)"
    }

}
