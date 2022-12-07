package com.example.styleplt.models;

import com.example.styleplt.utility.FirebaseID;
import com.google.firebase.firestore.FieldValue;

public class Save {

    private String documentId;
    private String contents;
    private String image;
    private String nickname;
    private String timestamp;
    private String time;

    public Save() {
    }

    public Save(String documentId, String contents, String image, String nickname, String timestamp, String time) {
        this.documentId = documentId;
        this.contents = contents;
        this.image = image;
        this.nickname = nickname;
        this.timestamp = timestamp;
        this.time = time;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getContents() {
        return contents;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return "Save{" +
                "documentId='" + documentId + '\'' +
                ", contents='" + contents + '\'' +
                ", image='" + image + '\'' +
                ", nickname='" + nickname + '\'' +
                ", timestamp='" + timestamp + '\'' +
                ", time='" + time + '\'' +
                '}';
    }
}

