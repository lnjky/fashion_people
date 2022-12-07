package com.example.styleplt.models;

import android.provider.ContactsContract;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class Comments {

    private String documentID;
    private String nickname;
    private String comments;
    private String time;
    private String collectionID;
    @ServerTimestamp
    private ContactsContract.Data data;

    // Alt + Insert Consturctor
    public Comments() {
    }

    public Comments(String documentID, String nickname, String comments, String time, String collectionID) {
        this.documentID = documentID;
        this.nickname = nickname;
        this.comments = comments;
        this.time = time;
        this.collectionID = collectionID;
    }

    public String getDocumentID() {
        return documentID;
    }

    public void setDocumentID(String documentID) {
        this.documentID = documentID;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getCollectionID() {
        return collectionID;
    }

    public void setCollectionID(String collectionID) {
        this.collectionID = collectionID;
    }

    @Override
    public String toString() {
        return "Comments{" +
                "documentID='" + documentID + '\'' +
                ", nickname='" + nickname + '\'' +
                ", comments='" + comments + '\'' +
                ", time='" + time + '\'' +
                ", collectionID='" + collectionID + '\'' +
                '}';
    }
}
