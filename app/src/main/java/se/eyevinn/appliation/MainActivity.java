package se.eyevinn.appliation;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.util.MimeTypes;
import com.google.android.exoplayer2.video.VideoRendererEventListener;

import org.jetbrains.annotations.NotNull;


public class MainActivity extends AppCompatActivity implements VideoRendererEventListener {

    public static final String HLS_URL = "https://maitv-vod.lab.eyevinn.technology/VINN.mp4/master.m3u8";
    public static final String MPEG_DASH = "https://storage.googleapis.com/shaka-demo-assets/sintel-mp4-only/dash.mpd";
    private static final String TAG = "MainActivity";
    private PlayerView playerView;
    private SimpleExoPlayer player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupPlayer();
        loadButtonListener();
        hlsButtonListener();
        mpegDashButtonListener();
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
        playerView = findViewById(R.id.exo_player_view);
        playerView.setPlayer(player);
        playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
        playStreamInPlayer(HLS_URL);
        EditText edtView = (EditText) findViewById(R.id.inputtext);
        View.OnFocusChangeListener ofcListener = new MyFocusChangeListener();
        edtView.setOnFocusChangeListener(ofcListener);
        player.setPlayWhenReady(true); //run file/link when ready to play.
    }

    private void mpegDashButtonListener() {
        Button mpeg_dashButton = (Button) findViewById(R.id.mpeg_dashButton);
        mpeg_dashButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText editText = (EditText) findViewById(R.id.inputtext);
                editText.setText(MPEG_DASH, TextView.BufferType.EDITABLE);
                editText.clearFocus();
            }
        });
    }

    private void hlsButtonListener() {
        Button hlsButton = (Button) findViewById(R.id.hlsButton);
        hlsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText editText = (EditText) findViewById(R.id.inputtext);
                editText.setText(HLS_URL, TextView.BufferType.EDITABLE);
                editText.clearFocus();
            }
        });
    }

    private void loadButtonListener() {
        Button button = (Button) findViewById(R.id.loadButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText editText = (EditText) findViewById(R.id.inputtext);
                editText.clearFocus();
                EditText inputText = (EditText) findViewById(R.id.inputtext);
                String message = inputText.getText().toString();
                playStreamInPlayer(message);
            }
        });
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