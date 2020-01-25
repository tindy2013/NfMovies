package com.futuretech.nfmovies.Entity

class Category {
    var title: String = ""
    var movies: List<Movie> = ArrayList()
    var url: String = ""

    constructor()

    constructor(title: String, movies: List<Movie>, url: String) {
        this.title = title.trim { it <= ' ' }
        this.movies = movies
        this.url = url
    }
}
