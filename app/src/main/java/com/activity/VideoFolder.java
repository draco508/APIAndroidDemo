package com.activity;

/**
 * Created by QuangPH on 2025-08-22
 */
public class VideoFolder {
    private String folderPath;
    private String folderName;
    private int videoCount;

    public VideoFolder(String folderPath, String folderName, int videoCount) {
        this.folderPath = folderPath;
        this.folderName = folderName;
        this.videoCount = videoCount;
    }

    public String getFolderPath() {
        return folderPath;
    }

    public String getFolderName() {
        return folderName;
    }

    public int getVideoCount() {
        return videoCount;
    }
}

