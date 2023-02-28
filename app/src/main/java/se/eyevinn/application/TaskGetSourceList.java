package se.eyevinn.application;

import android.os.AsyncTask;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


class TaskGetSourceList extends AsyncTask<String, String, TaskGetSourceList.SourceListLoaded> {

    private Consumer<SourceListLoaded> onFinished;

    public TaskGetSourceList(Consumer<SourceListLoaded> onFinished) {
        this.onFinished = onFinished;
    }

    static class SourceList {
        @JsonCreator
        public SourceList(@JsonProperty("sourceList") List<Source> sourceList) {
            this.sourceList = sourceList;
        }

        public List<Source> getSourceList() {
            return sourceList;
        }

        private List<Source> sourceList;
    }

    static class Source {
        @JsonCreator
        public Source(@JsonProperty("name") String name, @JsonProperty("url") String url) {
            this.name = name;
            this.url = url;
        }

        public String getName() {
            return name;
        }

        public String getUrl() {
            return url;
        }

        private String name;
        private String url;
    }

    static class SourceListLoaded {
        private SourceList sourceList;
        private URI sourceListUrl;

        public SourceListLoaded(SourceList sourceList, URI sourceListUrl) {
            this.sourceList = sourceList;
            this.sourceListUrl = sourceListUrl;
        }

        public List<Source> getSourceList() {
            return sourceList.getSourceList();
        }

        public URI getSourceListUrl() {
            return sourceListUrl;
        }
    }

    @Override
    protected void onPreExecute() {
       System.out.println("Getting data ...");
    }

    @Override
    protected SourceListLoaded doInBackground(String... params) {
        OkHttpClient client = new OkHttpClient();
        String url = params[0];
        Request request = new Request.Builder()
                .url(url)
                .build();

        ObjectMapper mapper = new ObjectMapper();

        try {
            Response response = client.newCall(request).execute();
            String body = response.body().string();
            System.out.println("Got body: " + body);
            return new SourceListLoaded(mapper.readValue(body, SourceList.class), URI.create(url));
        } catch (Exception e) {
            System.out.println("Error: " + e);
            e.printStackTrace();
        }
        return new SourceListLoaded(new SourceList(Collections.emptyList()), URI.create(url));
    }

    @Override
    protected void onPostExecute(SourceListLoaded result) {
        System.out.println("Result: " + result);
        onFinished.accept(result);
    }
}
