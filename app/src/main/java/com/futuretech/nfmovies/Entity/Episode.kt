package com.futuretech.nfmovies.Entity

import java.io.Serializable

class Episode : Serializable {
    var name: String = ""
    var url: String = ""
    var caption: String = ""

    constructor(name: String, url: String) {
        this.name = name
        this.url = url
    }

    constructor()
}
