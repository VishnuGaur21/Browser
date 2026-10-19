package com.browser.ui;

import com.browser.service.ConfigManager;
import com.browser.service.HistoryManager;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.concurrent.Worker;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebHistory;
import javafx.scene.web.WebView;

import java.io.File;

public class BrowserTab extends StackPane {
    private final WebView webView;
    private final WebEngine webEngine;
    private final HistoryManager historyManager;
    private final ConfigManager configManager;

    private final StringProperty title = new SimpleStringProperty("New Tab");
    private final StringProperty url = new SimpleStringProperty("");
    private final DoubleProperty progress = new SimpleDoubleProperty(0.0);
    private final BooleanProperty loading = new SimpleBooleanProperty(false);
    private final BooleanProperty canGoBack = new SimpleBooleanProperty(false);
    private final BooleanProperty canGoForward = new SimpleBooleanProperty(false);
    private final DoubleProperty zoom = new SimpleDoubleProperty(1.0);

    public BrowserTab(HistoryManager historyManager, ConfigManager configManager) {
        this.historyManager = historyManager;
        this.configManager = configManager;
        this.webView = new WebView();
        this.webEngine = webView.getEngine();
        this.webEngine.setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36");

        // Dark background for webview during initial load to prevent white flash
        webView.setStyle("-fx-background-color: #0b0f19;");

        getChildren().add(webView);

        // Bind zoom
        zoom.addListener((obs, oldVal, newVal) -> webView.setZoom(newVal.doubleValue()));

        // Bind title
        webEngine.titleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.isBlank()) {
                title.set(newVal);
                historyManager.addEntry(newVal, url.get());
            }
        });

        // Bind location
        webEngine.locationProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.isBlank()) {
                url.set(newVal);
                updateHistoryNavigation();
            }
        });

        // Bind progress and load worker
        webEngine.getLoadWorker().progressProperty().addListener((obs, oldVal, newVal) -> {
            progress.set(newVal.doubleValue());
        });

        webEngine.getLoadWorker().stateProperty().addListener((obs, oldVal, newState) -> {
            if (newState == Worker.State.RUNNING || newState == Worker.State.SCHEDULED) {
                loading.set(true);
            } else {
                loading.set(false);
                updateHistoryNavigation();

                if (newState == Worker.State.SUCCEEDED) {
                    String currentTitle = webEngine.getTitle();
                    if (currentTitle == null || currentTitle.isBlank()) {
                        currentTitle = extractHostOrTitle(webEngine.getLocation());
                    }
                    title.set(currentTitle);
                    historyManager.addEntry(currentTitle, webEngine.getLocation());
                } else if (newState == Worker.State.FAILED) {
                    title.set("Load Failed");
                }
            }
        });

        // Handle popup requests
        webEngine.setCreatePopupHandler(config -> {
            return webEngine;
        });

        // JavaScript alerts handling: intercepts navigation and external app signals safely
        webEngine.setOnAlert(event -> {
            String data = event.getData();
            if (data != null && data.startsWith("NAVIGATE:")) {
                String targetUrl = data.substring(9).trim();
                Platform.runLater(() -> load(targetUrl));
            } else if (data != null && data.startsWith("CHROME:")) {
                String chromeQuery = data.substring(7).trim();
                Platform.runLater(() -> {
                    try {
                        String chromeUrl = chromeQuery.startsWith("http") ? chromeQuery :
                                "https://www.youtube.com/results?search_query=" + java.net.URLEncoder.encode(chromeQuery, java.nio.charset.StandardCharsets.UTF_8);
                        new ProcessBuilder("cmd", "/c", "start", "chrome", chromeUrl).start();
                    } catch (Exception e) {
                        System.err.println("Could not launch Chrome: " + e.getMessage());
                    }
                });
            } else {
                System.out.println("Web Alert: " + data);
            }
        });
    }

    private void updateHistoryNavigation() {
        WebHistory hist = webEngine.getHistory();
        int currentIndex = hist.getCurrentIndex();
        canGoBack.set(currentIndex > 0);
        canGoForward.set(currentIndex < hist.getEntries().size() - 1);
    }

    private String extractHostOrTitle(String fullUrl) {
        if (fullUrl == null) return "New Tab";
        if (fullUrl.contains("startpage.html")) return "New Tab";
        if (fullUrl.contains("player.html")) return "YouTube Player";
        try {
            java.net.URI uri = java.net.URI.create(fullUrl);
            String host = uri.getHost();
            if (host != null && !host.isBlank()) return host;
        } catch (Exception ignored) {}
        return fullUrl;
    }

    public void load(String target) {
        if (target == null || target.isBlank()) return;

        String formatted = target.trim();
        if (formatted.equalsIgnoreCase("about:blank") || formatted.equalsIgnoreCase("newtab")) {
            loadStartPage();
            return;
        }

        // Direct local file check
        if (formatted.startsWith("file:/")) {
            webEngine.load(formatted);
            return;
        }

        // Check if startpage.html
        if (formatted.equalsIgnoreCase("startpage.html") || formatted.contains("startpage.html")) {
            loadStartPage();
            return;
        }

        // Check if player.html
        if (formatted.equalsIgnoreCase("player.html") || formatted.contains("player.html")) {
            loadPlayerPage("popular music");
            return;
        }

        // Check if full URL
        if (formatted.startsWith("http://") || formatted.startsWith("https://")) {
            webEngine.load(formatted);
            return;
        }

        // Check if domain like google.com or github.com
        if (formatted.matches("^[a-zA-Z0-9-]+\\.[a-zA-Z]{2,}(/.*)?$")) {
            webEngine.load("https://" + formatted);
            return;
        }

        // Otherwise use configured search engine
        String searchUrl = configManager.getSearchUrl(formatted);
        webEngine.load(searchUrl);
    }

    public void loadStartPage() {
        File startPageFile = new File("startpage.html");
        if (startPageFile.exists()) {
            webEngine.load(startPageFile.toURI().toString());
            title.set("New Tab");
        } else {
            webEngine.loadContent("<html><body style='background:#0b0f19;color:#fff;font-family:sans-serif;padding:40px;'><h1>Welcome to Modern Browser</h1></body></html>");
            title.set("New Tab");
        }
    }

    public void reload() {
        webEngine.reload();
    }

    public void stop() {
        webEngine.getLoadWorker().cancel();
        loading.set(false);
    }

    public void goBack() {
        WebHistory hist = webEngine.getHistory();
        if (hist.getCurrentIndex() > 0) {
            hist.go(-1);
            updateHistoryNavigation();
        }
    }

    public void goForward() {
        WebHistory hist = webEngine.getHistory();
        if (hist.getCurrentIndex() < hist.getEntries().size() - 1) {
            hist.go(1);
            updateHistoryNavigation();
        }
    }

    public void zoomIn() {
        double current = zoom.get();
        if (current < 2.5) {
            zoom.set(Math.round((current + 0.1) * 10.0) / 10.0);
        }
    }

    public void zoomOut() {
        double current = zoom.get();
        if (current > 0.4) {
            zoom.set(Math.round((current - 0.1) * 10.0) / 10.0);
        }
    }

    public void resetZoom() {
        zoom.set(1.0);
    }

    public void loadPlayerPage(String query) {
        File playerFile = new File("player.html");
        String cleanQuery = (query != null && !query.isBlank()) ? query : "popular music";
        String encoded = java.net.URLEncoder.encode(cleanQuery, java.nio.charset.StandardCharsets.UTF_8);
        if (playerFile.exists()) {
            webEngine.load(playerFile.toURI().toString() + "#q=" + encoded);
        } else {
            webEngine.load("https://www.youtube-nocookie.com/embed?listType=search&list=" + encoded + "&autoplay=1");
        }
        title.set("Playing: " + cleanQuery);
    }

    public void playYouTubeAndAutoStart(String query, String targetUrl) {
        String cleanQuery = (query != null && !query.isBlank()) ? query : "relaxing music";
        loadPlayerPage(cleanQuery);
    }

    public void extractPageText(java.util.function.Consumer<String> callback) {
        try {
            Object result = webEngine.executeScript(
                    "(function() { " +
                    "  var clone = document.body ? document.body.cloneNode(true) : null; " +
                    "  if (!clone) return ''; " +
                    "  var scripts = clone.querySelectorAll('script, style, noscript, svg, nav, footer'); " +
                    "  scripts.forEach(s => s.remove()); " +
                    "  return clone.innerText || clone.textContent || ''; " +
                    "})()"
            );
            callback.accept(result != null ? result.toString() : "");
        } catch (Exception e) {
            callback.accept("");
        }
    }

    public void extractPageHtml(java.util.function.Consumer<String> callback) {
        try {
            Object result = webEngine.executeScript("document.documentElement ? document.documentElement.outerHTML : ''");
            callback.accept(result != null ? result.toString() : "");
        } catch (Exception e) {
            callback.accept("<!-- Failed to extract HTML: " + e.getMessage() + " -->");
        }
    }

    public boolean findInPage(String text, boolean forward) {
        if (text == null || text.isBlank()) return false;
        try {
            String escaped = text.replace("'", "\\'");
            Object res = webEngine.executeScript("window.find('" + escaped + "', false, " + (!forward) + ", true, false, false, false);");
            return Boolean.TRUE.equals(res);
        } catch (Exception e) {
            return false;
        }
    }

    public void toggleReaderMode() {
        try {
            webEngine.executeScript(
                    "(function() { " +
                    "  if (document.getElementById('__modern_reader_style__')) { " +
                    "    document.getElementById('__modern_reader_style__').remove(); " +
                    "    return; " +
                    "  } " +
                    "  var style = document.createElement('style'); " +
                    "  style.id = '__modern_reader_style__'; " +
                    "  style.innerHTML = ` " +
                    "    body { max-width: 760px !important; margin: 40px auto !important; padding: 20px !important; " +
                    "           font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif !important; " +
                    "           line-height: 1.8 !important; font-size: 18px !important; background: #0f172a !important; color: #f1f5f9 !important; } " +
                    "    header, footer, nav, aside, .ad, .ads, .sidebar { display: none !important; } " +
                    "    p, li, h1, h2, h3, h4 { color: #f1f5f9 !important; } " +
                    "    a { color: #818cf8 !important; } " +
                    "    img { max-width: 100% !important; border-radius: 8px !important; margin: 16px 0 !important; } " +
                    "  `; " +
                    "  document.head.appendChild(style); " +
                    "})()"
            );
        } catch (Exception ignored) {}
    }

    // Properties
    public WebView getWebView() { return webView; }
    public WebEngine getWebEngine() { return webEngine; }
    public StringProperty titleProperty() { return title; }
    public StringProperty urlProperty() { return url; }
    public DoubleProperty progressProperty() { return progress; }
    public BooleanProperty loadingProperty() { return loading; }
    public BooleanProperty canGoBackProperty() { return canGoBack; }
    public BooleanProperty canGoForwardProperty() { return canGoForward; }
    public DoubleProperty zoomProperty() { return zoom; }
}
