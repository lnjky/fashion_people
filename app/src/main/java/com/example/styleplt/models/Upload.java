package com.example.styleplt.models;

public class Upload {

    private String documentId;
    private String contents;
    private String nickname;
    private String image;
    private String collectionId;
    private String rating;
    private String timestamp;
    private String url;

    public Upload() {
    }

    public Upload(String documentId, String contents, String nickname, String image, String collectionId, String rating, String timestamp, String url) {
        this.documentId = documentId;
        this.contents = contents;
        this.nickname = nickname;
        this.image = image;
        this.collectionId = collectionId;
        this.rating = rating;
        this.timestamp = timestamp;
        this.url = url;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
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

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }


    public String getCollectionId() {
        return collectionId;
    }

    public void setCollectionId(String collectionId) {
        this.collectionId = collectionId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return "Upload{" +
                "documentId='" + documentId + '\'' +
                ", contents='" + contents + '\'' +
                ", nickname='" + nickname + '\'' +
                ", image='" + image + '\'' +
                ", collectionId='" + collectionId + '\'' +
                ", rating='" + rating + '\'' +
                ", timestamp='" + timestamp + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}
