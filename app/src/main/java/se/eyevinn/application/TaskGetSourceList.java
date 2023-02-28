package se.eyevinn.application;

import android.os.AsyncTask;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


class TaskGetSourceList extends AsyncTask<String, String, TaskGetSourceList.SourceList> {

    private Consumer<SourceList> onFinished;

    public TaskGetSourceList(Consumer<SourceList> onFinished) {
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

    @Override
    protected void onPreExecute() {
       System.out.println("Getting data ...");
    }

    @Override
    protected SourceList doInBackground(String... params) {
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
            return mapper.readValue(body, SourceList.class);
        } catch (Exception e) {
            System.out.println("Error: " + e);
            e.printStackTrace();
        }
        return new SourceList(Collections.emptyList());
    }

    @Override
    protected void onPostExecute(SourceList result) {
        System.out.println("Result: " + result);
        onFinished.accept(result);
    }
}
