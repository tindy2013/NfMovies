package com.futuretech.nfmovies.Entity

import com.futuretech.nfmovies.API.Site

import java.io.Serializable

class Movie : Serializable {
    var img: String? = null
    var name: String? = null
    var url: String? = null
    var description: String? = null
    var year: String? = null
    var type: String? = null
    var episodes: List<Episodes>? = null
    var site: Site? = null


    constructor()

    constructor(name: String, img: String, url: String, site: Site) {
        this.img = img
        this.name = name.trim { it <= ' ' }
        this.url = url
        this.site = site
        this.description = ""
        this.year = ""
        this.type = ""
    }
}
