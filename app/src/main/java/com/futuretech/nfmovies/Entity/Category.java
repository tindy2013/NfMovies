package com.futuretech.nfmovies.Entity;

import java.util.List;

public class Category {
    private String title;
    private List<Movie> movies;
    private String url;

    public Category() {
    }

    public Category(String title, List<Movie> movies, String url) {
        this.title = title.trim();
        this.movies = movies;
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<Movie> getMovies() {
        return movies;
    }

    public void setMovies(List<Movie> movies) {
        this.movies = movies;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
