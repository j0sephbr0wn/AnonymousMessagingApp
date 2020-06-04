package com.brownjs.anonymousmessagingapp.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Chat {

    private String id;
    private String initiator;
    private String champion;
    private String champion_display_name;
    private String subject;
    private String latest_message;
    private String latest_message_time;
    private String latest_messager;
    private boolean read;

    public Chat(String id, String initiator, String champion, String champion_display_name, String subject,
                String latest_message, String latest_message_time, String latest_messager, boolean read) {
        this.id = id;
        this.initiator = initiator;
        this.champion = champion;
        this.champion_display_name = champion_display_name;
        this.subject = subject;
        this.latest_message = latest_message;
        this.latest_message_time = latest_message_time;
        this.latest_messager = latest_messager;
        this.read = read;
    }

    public Chat() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getInitiator() {
        return initiator;
    }

    public void setInitiator(String initiator) {
        this.initiator = initiator;
    }

    public String getChampion() {
        return champion;
    }

    public void setChampion(String champion) {
        this.champion = champion;
    }

    public String getChampion_Display_Name() {
        return champion_display_name;
    }

    public String getChampionDisplayName() {
        return champion_display_name;
    }

    public void setChampionDisplayName(String championDisplayName) {
        this.champion_display_name = championDisplayName;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getLatest_Message() {
        return latest_message;
    }

    public String getLatestMessage() {
        return latest_message;
    }

    public void setLatestMessage(String latestMessage) {
        this.latest_message = latestMessage;
    }

    public String getLatest_Message_Time() {
        return latest_message_time;
    }

    public Date getLatestMessageTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.UK);
        try {
            return sdf.parse(latest_message_time);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return new Date(System.currentTimeMillis());
    }

    public void setLatestMessageTime(String latestMessageTime) {
        this.latest_message_time = latestMessageTime;
    }

    public String getLatestMessager() {
        return latest_messager;
    }

    public String getLatest_messager() {
        return latest_messager;
    }

    public void setLatest_messager(String latest_messager) {
        this.latest_messager = latest_messager;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    //    public String getUnread() {
//        return unread;
//    }
//
//    public void setUnread(String unread) {
//        this.unread = unread;
//    }
}
