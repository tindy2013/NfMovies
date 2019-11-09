package com.xuvjso.nfmovies.Entity;

import com.xuvjso.nfmovies.API.Site;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class Movie implements Serializable {
    private String img;
    private String name;
    private String url;
    private String description;
    private String year;
    private String type;
    private List<Episodes> episodes;
    private Site site;
    public Site getSite() {
        return site;
    }

    public void setSite(Site site) {
        this.site = site;
    }


    public Movie() {
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Movie(String name, String img, String url, Site site) {
        this.img = img;
        this.name = name.trim();
        this.url = url;
        this.site = site;
        this.description = "";
        this.year = "";
        this.type = "";
    }
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
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

    public List<Episodes> getEpisodes() {
        return episodes;
    }

    public void setEpisodes(List<Episodes> episodes) {
        this.episodes = episodes;
    }
}
