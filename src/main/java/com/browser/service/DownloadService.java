package com.browser.service;

import com.browser.model.DownloadItem;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.concurrent.CompletableFuture;

public class DownloadService {
    private final ObservableList<DownloadItem> downloads = FXCollections.observableArrayList();
    private final File downloadFolder;

    public DownloadService() {
        String userHome = System.getProperty("user.home", ".");
        File defaultFolder = new File(userHome, "Downloads");
        if (!defaultFolder.exists()) {
            defaultFolder.mkdirs();
        }
        this.downloadFolder = defaultFolder;
    }

    public ObservableList<DownloadItem> getDownloads() {
        return downloads;
    }

    public File getDownloadFolder() {
        return downloadFolder;
    }

    public void startDownload(String fileUrl, String suggestedName) {
        if (fileUrl == null || fileUrl.isBlank()) return;

        String fileName = suggestedName;
        if (fileName == null || fileName.isBlank()) {
            try {
                URI uri = URI.create(fileUrl);
                String path = uri.getPath();
                if (path != null && path.contains("/")) {
                    fileName = path.substring(path.lastIndexOf('/') + 1);
                }
            } catch (Exception ignored) {}
        }
        if (fileName == null || fileName.isBlank()) {
            fileName = "download_" + System.currentTimeMillis();
        }

        File targetFile = new File(downloadFolder, fileName);
        // Avoid overwriting
        int count = 1;
        String baseName = fileName;
        String ext = "";
        int dotIdx = fileName.lastIndexOf('.');
        if (dotIdx > 0) {
            baseName = fileName.substring(0, dotIdx);
            ext = fileName.substring(dotIdx);
        }
        while (targetFile.exists()) {
            targetFile = new File(downloadFolder, baseName + " (" + count + ")" + ext);
            count++;
        }

        final File finalTargetFile = targetFile;
        DownloadItem item = new DownloadItem(finalTargetFile.getName(), fileUrl, finalTargetFile);
        downloads.add(0, item);

        CompletableFuture.runAsync(() -> {
            Platform.runLater(() -> item.setStatus(DownloadItem.Status.DOWNLOADING));
            try {
                URL url = URI.create(fileUrl).toURL();
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("User-Agent", "Mozilla/5.0");
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(30000);

                int responseCode = conn.getResponseCode();
                if (responseCode >= 400) {
                    throw new Exception("HTTP " + responseCode + ": " + conn.getResponseMessage());
                }

                long contentLength = conn.getContentLengthLong();
                Platform.runLater(() -> item.setTotalBytes(contentLength));

                try (InputStream in = conn.getInputStream();
                     FileOutputStream out = new FileOutputStream(finalTargetFile)) {

                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    long totalRead = 0;
                    long lastUpdate = 0;

                    while ((bytesRead = in.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                        totalRead += bytesRead;

                        long now = System.currentTimeMillis();
                        if (now - lastUpdate > 200) {
                            final long currentRead = totalRead;
                            Platform.runLater(() -> item.setDownloadedBytes(currentRead));
                            lastUpdate = now;
                        }
                    }

                    final long finalRead = totalRead;
                    Platform.runLater(() -> {
                        item.setDownloadedBytes(finalRead);
                        item.setStatus(DownloadItem.Status.COMPLETED);
                    });
                }
            } catch (Exception e) {
                Platform.runLater(() -> {
                    item.setStatus(DownloadItem.Status.FAILED);
                    item.setErrorMessage(e.getMessage());
                });
            }
        });
    }
}
