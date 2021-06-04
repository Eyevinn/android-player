package se.eyevinn.application;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.test.annotation.UiThreadTest;
import androidx.test.filters.LargeTest;
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner;
import androidx.test.rule.ActivityTestRule;

import com.google.android.exoplayer2.ui.PlayerView;

import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4ClassRunner.class)
@LargeTest
public class MainActivityTesting {

    @Rule
    public ActivityTestRule<MainActivity> rule = new ActivityTestRule<>(MainActivity.class);

    @Test
    public void ensureExoPlayerIsPresent() {
        MainActivity activity = rule.getActivity();
        View viewById = activity.findViewById(R.id.exo_player_view);
        assertNotNull(viewById);
        assertThat(viewById, Matchers.instanceOf(PlayerView.class));
    }

    @UiThreadTest
    @Test
    public void verifyClickHLSButton() {
        MainActivity activity = rule.getActivity();
        Button hlsButton = (Button) activity.findViewById(R.id.hlsButton);
        assertNotNull(hlsButton);
        hlsButton.performClick();
        EditText editText = (EditText) activity.findViewById(R.id.inputtext);
        String message = editText.getText().toString();
        assertNotNull(message);
        assertThat(message, is("https://maitv-vod.lab.eyevinn.technology/VINN.mp4/master.m3u8"));
    }

    @UiThreadTest
    @Test
    public void verifyClickMPEGDASHButton() {
        MainActivity activity = rule.getActivity();
        Button mpegDashButton = (Button) activity.findViewById(R.id.mpeg_dashButton);
        assertNotNull(mpegDashButton);
        mpegDashButton.performClick();
        EditText editText = (EditText) activity.findViewById(R.id.inputtext);
        String message = editText.getText().toString();
        assertNotNull(message);
        assertThat(message, is("https://storage.googleapis.com/shaka-demo-assets/sintel-mp4-only/dash.mpd"));
    }

    @UiThreadTest
    @Test
    public void verifyClickButton() throws InterruptedException {
        MainActivity activity = rule.getActivity();
        Button mpegDashButton = (Button) activity.findViewById(R.id.mpeg_dashButton);
        mpegDashButton.performClick();
        Button loadButton = (Button) activity.findViewById(R.id.loadButton);
        loadButton.performClick();
        PlayerView playerView = (PlayerView) activity.findViewById(R.id.exo_player_view);
        String url = playerView.getPlayer().getCurrentMediaItem().playbackProperties.uri.toString();
        assertThat(url, is("https://storage.googleapis.com/shaka-demo-assets/sintel-mp4-only/dash.mpd"));
    }
}
