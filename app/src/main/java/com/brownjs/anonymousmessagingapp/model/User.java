package com.brownjs.anonymousmessagingapp.model;

/**
 * Class to hold the User document
 */
public class User {

    private String id;
    private String email;
    private String phone;
    private String username;
    private String imageURL;
    private String description;
    private boolean champion;
    private String status;

    /**
     *
     * @param id user id
     * @param email sign-in email
     * @param username chosen display name
     * @param imageURL profile image
     * @param description user description
     * @param champion true if user is a Mental Health Champion
     * @param status online/offline
     */
    public User(String id, String email, String phone, String username, String imageURL, String description, boolean champion, String status) {
        this.id = id;
        this.email = email;
        this.phone = phone;
        this.username = username;
        this.imageURL = imageURL;
        this.description = description;
        this.champion = champion;
        this.status = status;
    }

    public User() {
    }

    public String getId() {
        return id;
    }

    public void setId(String uid) {
        id = uid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
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
