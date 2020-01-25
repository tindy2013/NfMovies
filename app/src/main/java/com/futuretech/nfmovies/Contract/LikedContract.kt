package com.futuretech.nfmovies.Contract

import android.provider.BaseColumns

object LikedContract {

    class Liked : BaseColumns {
        companion object {
            const val TABLE_NAME = "liked"
            const val COLUMN_NAME_NAME = "name"
            const val COLUMN_NAME_URL = "url"
            const val COLUMN_NAME_IMAGE = "image"
            const val COLUMN_NAME_SITE = "site"
            const val _ID = "id"
        }
    }
}
