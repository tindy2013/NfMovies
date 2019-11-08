package com.xuvjso.nfmovies.API;

import com.xuvjso.nfmovies.Entity.Category;
import com.xuvjso.nfmovies.Entity.Episode;
import com.xuvjso.nfmovies.Entity.Movie;
import org.json.JSONException;

import java.util.List;

public interface ISite {

    List<Category> getCategories() throws Exception;
    Site getSite();
    Movie getMovieDetail(Movie movie) throws Exception;
    String getPlayURL(Episode e) throws Exception;
    Category search(String str) throws Exception;
    String getName();
}
