package com.browser.model;

import java.io.File;

public class DownloadItem {
    public enum Status { QUEUED, DOWNLOADING, COMPLETED, FAILED, CANCELLED }

    private String fileName;
    private String url;
    private File targetFile;
    private long totalBytes;
    private long downloadedBytes;
    private Status status;
    private String errorMessage;

    public DownloadItem(String fileName, String url, File targetFile) {
        this.fileName = fileName;
        this.url = url;
        this.targetFile = targetFile;
        this.totalBytes = -1;
        this.downloadedBytes = 0;
        this.status = Status.QUEUED;
    }

    public String getFileName() { return fileName; }
    public String getUrl() { return url; }
    public File getTargetFile() { return targetFile; }
    public long getTotalBytes() { return totalBytes; }
    public void setTotalBytes(long totalBytes) { this.totalBytes = totalBytes; }
    public long getDownloadedBytes() { return downloadedBytes; }
    public void setDownloadedBytes(long downloadedBytes) { this.downloadedBytes = downloadedBytes; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public double getProgress() {
        if (totalBytes <= 0) return 0.0;
        return (double) downloadedBytes / totalBytes;
    }
}
