package com.brownjs.anonymousmessagingapp.model;

public class User {

    private String Uid;
    private String email;
    private String username;
    private String imageURL;
    private String description;
    private boolean champion;
    private String status;

    public User(String uid, String email, String username, String imageURL, String description, boolean champion, String status) {
        Uid = uid;
        this.email = email;
        this.username = username;
        this.imageURL = imageURL;
        this.description = description;
        this.champion = champion;
        this.status = status;
    }

    public User() {
    }

    public String getUid() {
        return Uid;
    }

    public void setUid(String uid) {
        Uid = uid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isChampion() {
        return champion;
    }

    public void setChampion(boolean champion) {
        this.champion = champion;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
