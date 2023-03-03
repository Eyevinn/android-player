package se.eyevinn.application.video;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Nullable;

@JsonIgnoreProperties()
public class VideoSource {


    public VideoSource(String name,
                       String url) {
        this(name, url, false, false);
    }

    @JsonCreator
    public VideoSource(@JsonProperty("name") String name,
                       @JsonProperty("url") String url,
                       @Nullable  @JsonProperty("lcevc") boolean lcevc,
                       @Nullable  @JsonProperty("forceSoftwareDecoding") boolean forceSoftwareDecoding) {
        this.name = name;
        this.url = url;
        this.lcevc = lcevc;
        this.forceSoftwareDecoding = forceSoftwareDecoding;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public boolean isLcevc() { return lcevc; }

    public boolean isForceSoftwareDecoding() {
        return forceSoftwareDecoding;
    }

    private String name;
    private String url;
    private boolean lcevc;
    private boolean forceSoftwareDecoding;
}
