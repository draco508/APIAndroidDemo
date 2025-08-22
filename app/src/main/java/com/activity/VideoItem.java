package com.activity;

/**
 * Created by QuangPH on 2025-08-20
 */
public class VideoItem {
    public  String id;
    public  String duration;
    public  String thumbnailBase64;

    public VideoItem(String id, String duration, String thumbnailBase64) {
        this.id = id;
        this.duration = duration;
        this.thumbnailBase64 = thumbnailBase64;
    }
}
