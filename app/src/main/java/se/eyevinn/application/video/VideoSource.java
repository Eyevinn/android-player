package se.eyevinn.application.video;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class VideoSource {


    public VideoSource(String name,
                       String url) {
        this(name, url, false, false);
    }

    @JsonCreator
    public VideoSource(@JsonProperty("name") String name,
                       @JsonProperty("url") String url,
                       @JsonProperty("preferExtensionRenderer") boolean preferExtensionRenderer,
                       @JsonProperty("forceSoftwareDecoding") boolean forceSoftwareDecoding) {
        this.name = name;
        this.url = url;
        this.preferExtensionRenderer = preferExtensionRenderer;
        this.forceSoftwareDecoding = forceSoftwareDecoding;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public boolean isPreferExtensionRenderer() { return preferExtensionRenderer; }

    public boolean isForceSoftwareDecoding() {
        return forceSoftwareDecoding;
    }

    private String name;
    private String url;
    private boolean preferExtensionRenderer;
    private boolean forceSoftwareDecoding;
}
