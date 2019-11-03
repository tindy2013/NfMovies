package com.xuvjso.nfmovies.API;

import com.xuvjso.nfmovies.Entity.Category;
import com.xuvjso.nfmovies.Entity.Episode;
import com.xuvjso.nfmovies.Entity.Movie;
import org.json.JSONException;

import java.util.List;

public interface ISite {

    List<Category> getCategories() throws Exception;
    Type getType();
    Movie getMovieDetail(Movie movie) throws JSONException;
    String getPlayURL(Episode e) throws Exception;
    List<Movie> getSinglePage(String url, int page);
    Category search(String str);
    String getName();
}
