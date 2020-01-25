package com.futuretech.nfmovies.Helper

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

import com.futuretech.nfmovies.API.Site
import com.futuretech.nfmovies.Contract.LikedContract.Liked
import com.futuretech.nfmovies.Entity.Movie

import java.util.ArrayList

class LikedHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_CREATE_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL(SQL_DELETE_TABLE)
        onCreate(db)
    }

    fun delete(db: SQLiteDatabase, m: Movie): Long {
        val selection = Liked.COLUMN_NAME_URL + " = ?"
        val selectionArgs = arrayOf(m.url)
        return db.delete(Liked.TABLE_NAME, selection, selectionArgs).toLong()
    }

    fun add(db: SQLiteDatabase, m: Movie): Long {
        val values = ContentValues()
        values.put(Liked.COLUMN_NAME_NAME, m.name)
        values.put(Liked.COLUMN_NAME_IMAGE, m.img)
        values.put(Liked.COLUMN_NAME_SITE, m.site?.ordinal)
        values.put(Liked.COLUMN_NAME_URL, m.url)
        return db.insert(Liked.TABLE_NAME,
                null, values)
    }

    fun queryAll(db: SQLiteDatabase?): List<Movie> {
        val projection = arrayOf(Liked.COLUMN_NAME_NAME, Liked.COLUMN_NAME_IMAGE, Liked.COLUMN_NAME_URL, Liked.COLUMN_NAME_SITE)
        val cursor = db!!.query(Liked.TABLE_NAME, projection, null, null, null, null, null)
        val movies = ArrayList<Movie>()
        while (cursor.moveToNext()) {
            val name = cursor.getString(cursor.getColumnIndex(Liked.COLUMN_NAME_NAME))
            val img = cursor.getString(cursor.getColumnIndex(Liked.COLUMN_NAME_IMAGE))
            val url = cursor.getString(cursor.getColumnIndex(Liked.COLUMN_NAME_URL))
            val type = cursor.getInt(cursor.getColumnIndex(Liked.COLUMN_NAME_SITE))
            val site = Site.values()[type]
            val movie = Movie(name, img, url, site)
            movies.add(movie)
        }
        cursor.close()

        return movies
    }


    fun query(db: SQLiteDatabase, m: Movie): Movie? {
        if(m.url == null || m.url!!.isEmpty())
            return null
        val projection = arrayOf(Liked.COLUMN_NAME_NAME, Liked.COLUMN_NAME_IMAGE, Liked.COLUMN_NAME_URL, Liked.COLUMN_NAME_SITE)
        val selection = Liked.COLUMN_NAME_URL + " = ?"
        val selectionArgs = arrayOf(m.url)
        val sortOrder = Liked._ID + " DESC"
        val cursor = db.query(
                Liked.TABLE_NAME,
                projection,
                selection,
                selectionArgs, null, null,
                sortOrder
        )
        if (cursor.count == 0) return null
        cursor.moveToFirst()
        val name = cursor.getString(cursor.getColumnIndex(Liked.COLUMN_NAME_NAME))
        val img = cursor.getString(cursor.getColumnIndex(Liked.COLUMN_NAME_IMAGE))
        val url = cursor.getString(cursor.getColumnIndex(Liked.COLUMN_NAME_URL))
        val type = cursor.getInt(cursor.getColumnIndex(Liked.COLUMN_NAME_SITE))
        val site = Site.values()[type]
        val movie = Movie(name, img, url, site)
        cursor.close()
        return movie
    }

    companion object {
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "data.db"

        private const val SQL_CREATE_TABLE = "CREATE TABLE " + Liked.TABLE_NAME + " (" +
                Liked._ID + " INTEGER PRIMARY KEY," +
                Liked.COLUMN_NAME_NAME + " TEXT," +
                Liked.COLUMN_NAME_URL + " TEXT," +
                Liked.COLUMN_NAME_IMAGE + " TEXT," +
                Liked.COLUMN_NAME_SITE + " INTEGER)"

        private const val SQL_DELETE_TABLE = "DROP TABLE IF EXISTS " + Liked.TABLE_NAME
    }
}
