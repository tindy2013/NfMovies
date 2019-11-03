package com.xuvjso.nfmovies.Entity;

import com.xuvjso.nfmovies.API.Type;

import java.util.List;
import java.util.Map;

public class Movie {
    private String img;
    private String name;
    private String url;
    private String description;
    private String year;
    private String type;
    private Type site;
    public Type getSite() {
        return site;
    }

    public void setSite(Type site) {
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

    private Map<String, List<Episode>> episodes;

    public Movie(String name, String img, String url) {
        this.img = img;
        this.name = name.trim();
        this.url = url;
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

    public Map<String, List<Episode>> getEpisodes() {
        return episodes;
    }

    public void setEpisodes(Map<String, List<Episode>> episodes) {
        this.episodes = episodes;
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
}