package se.eyevinn.application;


import static android.view.KeyEvent.KEYCODE_MEDIA_FAST_FORWARD;
import static android.view.KeyEvent.KEYCODE_MEDIA_NEXT;
import static android.view.KeyEvent.KEYCODE_MEDIA_PAUSE;
import static android.view.KeyEvent.KEYCODE_MEDIA_PLAY;
import static android.view.KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE;
import static android.view.KeyEvent.KEYCODE_MEDIA_PREVIOUS;
import static android.view.KeyEvent.KEYCODE_MEDIA_REWIND;
import static android.view.KeyEvent.KEYCODE_MEDIA_SKIP_BACKWARD;
import static android.view.KeyEvent.KEYCODE_MEDIA_SKIP_FORWARD;
import static android.view.KeyEvent.KEYCODE_MEDIA_STEP_BACKWARD;
import static android.view.KeyEvent.KEYCODE_MEDIA_STEP_FORWARD;

import android.app.ActivityManager;
import android.content.Context;
import android.net.TrafficStats;
import android.os.Bundle;
import android.os.Process;
import android.system.Os;
import android.system.OsConstants;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.util.MimeTypes;
import com.google.android.exoplayer2.video.VideoRendererEventListener;
import com.google.android.exoplayer2.video.VideoSize;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayout;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

import se.eyevinn.application.video.DefaultVideoSources;
import se.eyevinn.application.video.VideoSource;
import se.eyevinn.application.video.VideoSourceList;


public class MainActivity extends AppCompatActivity implements VideoRendererEventListener {

    public static final String HLS_VOD = "https://f53accc45b7aded64ed8085068f31881.egress.mediapackage-vod.eu-north-1.amazonaws.com/out/v1/1c63bf88e2664639a6c293b4d055e6bb/ade303f83e8444d69b7658f988abb054/2a647c0cf9b7409598770b9f11799178/manifest.m3u8";
    public static final String MPD_VOD = "https://f53accc45b7aded64ed8085068f31881.egress.mediapackage-vod.eu-north-1.amazonaws.com/out/v1/1c63bf88e2664639a6c293b4d055e6bb/64651f16da554640930b7ce2cd9f758b/66d211307b7d43d3bd515a3bfb654e1c/manifest.mpd";
    public static final String HLS_LIVE = "https://d2fz24s2fts31b.cloudfront.net/out/v1/6484d7c664924b77893f9b4f63080e5d/manifest.m3u8";
    public static final String MPD_LIVE = "https://d2fz24s2fts31b.cloudfront.net/out/v1/3b6879c0836346c2a44c9b4b33520f4e/manifest.mpd";
    public static final String HLS_LIVE_SSAI = "https://edfaeed9c7154a20828a30a26878ade0.mediatailor.eu-west-1.amazonaws.com/v1/master/1b8a07d9a44fe90e52d5698704c72270d177ae74/AdTest/master.m3u8";

    private static final String TAG = "MainActivity";
    private SimpleExoPlayer player;
    private Timer timer;

    private VideoSourceList videoSourceList;
    private static final long numCores = Os.sysconf(OsConstants._SC_NPROCESSORS_CONF);
    private static final long clockSpeedHz = Os.sysconf(OsConstants._SC_CLK_TCK);

    private static final int appPID = Process.myPid();
    private static final int appUID = Process.myUid();

    private long prevRxBytes = TrafficStats.getTotalRxBytes();
    private long displayedBitrate = 0;
    private long totBitrate = 0;
    private long numTicks = 0;

    private DefaultRenderersFactory renderersFactory;
    private static final CpuMetrics cpuMetrics = new CpuMetrics();

    private static final List<Integer> mediaKeyCodes = Arrays.asList(
            KEYCODE_MEDIA_SKIP_BACKWARD,
            KEYCODE_MEDIA_SKIP_FORWARD,
            KEYCODE_MEDIA_REWIND,
            KEYCODE_MEDIA_FAST_FORWARD,
            KEYCODE_MEDIA_PLAY_PAUSE,
            KEYCODE_MEDIA_PLAY,
            KEYCODE_MEDIA_PAUSE,
            KEYCODE_MEDIA_PREVIOUS,
            KEYCODE_MEDIA_NEXT,
            KEYCODE_MEDIA_STEP_BACKWARD,
            KEYCODE_MEDIA_STEP_FORWARD
    );

    @Override
    public boolean dispatchKeyEvent(KeyEvent keyEvent) {
        System.out.println("KeyEvent: " + keyEvent);
        if ( mediaKeyCodes.contains(keyEvent.getKeyCode())) {
            findViewById(R.id.exo_player_view).dispatchKeyEvent(keyEvent);
            return true;
        }
        return super.dispatchKeyEvent(keyEvent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupPlayer();
        videoSourceList = DefaultVideoSources.getDefaultVideoSources();
        loadButtonListener();
        onSourcesLoaded(new TaskGetSourceList.SourceListLoaded(videoSourceList, null));
        debugButtonListener();
    }


    private void setupPlayer() {
        if (player == null) {
            DefaultTrackSelector trackSelector = new DefaultTrackSelector(this);
            trackSelector.setParameters(
                    trackSelector.buildUponParameters().setMaxVideoSizeSd());
            this.renderersFactory = new DefaultRenderersFactory(this);
            player = new SimpleExoPlayer.Builder(this, renderersFactory)
                    .setTrackSelector(trackSelector)
                    .build();
        }

        PlayerView playerView = findViewById(R.id.exo_player_view);

        playerView.setPlayer(player);
        playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
        EditText edtView = findViewById(R.id.inputtext);
        View.OnFocusChangeListener ofcListener = new MyFocusChangeListener();
        edtView.setOnFocusChangeListener(ofcListener);
        player.setPlayWhenReady(true); //run file/link when ready to play.
        player.addListener(new Player.Listener(){
            @Override
            public void onIsPlayingChanged(boolean isPlaying) {
                if (isPlaying) {
                    findViewById(R.id.streamControls).setVisibility(View.GONE);
                    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    // Active playback.
                } else {
                    // Not playing because playback is paused, ended, suppressed, or the player
                    // is buffering, stopped or failed. Check player.getPlayWhenReady,
                    // player.getPlaybackState, player.getPlaybackSuppressionReason and
                    // player.getPlaybackError for details.
                    findViewById(R.id.streamControls).setVisibility(View.VISIBLE);
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                //(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                }
            }
        });
    }

    private void loadButtonListener() {
        Button button = findViewById(R.id.loadButton);
        button.setOnClickListener(view -> {
            PlayerView pv = findViewById(R.id.exo_player_view);
            pv.setVisibility(View.VISIBLE);
            EditText editText = findViewById(R.id.inputtext);
            editText.clearFocus();
            EditText inputText = findViewById(R.id.inputtext);
            String message = inputText.getText().toString();
            if (isVideoUrl(message)) {
                playStreamInPlayer(message);
            } else {
                new TaskGetSourceList((sourceList -> { onSourcesLoaded(sourceList); }))
                        .execute(message);
            }
            if(totBitrate != 0) {
                totBitrate = 0;
                numTicks = 0;
            }
        });
    }

    private void onSourcesLoaded(TaskGetSourceList.SourceListLoaded sourceListLoaded) {
        LinearLayout buttonPanel1 = findViewById(R.id.buttonpanels);
        buttonPanel1.removeAllViews();
        FlexboxLayout flexbox = new FlexboxLayout(getApplicationContext());
        flexbox.setFlexDirection(FlexDirection.ROW);
        flexbox.setFlexWrap(FlexWrap.WRAP);
        for(VideoSource s : sourceListLoaded.getSourceList()) {
            Button b = new Button(this);
            b.setText(s.getName());
            URI baseUri = sourceListLoaded.getSourceListUrl();
            String fullUrl = baseUri != null ? baseUri.resolve(s.getUrl()).toString() : s.getUrl();
            b.setTooltipText(fullUrl);
            b.setOnClickListener(view -> {
                Log.v(TAG, "Source " + s.getName() + " is lcevc: " + s.isLcevc());
                if (s.isLcevc()) {
                    renderersFactory.setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER);
                    player.release();
                    player = null;
                    setupPlayer();
                } else {
                    renderersFactory.setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF);
                }
                playStreamInPlayer(fullUrl);
            });
           flexbox.addView(b);
        }
        buttonPanel1.addView(flexbox);
    }

    private boolean isVideoUrl(String url) {
        URI uri = URI.create(url);
        String path = uri.getPath();
        return path.endsWith(".m3u8") || path.endsWith(".mpd") || path.endsWith(".mp4");
    }

    private void debugButtonListener() {
        ImageButton button = findViewById(R.id.debug_icon);
        RelativeLayout layout = findViewById(R.id.hwMetrics);
        button.setOnClickListener(view -> {
            if(layout.getVisibility() == View.VISIBLE) {
                System.out.println("Hiding metrics");
                layout.setVisibility(View.GONE);
                timer.cancel();
            } else {
                cpuMetrics.setStartTime(System.currentTimeMillis());
                timer = new Timer();
                layout.setVisibility(View.VISIBLE);

                System.out.println("Showing metrics");
                timer.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        getHardwareMetrics();
                    }
                },0, 1000);
            }
        });
    }

    private void getHardwareMetrics() {
        updateMemoryMetrics();
        updateResolutionMetrics();
        updateCpuMetrics();
        updateNetworkMetrics();
        updateFPSMetrics();
        updateCodecMetrics();
    }

    private void updateFPSMetrics() {
        TextView fpsText = findViewById(R.id.fpsText);
        float fps = (player.getVideoFormat() != null && player.getVideoFormat().frameRate > 0.0f) ? player.getVideoFormat().frameRate : 0;
        String fpsString = fps != 0 ? String.format("%.2f", fps) : "N/A";
        this.runOnUiThread(() -> fpsText.setText(String.format("Frame rate: %s", fpsString)));
    }

    private void updateNetworkMetrics() {

        if(TrafficStats.getUidRxBytes(appUID) == TrafficStats.UNSUPPORTED) {
            long rxBytes = TrafficStats.getTotalRxBytes();
            updateBitrate(rxBytes);
        } else {
            long rxBytes = TrafficStats.getUidRxBytes(appUID);
            updateBitrate(rxBytes);
        }
    }

    private void updateBitrate(long rxBytes) {
        TextView nwText = findViewById(R.id.nwText);
        TextView avgNwText = findViewById(R.id.avgNwText);

        displayedBitrate = (rxBytes - prevRxBytes) * 8 / 1000;
        this.runOnUiThread(() -> {
            if(player.isPlaying()) {
                numTicks++;
                totBitrate += displayedBitrate;
            }
                nwText.setText("App bitrate: " + displayedBitrate + "kb/s");
                avgNwText.setText("Avg bitrate: " + (int)(totBitrate / (numTicks != 0 ? numTicks : 1)) + "kb/s");
        });
        prevRxBytes = rxBytes;
    }

    private void updateCpuMetrics() {
        TextView cpuText = findViewById(R.id.cpuText);
        try {
            File file = new File(String.format("/proc/%s/stat", appPID));
            Scanner reader = new Scanner(file);
            String statResult = "";
            while(reader.hasNextLine()) {
                statResult += reader.nextLine();
            }
            long currTime = System.currentTimeMillis();
            String[] splitStatResult = statResult.split(" ");

            float cpuTimeSec = cpuMetrics.calcCpuTime(splitStatResult);
            float avgCpuUsage = cpuMetrics.calcAvgCpuUsage (currTime);
            this.runOnUiThread(() -> cpuText.setText(String.format("CPU: %.2f%%", (double) Math.abs(avgCpuUsage))));
            cpuMetrics.updateCpuMetrics(currTime, cpuTimeSec);
            cpuMetrics.updateStatMetrics(splitStatResult);
            reader.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private void updateResolutionMetrics() {
        TextView resText = findViewById(R.id.resText);
        if(player.getVideoSize() != null) {
            VideoSize videoSize =  player.getVideoSize();
            this.runOnUiThread(() -> resText.setText(String.format("Resolution: %sX%s", videoSize.width, videoSize.height)));
        } else {
            this.runOnUiThread(() -> {
                resText.setText("Resolution: Not available");
            });
        }
    }

    private void updateCodecMetrics() {
        TextView resText = findViewById(R.id.codecText);
        if(player.getVideoFormat() != null && player.getVideoFormat().codecs != null) {
            this.runOnUiThread(() -> resText.setText(String.format("Codec: %s", player.getVideoFormat().codecs)));
        } else {
            this.runOnUiThread(() -> {
                resText.setText("Codec: Not available");
            });
        }
    }

    private void updateMemoryMetrics() {
        ActivityManager activityManager = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);
        TextView memText = findViewById(R.id.memText);
        ActivityManager.MemoryInfo mem = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(mem);
        this.runOnUiThread(() -> {
            memText.setText(String.format("Memory: %.2fGB", ((double)mem.availMem / 1024 / 1024 / 1024)));
        });
    }

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
        } else if (url.endsWith("mp4")) {
            MediaItem mediaItem = new MediaItem.Builder()
                    .setUri(url)
                    .setMimeType(MimeTypes.APPLICATION_MP4)
                    .build();
            player.setMediaItem(mediaItem);
        } else {
            Log.v(TAG, "Invalid url");
        }
        player.setPlayWhenReady(false);
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