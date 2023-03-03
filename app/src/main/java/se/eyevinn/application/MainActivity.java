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
import com.google.android.exoplayer2.mediacodec.MediaCodecInfo;
import com.google.android.exoplayer2.mediacodec.MediaCodecSelector;
import com.google.android.exoplayer2.mediacodec.MediaCodecUtil;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.util.MimeTypes;
import com.google.android.exoplayer2.video.VideoRendererEventListener;
import com.google.android.exoplayer2.video.VideoSize;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayout;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

import se.eyevinn.application.video.DefaultVideoSources;
import se.eyevinn.application.video.VideoSource;
import se.eyevinn.application.video.VideoSourceList;


public class MainActivity extends AppCompatActivity implements VideoRendererEventListener {

    private static final String TAG = "MainActivity";
    private SimpleExoPlayer player;
    private Timer timer;
    private VideoSourceList videoSourceList;
    private static final int appPID = Process.myPid();
    private static final int appUID = Process.myUid();
    private long prevRxBytes = 0;
    private long displayedBitrate = 0;
    private long totBitrate = 0;
    private float avgCpu = 0.0f;
    private long numTicks = 1;

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
        setupPlayer(false, false);
        videoSourceList = DefaultVideoSources.getDefaultVideoSources();
        loadButtonListener();
        onSourcesLoaded(new TaskGetSourceList.SourceListLoaded(videoSourceList, null));
        debugButtonListener();
    }

    private void setupPlayer(boolean isLcevc, boolean forceSoftwareDecoding) {
        if (player == null) {
            DefaultTrackSelector trackSelector = new DefaultTrackSelector(this);
            trackSelector.setParameters(
                    trackSelector.buildUponParameters().setMaxVideoSizeSd());
            this.renderersFactory = new DefaultRenderersFactory(this);
            if (forceSoftwareDecoding) {
                renderersFactory.setMediaCodecSelector((mimeType, requiresSecureDecoder, requiresTunnelingDecoder) -> {
                    List<MediaCodecInfo> infos =
                            MediaCodecSelector.DEFAULT.getDecoderInfos(mimeType, requiresSecureDecoder, requiresTunnelingDecoder);
                    return infos.stream().filter(i -> !i.hardwareAccelerated).collect(Collectors.toList());
                });
            }
            if (isLcevc) {
                renderersFactory.setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER);
            }
            player = new SimpleExoPlayer.Builder(this)
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
            if(message.length() == 0) {
                onSourcesLoaded(new TaskGetSourceList.SourceListLoaded(videoSourceList, null));
            } else if (isVideoUrl(message)) {
                playStreamInPlayer(message);
            } else {
                new TaskGetSourceList((sourceList -> { onSourcesLoaded(sourceList); }))
                        .execute(message);
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
                resetBitrateCounter();
                Log.v(TAG, "Source " + s.getName() + " is lcevc: " + s.isLcevc());

                player.release();
                player = null;
                setupPlayer(s.isLcevc(), s.isForceSoftwareDecoding());

                playStreamInPlayer(fullUrl);
            });
           flexbox.addView(b);
        }
        buttonPanel1.addView(flexbox);
    }

    private void resetBitrateCounter() {
        this.prevRxBytes = 0;
        this.displayedBitrate = 0;
        this.avgCpu = 0.0f;
        this.totBitrate = 0;
        this.numTicks = 1;
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
        numTicks++;
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
        totBitrate += numTicks > 1 ? displayedBitrate : 0;
        this.runOnUiThread(() -> {
            nwText.setText("App bitrate: " + displayedBitrate + "kb/s");
            avgNwText.setText("Avg bitrate: " + (int)(totBitrate / numTicks) + "kb/s");
        });
        prevRxBytes = rxBytes;
    }

    private void updateCpuMetrics() {
        TextView cpuText = findViewById(R.id.cpuText);
        TextView avgCpuText = findViewById(R.id.avgCpuText);
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
            float CurrCpuUsage = cpuMetrics.calcCurrCpuUsage(currTime);
            this.avgCpu += CurrCpuUsage;
            this.runOnUiThread(() -> {
                cpuText.setText(String.format("CPU: %.2f%%", (double) Math.abs(CurrCpuUsage)));
                avgCpuText.setText(String.format("Avg CPU: %.2f%%", (double) Math.abs(avgCpu / numTicks)));

            });
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