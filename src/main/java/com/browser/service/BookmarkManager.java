package com.browser.service;

import com.browser.model.BookmarkItem;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class BookmarkManager {
    private static final String BOOKMARKS_FILE = "bookmarks.json";
    private final ObservableList<BookmarkItem> bookmarks = FXCollections.observableArrayList();

    public BookmarkManager() {
        load();
    }

    public ObservableList<BookmarkItem> getBookmarks() {
        return bookmarks;
    }

    public synchronized void load() {
        bookmarks.clear();
        File file = new File(BOOKMARKS_FILE);
        if (!file.exists()) {
            addDefaultBookmarks();
            save();
            return;
        }

        try {
            String content = Files.readString(Paths.get(BOOKMARKS_FILE), StandardCharsets.UTF_8);
            JSONArray array = new JSONArray(content);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                bookmarks.add(BookmarkItem.fromJson(obj));
            }
        } catch (Exception e) {
            System.err.println("Failed to read bookmarks.json: " + e.getMessage());
            addDefaultBookmarks();
        }
    }

    private void addDefaultBookmarks() {
        bookmarks.add(new BookmarkItem("Google", "https://google.com"));
        bookmarks.add(new BookmarkItem("YouTube", "https://youtube.com"));
        bookmarks.add(new BookmarkItem("GitHub", "https://github.com"));
        bookmarks.add(new BookmarkItem("Wikipedia", "https://wikipedia.org"));
    }

    public synchronized void save() {
        try {
            JSONArray array = new JSONArray();
            for (BookmarkItem item : bookmarks) {
                array.put(item.toJson());
            }
            Files.writeString(Paths.get(BOOKMARKS_FILE), array.toString(2), StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("Failed to write bookmarks.json: " + e.getMessage());
        }
    }

    public boolean isBookmarked(String url) {
        if (url == null || url.isBlank()) return false;
        String cleanUrl = url.trim().toLowerCase();
        if (cleanUrl.endsWith("/")) cleanUrl = cleanUrl.substring(0, cleanUrl.length() - 1);
        for (BookmarkItem item : bookmarks) {
            String itemUrl = item.getUrl().trim().toLowerCase();
            if (itemUrl.endsWith("/")) itemUrl = itemUrl.substring(0, itemUrl.length() - 1);
            if (cleanUrl.equals(itemUrl)) return true;
        }
        return false;
    }

    public void addBookmark(String title, String url) {
        if (url == null || url.isBlank()) return;
        if (!isBookmarked(url)) {
            bookmarks.add(new BookmarkItem(title, url));
            save();
        }
    }

    public void removeBookmark(String url) {
        if (url == null || url.isBlank()) return;
        String trimmed = url.trim().toLowerCase();
        final String target = trimmed.endsWith("/") ? trimmed.substring(0, trimmed.length() - 1) : trimmed;
        bookmarks.removeIf(item -> {
            String itemUrl = item.getUrl().trim().toLowerCase();
            if (itemUrl.endsWith("/")) itemUrl = itemUrl.substring(0, itemUrl.length() - 1);
            return target.equals(itemUrl);
        });
        save();
    }

    public void toggleBookmark(String title, String url) {
        if (isBookmarked(url)) {
            removeBookmark(url);
        } else {
            addBookmark(title, url);
        }
    }
}
