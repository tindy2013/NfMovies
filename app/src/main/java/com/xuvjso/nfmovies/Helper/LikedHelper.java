package com.xuvjso.nfmovies.Helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.strictmode.SqliteObjectLeakedViolation;
import com.xuvjso.nfmovies.API.Site;
import com.xuvjso.nfmovies.Contract.LikedContract.Liked;
import com.xuvjso.nfmovies.Entity.Movie;

import java.util.ArrayList;
import java.util.List;

public class LikedHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "data.db";

    private static final String SQL_CREATE_TABLE =
            "CREATE TABLE " + Liked.TABLE_NAME + " (" +
                    Liked._ID + " INTEGER PRIMARY KEY," +
                    Liked.COLUMN_NAME_NAME + " TEXT," +
                    Liked.COLUMN_NAME_URL + " TEXT," +
                    Liked.COLUMN_NAME_IMAGE + " TEXT," +
                    Liked.COLUMN_NAME_SITE + " INTEGER)";

    private static final String SQL_DELETE_TABLE =
            "DROP TABLE IF EXISTS " + Liked.TABLE_NAME;

    public LikedHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_TABLE);
        onCreate(db);
    }

    public long delete(SQLiteDatabase db, Movie m) {
        String selection = Liked.COLUMN_NAME_URL + " = ?";
        String[] selectionArgs = { m.getUrl() };
        return db.delete(Liked.TABLE_NAME, selection, selectionArgs);
    }

    public long add(SQLiteDatabase db, Movie m) {
        ContentValues values = new ContentValues();
        values.put(Liked.COLUMN_NAME_NAME, m.getName());
        values.put(Liked.COLUMN_NAME_IMAGE, m.getImg());
        values.put(Liked.COLUMN_NAME_SITE, m.getSite().ordinal());
        values.put(Liked.COLUMN_NAME_URL, m.getUrl());
        return db.insert(Liked.TABLE_NAME, null, values);
    }

    public List<Movie> queryAll(SQLiteDatabase db) {
        String[] projection = {
                Liked.COLUMN_NAME_NAME,
                Liked.COLUMN_NAME_IMAGE,
                Liked.COLUMN_NAME_URL,
                Liked.COLUMN_NAME_SITE
        };
        Cursor cursor = db.query(Liked.TABLE_NAME, projection, null, null, null, null, null);
        List<Movie> movies = new ArrayList<>();
        while (cursor.moveToNext()) {
            String name = cursor.getString(cursor.getColumnIndex(Liked.COLUMN_NAME_NAME));
            String img = cursor.getString(cursor.getColumnIndex(Liked.COLUMN_NAME_IMAGE));
            String url = cursor.getString(cursor.getColumnIndex(Liked.COLUMN_NAME_URL));
            int type = cursor.getInt(cursor.getColumnIndex(Liked.COLUMN_NAME_SITE));
            Site site = Site.values()[type];
            Movie movie = new Movie(name, img, url);
            movie.setSite(site);
            movies.add(movie);
        }

        return movies;
    }


    public Movie query(SQLiteDatabase db, Movie m) {
        String[] projection = {
                Liked.COLUMN_NAME_NAME,
                Liked.COLUMN_NAME_IMAGE,
                Liked.COLUMN_NAME_URL,
                Liked.COLUMN_NAME_SITE
        };
        String selection = Liked.COLUMN_NAME_URL + " = ?";
        String[] selectionArgs = { m.getUrl() };
        String sortOrder = Liked._ID + " DESC";
        Cursor cursor = db.query(
                Liked.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
        if (cursor.getCount() == 0) return null;
        cursor.moveToFirst();
        String name = cursor.getString(cursor.getColumnIndex(Liked.COLUMN_NAME_NAME));
        String img = cursor.getString(cursor.getColumnIndex(Liked.COLUMN_NAME_IMAGE));
        String url = cursor.getString(cursor.getColumnIndex(Liked.COLUMN_NAME_URL));
        int type = cursor.getInt(cursor.getColumnIndex(Liked.COLUMN_NAME_SITE));
        Site site = Site.values()[type];
        Movie movie = new Movie(name, img, url);
        movie.setSite(site);
        cursor.close();
        return movie;
    }
}
