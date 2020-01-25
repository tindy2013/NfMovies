package com.futuretech.nfmovies.API

import com.futuretech.nfmovies.Entity.Category
import com.futuretech.nfmovies.Entity.Episode
import com.futuretech.nfmovies.Entity.Movie

interface ISite {

    val categories: List<Category>
    val site: Site
    val name: String
    val host: String
    @Throws(Exception::class)
    fun getMovieDetail(movie: Movie): Movie

    @Throws(Exception::class)
    fun getPlayURL(e: Episode): String

    @Throws(Exception::class)
    fun search(str: String): Category
}
