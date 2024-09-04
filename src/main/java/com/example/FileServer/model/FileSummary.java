package com.example.FileServer.model;

public class FileSummary {

    private long totalFiles;
    private long totalStorage;

    public FileSummary(long totalFiles, long totalStorage) {
        this.totalFiles = totalFiles;
        this.totalStorage = totalStorage;
    }

    public long getTotalFiles() {
        return totalFiles;
    }

    public void setTotalFiles(long totalFiles) {
        this.totalFiles = totalFiles;
    }

    public long getTotalStorage() {
        return totalStorage;
    }

    public void setTotalStorage(long totalStorage) {
        this.totalStorage = totalStorage;
    }
}
