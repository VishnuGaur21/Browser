package com.browser.service;

import com.browser.model.HistoryItem;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class HistoryManager {
    private static final String HISTORY_FILE = "history.json";
    private final ObservableList<HistoryItem> history = FXCollections.observableArrayList();

    public HistoryManager() {
        load();
    }

    public ObservableList<HistoryItem> getHistory() {
        return history;
    }

    public synchronized void load() {
        history.clear();
        File file = new File(HISTORY_FILE);
        if (!file.exists()) return;

        try {
            String content = Files.readString(Paths.get(HISTORY_FILE), StandardCharsets.UTF_8);
            JSONArray array = new JSONArray(content);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                history.add(HistoryItem.fromJson(obj));
            }
        } catch (Exception e) {
            System.err.println("Failed to read history.json: " + e.getMessage());
        }
    }

    public synchronized void save() {
        try {
            JSONArray array = new JSONArray();
            // Store up to 1000 items
            int limit = Math.min(history.size(), 1000);
            for (int i = 0; i < limit; i++) {
                array.put(history.get(i).toJson());
            }
            Files.writeString(Paths.get(HISTORY_FILE), array.toString(2), StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("Failed to write history.json: " + e.getMessage());
        }
    }

    public void addEntry(String title, String url) {
        if (url == null || url.isBlank() || url.startsWith("about:") || url.contains("startpage.html")) {
            return;
        }

        Platform.runLater(() -> {
            // Avoid duplicate consecutive entry with same URL
            if (!history.isEmpty() && history.get(0).getUrl().equalsIgnoreCase(url)) {
                if (title != null && !title.isBlank() && !title.equals(url)) {
                    history.get(0).setTitle(title);
                }
                return;
            }

            history.add(0, new HistoryItem(title, url));
            save();
        });
    }

    public void removeEntry(HistoryItem item) {
        history.remove(item);
        save();
    }

    public void clearHistory() {
        history.clear();
        save();
    }
}
