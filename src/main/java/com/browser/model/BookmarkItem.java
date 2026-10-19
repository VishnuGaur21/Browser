package com.browser.model;

import org.json.JSONObject;

public class BookmarkItem {
    private String title;
    private String url;

    public BookmarkItem(String title, String url) {
        this.title = (title != null && !title.isBlank()) ? title : url;
        this.url = url;
    }

    public static BookmarkItem fromJson(JSONObject json) {
        return new BookmarkItem(
                json.optString("title", "Untitled"),
                json.optString("url", "")
        );
    }

    public JSONObject toJson() {
        JSONObject obj = new JSONObject();
        obj.put("title", title);
        obj.put("url", url);
        return obj;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    @Override
    public String toString() {
        return title + " (" + url + ")";
    }
}
