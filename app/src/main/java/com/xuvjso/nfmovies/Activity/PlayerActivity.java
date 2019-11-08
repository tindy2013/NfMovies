package com.xuvjso.nfmovies.Activity;

import android.graphics.Color;
import android.net.Uri;
import android.util.Log;
import android.view.WindowManager;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.MergingMediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.SingleSampleMediaSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.text.CaptionStyleCompat;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.ui.SubtitleView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.util.MimeTypes;
import com.xuvjso.nfmovies.R;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

public class PlayerActivity extends AppCompatActivity {

    private PlayerView playerView;
    private SimpleExoPlayer simpleExoPlayer;
    private String url;
    private String caption;
    private boolean isLive;
    private static final String UA = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/78.0.3904.87 Safari/537.36)";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_player);
        url = getIntent().getStringExtra("url");
        caption = getIntent().getStringExtra("caption");
        isLive = getIntent().getBooleanExtra("isLive", false);
        initView();
        configureSubtitleView();
        play();
    }

    private void initView() {
        playerView = findViewById(R.id.player_view);
        simpleExoPlayer = ExoPlayerFactory.newSimpleInstance(this);
        playerView.setPlayer(simpleExoPlayer);
        playerView.setFastForwardIncrementMs(10000);
        playerView.setRewindIncrementMs(10000);
        playerView.setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING);
        if (isLive) playerView.setUseController(false);
    }

    private void configureSubtitleView() {
        SubtitleView subtitleView = playerView.getSubtitleView();
        subtitleView.setFractionalTextSize(0.06f);
        int defaultSubtitleColor = Color.argb(255, 218, 218, 218);
        int outlineColor = Color.argb(255, 43, 43, 43);
        CaptionStyleCompat style =
                new CaptionStyleCompat(defaultSubtitleColor,
                        Color.TRANSPARENT, Color.TRANSPARENT,
                        CaptionStyleCompat.EDGE_TYPE_OUTLINE,
                        outlineColor, null);
        subtitleView.setStyle(style);
        subtitleView.setBottomPaddingFraction(0.05f);
    }

    private void play() {
        DataSource.Factory dataSource = new DefaultDataSourceFactory(this, UA);
        MediaSource videoSource = null;
        MediaSource subtitleSource = null;
        if (url.contains("m3u8")) {
            videoSource = new HlsMediaSource.Factory(dataSource)
                    .createMediaSource(Uri.parse(url));
        } else {
            videoSource = new ProgressiveMediaSource.Factory(dataSource)
                    .createMediaSource(Uri.parse(url));
        }

        if (caption != null) {
            Format subtitleFormat = Format.createTextSampleFormat(
                    null, MimeTypes.TEXT_VTT,
                    null, Format.NO_VALUE, Format.NO_VALUE, null, null, Format.OFFSET_SAMPLE_RELATIVE
            );
            File file = new File(caption);
            subtitleSource = new SingleSampleMediaSource.Factory(dataSource)
                    .createMediaSource(Uri.fromFile(file), subtitleFormat, C.TIME_UNSET);
            MergingMediaSource mergingMediaSource = new MergingMediaSource(videoSource, subtitleSource);
            simpleExoPlayer.prepare(mergingMediaSource);
        } else {
            simpleExoPlayer.prepare(videoSource);
        }

        simpleExoPlayer.setPlayWhenReady(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        simpleExoPlayer.release();
    }

}
