package com.memeticame.memeticame.models;

/**
 * Created by ESTEBANFML on 01-10-2017.
 */

public class Invitation {

    private String uid;
    private String ChatRoomUuid;
    private String authorMail;
    private String chatWith;
    private String message;
    private Long timestamp;


    public String getChatRoomUuid() {
        return ChatRoomUuid;
    }

    public void setChatRoomUuid(String chatRoomUuid) {
        ChatRoomUuid = chatRoomUuid;
    }

    public String getChatWith() {
        return chatWith;
    }

    public void setChatWith(String chatWith) {
        this.chatWith = chatWith;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getAuthorMail() {
        return authorMail;
    }

    public void setAuthorMail(String authorMail) {
        this.authorMail = authorMail;
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
