package se.eyevinn.application.video;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DefaultVideoSources {

    public static VideoSourceList getDefaultVideoSources() {
        List<VideoSource> videoSources = Arrays.asList(
                new VideoSource("HLS_VOD", "https://f53accc45b7aded64ed8085068f31881.egress.mediapackage-vod.eu-north-1.amazonaws.com/out/v1/1c63bf88e2664639a6c293b4d055e6bb/ade303f83e8444d69b7658f988abb054/2a647c0cf9b7409598770b9f11799178/manifest.m3u8"),
                new VideoSource( "MPD_VOD", "https://f53accc45b7aded64ed8085068f31881.egress.mediapackage-vod.eu-north-1.amazonaws.com/out/v1/1c63bf88e2664639a6c293b4d055e6bb/64651f16da554640930b7ce2cd9f758b/66d211307b7d43d3bd515a3bfb654e1c/manifest.mpd"),
                new VideoSource("HLS_LIVE", "https://d2fz24s2fts31b.cloudfront.net/out/v1/6484d7c664924b77893f9b4f63080e5d/manifest.m3u8"),
                new VideoSource("MPD_LIVE", "https://d2fz24s2fts31b.cloudfront.net/out/v1/3b6879c0836346c2a44c9b4b33520f4e/manifest.mpd"),
                new VideoSource("HLS_LIVE_SSAI", "https://edfaeed9c7154a20828a30a26878ade0.mediatailor.eu-west-1.amazonaws.com/v1/master/1b8a07d9a44fe90e52d5698704c72270d177ae74/AdTest/master.m3u8")
        );
        return new VideoSourceList(videoSources);
    }
}
