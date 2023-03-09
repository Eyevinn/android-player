package se.eyevinn.application;

import android.os.AsyncTask;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import se.eyevinn.application.video.VideoSourceList;
import se.eyevinn.application.video.VideoSource;


class TaskGetSourceList extends AsyncTask<String, String, TaskGetSourceList.SourceListLoaded> {

    private Consumer<SourceListLoaded> onFinished;

    public TaskGetSourceList(Consumer<SourceListLoaded> onFinished) {
        this.onFinished = onFinished;
    }

    static class SourceListLoaded {
        private VideoSourceList videoSourceList;
        private URI sourceListUrl;

        public SourceListLoaded(VideoSourceList videoSourceList, URI sourceListUrl) {
            this.videoSourceList = videoSourceList;
            this.sourceListUrl = sourceListUrl;
        }

        public List<VideoSource> getSourceList() {
            return videoSourceList.getSourceList();
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
            return new SourceListLoaded(mapper.readValue(body, VideoSourceList.class), URI.create(url));
        } catch (Exception e) {
            System.out.println("Error: " + e);
            e.printStackTrace();
        }
        return new SourceListLoaded(new VideoSourceList(Collections.emptyList()), URI.create(url));
    }

    @Override
    protected void onPostExecute(SourceListLoaded result) {
        System.out.println("Result: " + result);
        onFinished.accept(result);
    }
}
