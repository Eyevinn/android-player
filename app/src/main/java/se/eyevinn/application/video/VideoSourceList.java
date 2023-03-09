package se.eyevinn.application.video;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class VideoSourceList {
    @JsonCreator
    public VideoSourceList(@JsonProperty("sourceList") List<VideoSource> videoSourceList) {
        this.videoSourceList = videoSourceList;
    }

    public List<VideoSource> getSourceList() {
        return videoSourceList;
    }

    private List<VideoSource> videoSourceList;
}
