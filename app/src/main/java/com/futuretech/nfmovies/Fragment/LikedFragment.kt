package com.futuretech.nfmovies.Fragment


import android.database.sqlite.SQLiteDatabase
import android.os.AsyncTask
import android.os.Bundle
import android.widget.GridView

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.futuretech.nfmovies.Adapter.MovieGridViewAdapter
import com.futuretech.nfmovies.Entity.Movie
import com.futuretech.nfmovies.Helper.LikedHelper
import com.futuretech.nfmovies.R
import me.zhanghai.android.materialprogressbar.MaterialProgressBar

import java.util.ArrayList


class LikedFragment : BaseFragment() {

    private var likedGridView: GridView? = null
    private var likedHelper: LikedHelper? = null
    private var db: SQLiteDatabase? = null
    private var progressBar: MaterialProgressBar? = null
    private var swipeRefreshLayout: SwipeRefreshLayout? = null
    private var task: Task? = null
    private var adapter: MovieGridViewAdapter? = null
    private var queryTask: QueryTask? = null
    private var movies: List<Movie>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_liked, container, false)
        likedGridView = view.findViewById(R.id.liked_grid)
        progressBar = view.findViewById(R.id.progressbar)
        swipeRefreshLayout = view.findViewById(R.id.liked_swipe_refresh)
        swipeRefreshLayout!!.setOnRefreshListener {
            swipeRefreshLayout!!.isRefreshing = false
            if (queryTask == null || queryTask!!.status == AsyncTask.Status.FINISHED) {
                queryTask = QueryTask()
                queryTask!!.execute(task)
            }
        }
        return view
    }

    override fun onSupportVisible() {
        if (likedHelper == null) likedHelper = LikedHelper(context!!)
        if (db == null) db = likedHelper!!.readableDatabase
        if (task == null) task = Task(likedHelper, db)
        if (movies == null) movies = ArrayList()
        if (adapter == null) adapter = MovieGridViewAdapter(context!!, movies!!, this)

        queryTask = QueryTask()
        queryTask!!.execute(task)
    }

    override fun onDestroy() {
        if (likedHelper != null) likedHelper!!.close()
        super.onDestroy()
    }

    private inner class Task(var helper: LikedHelper?, var db: SQLiteDatabase?)

    private inner class QueryTask : AsyncTask<Task, String, List<Movie>>() {

        override fun onPreExecute() {
            progressBar!!.visibility = View.VISIBLE
        }

        override fun doInBackground(vararg tasks: Task): List<Movie> {
            val task = tasks[0]
            return task.helper!!.queryAll(task.db)
        }

        override fun onPostExecute(m: List<Movie>) {
            progressBar!!.visibility = View.INVISIBLE
            if (movies == null) return
            movies = ArrayList(m)
            adapter = MovieGridViewAdapter(context!!, movies!!, this@LikedFragment)
            likedGridView!!.adapter = adapter
            adapter!!.notifyDataSetChanged()
        }
    }

    companion object {


        fun newInstance(): LikedFragment {
            val fragment = LikedFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }
}// Required empty public constructor
