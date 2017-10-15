package com.memeticame.memeticame.models;

import java.util.HashMap;

/**
 * Created by ESTEBANFML on 11-10-2017.
 */

public class ChatRoom {


    private String groupName;
    private long createdAt;
    private String creator;
    private String members;

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getMembers() {
        return members;
    }

    public void setMembers(String members) {
        this.members = members;
    }

}
