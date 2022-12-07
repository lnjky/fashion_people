package com.example.styleplt.models;

public class Bottom {

    private String image;
    private String url;

    public Bottom() {
    }

    public Bottom(String image, String url) {
        this.image = image;
        this.url = url;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return "Bottom{" +
                "image='" + image + '\'' +
                ", url='" + url + '\'' +
                '}';
    }




}
