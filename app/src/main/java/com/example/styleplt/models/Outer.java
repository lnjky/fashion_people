package com.example.styleplt.models;

public class Outer {

    private String image;
    private String url;

    public Outer() {
    }

    public Outer(String image, String url) {
        this.image = image;
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    @Override
    public String toString() {
        return "Outer{" +
                "image='" + image + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}
