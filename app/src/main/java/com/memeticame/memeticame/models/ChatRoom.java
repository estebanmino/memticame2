package com.memeticame.memeticame.models;

import java.util.HashMap;

/**
 * Created by ESTEBANFML on 11-10-2017.
 */

public class ChatRoom {

    private String email;
    private String phone;
    private String name;
    private String id;
    private HashMap<String, String> contacts = null;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public HashMap<String, String> getContacts() {
        return contacts;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhone(String phone) {
        this.phone = phone.replace(",","").replace(" ","");
    }
}