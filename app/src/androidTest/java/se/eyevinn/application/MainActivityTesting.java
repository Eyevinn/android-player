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
    public void verifyClickHlsVodButton() {
        MainActivity activity = rule.getActivity();
        Button hlsButton = (Button) activity.findViewById(R.id.hlsVodButton);
        assertNotNull(hlsButton);
        hlsButton.performClick();
        EditText editText = (EditText) activity.findViewById(R.id.inputtext);
        String message = editText.getText().toString();
        assertNotNull(message);
        assertThat(message, is("https://f53accc45b7aded64ed8085068f31881.egress.mediapackage-vod.eu-north-1.amazonaws.com/out/v1/1c63bf88e2664639a6c293b4d055e6bb/ade303f83e8444d69b7658f988abb054/2a647c0cf9b7409598770b9f11799178/manifest.m3u8"));
    }

    @UiThreadTest
    @Test
    public void verifyClickMpdVodButton() {
        MainActivity activity = rule.getActivity();
        Button mpegDashButton = (Button) activity.findViewById(R.id.mpdVodButton);
        assertNotNull(mpegDashButton);
        mpegDashButton.performClick();
        EditText editText = (EditText) activity.findViewById(R.id.inputtext);
        String message = editText.getText().toString();
        assertNotNull(message);
        assertThat(message, is("https://f53accc45b7aded64ed8085068f31881.egress.mediapackage-vod.eu-north-1.amazonaws.com/out/v1/1c63bf88e2664639a6c293b4d055e6bb/64651f16da554640930b7ce2cd9f758b/66d211307b7d43d3bd515a3bfb654e1c/manifest.mpd"));
    }

    @UiThreadTest
    @Test
    public void verifyHlsLiveClickButton() throws InterruptedException {
        MainActivity activity = rule.getActivity();
        Button mpegDashButton = (Button) activity.findViewById(R.id.hlsLiveButton);
        mpegDashButton.performClick();
        Button loadButton = (Button) activity.findViewById(R.id.loadButton);
        loadButton.performClick();
        PlayerView playerView = (PlayerView) activity.findViewById(R.id.exo_player_view);
        String url = playerView.getPlayer().getCurrentMediaItem().playbackProperties.uri.toString();
        assertThat(url, is("https://d2fz24s2fts31b.cloudfront.net/out/v1/6484d7c664924b77893f9b4f63080e5d/manifest.m3u8"));
    }

    @UiThreadTest
    @Test
    public void verifyMpdLiveClickButton() throws InterruptedException {
        MainActivity activity = rule.getActivity();
        Button mpegDashButton = (Button) activity.findViewById(R.id.mpdLiveButton);
        mpegDashButton.performClick();
        Button loadButton = (Button) activity.findViewById(R.id.loadButton);
        loadButton.performClick();
        PlayerView playerView = (PlayerView) activity.findViewById(R.id.exo_player_view);
        String url = playerView.getPlayer().getCurrentMediaItem().playbackProperties.uri.toString();
        assertThat(url, is("https://d2fz24s2fts31b.cloudfront.net/out/v1/3b6879c0836346c2a44c9b4b33520f4e/manifest.mpd"));
    }

    @UiThreadTest
    @Test
    public void verifyHlsLiveSsaiClickButton() throws InterruptedException {
        MainActivity activity = rule.getActivity();
        Button mpegDashButton = (Button) activity.findViewById(R.id.hlsLiveSsaiButton);
        mpegDashButton.performClick();
        Button loadButton = (Button) activity.findViewById(R.id.loadButton);
        loadButton.performClick();
        PlayerView playerView = (PlayerView) activity.findViewById(R.id.exo_player_view);
        String url = playerView.getPlayer().getCurrentMediaItem().playbackProperties.uri.toString();
        assertThat(url, is("https://edfaeed9c7154a20828a30a26878ade0.mediatailor.eu-west-1.amazonaws.com/v1/master/1b8a07d9a44fe90e52d5698704c72270d177ae74/AdTest/master.m3u8"));
    }
}
