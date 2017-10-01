package com.memeticame.memeticame.models;

/**
 * Created by ESTEBANFML on 01-10-2017.
 */

public class Invitation {

    private String author;
    private String message;
    private Long timestamp;

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}
