package com.memeticame.memeticame.models;

import android.net.Uri;

/**
 * Created by ESTEBANFML on 02-10-2017.
 */

public class Message {

    private String content;
    private String author;
    private long timestamp;
    private String multimedia = null;
    private Uri multimediaUrl = null;
    private String multimediaPath;
    private String multimediaSize;


    public String getMultimediaSize() {
        return multimediaSize;
    }

    public void setMultimediaSize(String multimediaSize) {
        this.multimediaSize = multimediaSize;
    }

    public String getMultimediaPath() {
        return multimediaPath;
    }

    public void setMultimediaPath(String multimediaPath) {
        this.multimediaPath = multimediaPath;
    }


    public Uri getMultimediaUrl() {
        return multimediaUrl;
    }

    public void setMultimediaUrl(Uri multimediaUrl) {
        this.multimediaUrl = multimediaUrl;
    }

    public String getMultimedia() {
        return multimedia;
    }

    public String getMultimediaName() {
        return  multimedia.substring(multimedia.lastIndexOf("/") + 1);
    }

    public String getMultimediaType() {
        return  multimedia.substring(0, multimedia.lastIndexOf("/"));
    }

    public void setMultimedia(String multimedia) {
        this.multimedia = multimedia;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

}
