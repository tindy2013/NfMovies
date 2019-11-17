package com.futuretech.nfmovies.API;

import com.futuretech.nfmovies.Entity.Category;
import com.futuretech.nfmovies.Entity.Episode;
import com.futuretech.nfmovies.Entity.Movie;

import java.util.List;

public interface ISite {

    List<Category> getCategories() throws Exception;
    Site getSite();
    Movie getMovieDetail(Movie movie) throws Exception;
    String getPlayURL(Episode e) throws Exception;
    Category search(String str) throws Exception;
    String getName();
    String getHost();
}
