package com.xuvjso.nfmovies.Contract;

import android.provider.BaseColumns;

public final class LikedContract {
    private LikedContract() {}

    public static class Liked implements BaseColumns {
        public static final String TABLE_NAME = "liked";
        public static final String COLUMN_NAME_NAME = "name";
        public static final String COLUMN_NAME_URL = "url";
        public static final String COLUMN_NAME_IMAGE = "image";
        public static final String COLUMN_NAME_SITE = "site";
    }
}
