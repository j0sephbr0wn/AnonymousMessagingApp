package com.brownjs.anonymousmessagingapp.model;

public class Chat {

    private String initiator;
    private String respondent;
    private String subject;

    public Chat(String initiator, String respondent, String subject) {
        this.initiator = initiator;
        this.respondent = respondent;
        this.subject = subject;
    }

    public Chat() {
    }

    public String getInitiator() {
        return initiator;
    }

    public void setInitiator(String initiator) {
        this.initiator = initiator;
    }

    public String getRespondent() {
        return respondent;
    }

    public void setRespondent(String respondent) {
        this.respondent = respondent;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }
}
