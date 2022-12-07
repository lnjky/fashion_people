package com.example.styleplt.models;

import android.provider.ContactsContract;

import com.google.firebase.firestore.ServerTimestamp;

public class board {

    private String documentId;
    private String title;
    private String contents;
    private String nickname;
    private String collectionId;
    private String timestamp;
    @ServerTimestamp
    private ContactsContract.Data data;

    //alt + insert
    // 빈 생성자를 만드는 이유 = 만들지 않으면 데이터가 나오지 않음

    public board() {
    }

    public board(String documentId, String title, String contents, String nickname, String collectionId, String timestamp) {
        this.documentId = documentId;
        this.title = title;
        this.contents = contents;
        this.nickname = nickname;
        this.collectionId = collectionId;
        this.timestamp = timestamp;
        this.data = data;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContents() {
        return contents;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getCollectionId() {
        return collectionId;
    }

    public void setCollectionId(String collectionId) {
        this.collectionId = collectionId;
    }

    public ContactsContract.Data getData() {
        return data;
    }

    public void setData(ContactsContract.Data data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "board{" +
                "documentId='" + documentId + '\'' +
                ", title='" + title + '\'' +
                ", contents='" + contents + '\'' +
                ", nickname='" + nickname + '\'' +
                ", collectionId='" + collectionId + '\'' +
                ", timestamp='" + timestamp + '\'' +
                ", data=" + data +
                '}';
    }
}
