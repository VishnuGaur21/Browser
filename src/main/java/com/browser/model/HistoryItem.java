package com.browser.model;

import org.json.JSONObject;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class HistoryItem {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private String title;
    private String url;
    private String time;

    public HistoryItem(String title, String url, String time) {
        this.title = (title != null && !title.isBlank()) ? title : url;
        this.url = url;
        this.time = (time != null && !time.isBlank()) ? time : LocalDateTime.now().format(FORMATTER);
    }

    public HistoryItem(String title, String url) {
        this(title, url, LocalDateTime.now().format(FORMATTER));
    }

    public static HistoryItem fromJson(JSONObject json) {
        return new HistoryItem(
                json.optString("title", "Untitled"),
                json.optString("url", ""),
                json.optString("time", "")
        );
    }

    public JSONObject toJson() {
        JSONObject obj = new JSONObject();
        obj.put("title", title);
        obj.put("url", url);
        obj.put("time", time);
        return obj;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }
}
