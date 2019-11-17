package com.futuretech.nfmovies.Entity;

import java.io.Serializable;

public class Episode implements Serializable {
    private String name;
    private String url;
    private String caption;

    public Episode(String name, String url) {
        this.name = name;
        this.url = url;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public Episode() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
