package com.example.styleplt.models;

public class Top {

    private String image;
    private String url;

    public Top() {
    }

    public Top(String image, String url) {
        this.image = image;
        this.url = url;
    }

    @Override
    public String toString() {
        return "Top{" +
                "image='" + image + '\'' +
                ", url='" + url + '\'' +
                '}';
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



}
