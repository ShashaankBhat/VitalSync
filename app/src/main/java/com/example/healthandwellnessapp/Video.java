package com.example.healthandwellnessapp;

import java.util.List;

public class Video {
    private String title;
    private List<String> tags; // Use List instead of array
    private String image;
    private String youtubeUrl;

    // No-argument constructor
    public Video() {
    }

    public Video(String title, List<String> tags, String image, String youtubeUrl) {
        this.title = title;
        this.tags = tags;
        this.image = image;
        this.youtubeUrl = youtubeUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getYoutubeUrl() {
        return youtubeUrl;
    }

    public void setYoutubeUrl(String youtubeUrl) {
        this.youtubeUrl = youtubeUrl;
    }
}
