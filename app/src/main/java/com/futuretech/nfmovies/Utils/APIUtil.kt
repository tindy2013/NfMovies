package com.futuretech.nfmovies.Utils

import com.futuretech.nfmovies.API.*

object APIUtil {
    fun getClient(site: Site): ISite? {
        return when (site) {
            Site.NFMOVIES -> NfMovies.instance
            Site.TUANZHANG -> Tuanzhang.instance
            Site.DDRK -> DDRK.instance
            Site.DUBOKU -> Duboku.instance
        }
    }

    fun getClient(name: String): ISite? {
        when (name) {
            NfMovies.NAME -> return NfMovies.instance
            Tuanzhang.NAME -> return Tuanzhang.instance
            DDRK.NAME -> return DDRK.instance
            Duboku.NAME -> return Duboku.instance
        }
        return null
    }
}
