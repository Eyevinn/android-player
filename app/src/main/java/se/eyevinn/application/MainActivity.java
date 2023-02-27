package se.eyevinn.application;

import android.app.ActivityManager;
import android.content.Context;
import android.os.BatteryManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.util.MimeTypes;
import com.google.android.exoplayer2.video.VideoRendererEventListener;

import org.jetbrains.annotations.NotNull;

import java.lang.management.*;


public class MainActivity extends AppCompatActivity implements VideoRendererEventListener {

    public static final String HLS_VOD = "https://f53accc45b7aded64ed8085068f31881.egress.mediapackage-vod.eu-north-1.amazonaws.com/out/v1/1c63bf88e2664639a6c293b4d055e6bb/ade303f83e8444d69b7658f988abb054/2a647c0cf9b7409598770b9f11799178/manifest.m3u8";
    public static final String MPD_VOD = "https://f53accc45b7aded64ed8085068f31881.egress.mediapackage-vod.eu-north-1.amazonaws.com/out/v1/1c63bf88e2664639a6c293b4d055e6bb/64651f16da554640930b7ce2cd9f758b/66d211307b7d43d3bd515a3bfb654e1c/manifest.mpd";
    public static final String HLS_LIVE = "https://d2fz24s2fts31b.cloudfront.net/out/v1/6484d7c664924b77893f9b4f63080e5d/manifest.m3u8";
    public static final String MPD_LIVE = "https://d2fz24s2fts31b.cloudfront.net/out/v1/3b6879c0836346c2a44c9b4b33520f4e/manifest.mpd";
    public static final String HLS_LIVE_SSAI = "https://edfaeed9c7154a20828a30a26878ade0.mediatailor.eu-west-1.amazonaws.com/v1/master/1b8a07d9a44fe90e52d5698704c72270d177ae74/AdTest/master.m3u8";

    private static final String TAG = "MainActivity";
    private SimpleExoPlayer player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupPlayer();
        loadButtonListener();
        hlsVodButtonListener();
        mpdVodButtonListener();
        hlsLiveButtonListener();
        mpdLiveButtonListener();
        hlsLiveSsaiButtonListener();
        showDataButtonListener();
    }

    private void setupPlayer() {
        if (player == null) {
            DefaultTrackSelector trackSelector = new DefaultTrackSelector(this);
            trackSelector.setParameters(
                    trackSelector.buildUponParameters().setMaxVideoSizeSd());
            player = new SimpleExoPlayer.Builder(this)
                    .setTrackSelector(trackSelector)
                    .build();
        }
        PlayerView playerView = findViewById(R.id.exo_player_view);
        playerView.setPlayer(player);
        playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
        EditText edtView = (EditText) findViewById(R.id.inputtext);
        View.OnFocusChangeListener ofcListener = new MyFocusChangeListener();
        edtView.setOnFocusChangeListener(ofcListener);
        player.setPlayWhenReady(true); //run file/link when ready to play.
    }

    private void hlsVodButtonListener() {
        Button hlsButton = (Button) findViewById(R.id.hlsVodButton);
        hlsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText editText = (EditText) findViewById(R.id.inputtext);
                editText.setText(HLS_VOD, TextView.BufferType.EDITABLE);
                editText.clearFocus();
            }
        });
    }

    private void hlsLiveButtonListener() {
        Button hlsButton = (Button) findViewById(R.id.hlsLiveButton);
        hlsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText editText = (EditText) findViewById(R.id.inputtext);
                editText.setText(HLS_LIVE, TextView.BufferType.EDITABLE);
                editText.clearFocus();
            }
        });
    }

    private void mpdVodButtonListener() {
        Button hlsButton = (Button) findViewById(R.id.mpdVodButton);
        hlsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText editText = (EditText) findViewById(R.id.inputtext);
                editText.setText(MPD_VOD, TextView.BufferType.EDITABLE);
                editText.clearFocus();
            }
        });
    }

    private void mpdLiveButtonListener() {
        Button hlsButton = (Button) findViewById(R.id.mpdLiveButton);
        hlsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText editText = (EditText) findViewById(R.id.inputtext);
                editText.setText(MPD_LIVE, TextView.BufferType.EDITABLE);
                editText.clearFocus();
            }
        });
    }

    private void hlsLiveSsaiButtonListener() {
        Button hlsButton = (Button) findViewById(R.id.hlsLiveSsaiButton);
        hlsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText editText = (EditText) findViewById(R.id.inputtext);
                editText.setText(HLS_LIVE_SSAI, TextView.BufferType.EDITABLE);
                editText.clearFocus();
            }
        });
    }

    private void loadButtonListener() {
        Button button = (Button) findViewById(R.id.loadButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PlayerView pv = (PlayerView) findViewById(R.id.exo_player_view);
                pv.setVisibility(View.VISIBLE);
                EditText editText = (EditText) findViewById(R.id.inputtext);
                editText.clearFocus();
                EditText inputText = (EditText) findViewById(R.id.inputtext);
                String message = inputText.getText().toString();
                playStreamInPlayer(message);
            }
        });
    }

    private void showDataButtonListener() {
        Button button = (Button) findViewById(R.id.showDataButton);
        button.setOnClickListener(view -> {
            if(findViewById(R.id.hwMetrics).getVisibility() == View.VISIBLE) {
                System.out.println("Hiding metrics");
                RelativeLayout ll = (RelativeLayout) findViewById(R.id.hwMetrics);
                ll.setVisibility(View.GONE);
            } else {
                RelativeLayout ll = (RelativeLayout) findViewById(R.id.hwMetrics);
                ll.setVisibility(View.VISIBLE);
                System.out.println("Showing metrics");
                getHardwareMetrics();
            }
        });
    }

    private void getHardwareMetrics() {
        ActivityManager activityManager = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);
        updateMemoryMetrics(activityManager);
        updateBatteryMetrics();

    }

    private void updateBatteryMetrics() {
        BatteryManager batteryManager = (BatteryManager) this.getSystemService(BATTERY_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            int batteryLvl = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
        }
    }

    private void updateMemoryMetrics(ActivityManager activityManager) {
        ProgressBar memBar = (ProgressBar) findViewById(R.id.memBar);
        TextView memText = (TextView) findViewById(R.id.memText);
        ActivityManager.MemoryInfo mem = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(mem);
        int percent = (int)Math.ceil((1.0 - ((double)mem.availMem / mem.totalMem)) * 100);
        memBar.setProgress(percent);
        memText.setText(String.format("Available Memory: %.2fGB", ((double)mem.availMem / 1024 / 1024 / 1024)));
    }

    @NotNull
    private void playStreamInPlayer(String url) {
        if (url.endsWith("m3u8")) {
            MediaItem mediaItem = new MediaItem.Builder()
                    .setUri(url)
                    .setMimeType(MimeTypes.APPLICATION_M3U8)
                    .build();
            player.setMediaItem(mediaItem);

        } else if (url.endsWith("mpd")) {
            MediaItem mediaItem = new MediaItem.Builder()
                    .setUri(url)
                    .setMimeType(MimeTypes.APPLICATION_MPD)
                    .build();
            player.setMediaItem(mediaItem);
        } else {
            Log.v(TAG, "Invalid url");
        }
        player.setPlayWhenReady(true);
        player.seekTo(0, 0);
        player.prepare();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.v(TAG, "onDestroy()...");
        player.release();
    }

    private class MyFocusChangeListener implements View.OnFocusChangeListener {
        public void onFocusChange(View v, boolean hasFocus) {
            if (v.getId() == R.id.inputtext && !hasFocus) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            }
        }
    }
}