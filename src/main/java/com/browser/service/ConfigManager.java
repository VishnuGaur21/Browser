package com.browser.service;

import org.json.JSONObject;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ConfigManager {
    private static final String CONFIG_FILE = "config.json";

    private String geminiApiKey = "";
    private String searchEngine = "google";
    private String theme = "dark";
    private String homePage = "startpage.html";

    public ConfigManager() {
        load();
    }

    public synchronized void load() {
        File file = new File(CONFIG_FILE);
        if (!file.exists()) {
            save();
            return;
        }

        try {
            String content = Files.readString(Paths.get(CONFIG_FILE), StandardCharsets.UTF_8);
            JSONObject json = new JSONObject(content);
            this.geminiApiKey = json.optString("gemini_api_key", "");
            this.searchEngine = json.optString("search_engine", "google");
            this.theme = json.optString("theme", "dark");
            this.homePage = json.optString("home_page", "startpage.html");
        } catch (Exception e) {
            System.err.println("Failed to read config.json: " + e.getMessage());
        }
    }

    public synchronized void save() {
        try {
            JSONObject json = new JSONObject();
            json.put("gemini_api_key", geminiApiKey);
            json.put("search_engine", searchEngine);
            json.put("theme", theme);
            json.put("home_page", homePage);
            Files.writeString(Paths.get(CONFIG_FILE), json.toString(2), StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("Failed to write config.json: " + e.getMessage());
        }
    }

    public String getSearchUrl(String query) {
        String encoded = java.net.URLEncoder.encode(query, StandardCharsets.UTF_8);
        return switch (searchEngine.toLowerCase()) {
            case "duckduckgo" -> "https://duckduckgo.com/?q=" + encoded;
            case "bing" -> "https://www.bing.com/search?q=" + encoded;
            case "brave" -> "https://search.brave.com/search?q=" + encoded;
            default -> "https://www.google.com/search?q=" + encoded;
        };
    }

    public String getGeminiApiKey() { return geminiApiKey; }
    public void setGeminiApiKey(String geminiApiKey) { this.geminiApiKey = geminiApiKey; save(); }

    public String getSearchEngine() { return searchEngine; }
    public void setSearchEngine(String searchEngine) { this.searchEngine = searchEngine; save(); }

    public String getTheme() { return theme; }
    public void setTheme(String theme) { this.theme = theme; save(); }

    public String getHomePage() { return homePage; }
    public void setHomePage(String homePage) { this.homePage = homePage; save(); }
}
