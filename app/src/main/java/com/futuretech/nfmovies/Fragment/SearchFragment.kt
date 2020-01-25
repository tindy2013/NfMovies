package com.futuretech.nfmovies.Fragment


import android.os.AsyncTask
import android.os.Bundle
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.futuretech.nfmovies.API.DDRK
import com.futuretech.nfmovies.API.Duboku
import com.futuretech.nfmovies.API.NfMovies
import com.futuretech.nfmovies.API.Tuanzhang
import com.futuretech.nfmovies.Adapter.CategoryRecyclerViewAdapter
import com.futuretech.nfmovies.Entity.Category
import com.futuretech.nfmovies.NFMoviesApplication
import com.futuretech.nfmovies.R
import me.zhanghai.android.materialprogressbar.MaterialProgressBar

import java.util.ArrayList


/**
 * A simple [Fragment] subclass.
 * Use the [SearchFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SearchFragment : BaseFragment() {

    private var searchView: SearchView? = null
    private var resultRv: RecyclerView? = null
    private var searchTask: SearchTask? = null
    private var progressBar: MaterialProgressBar? = null
    private var swipe: SwipeRefreshLayout? = null
    private var str: String? = null
    private var adapter: CategoryRecyclerViewAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_search, container, false)
        searchView = rootView.findViewById(R.id.search_view)
        val toolbar = rootView.findViewById<Toolbar>(R.id.search_toolbar)
        resultRv = rootView.findViewById(R.id.result_rv)
        progressBar = rootView.findViewById(R.id.progressbar)
        swipe = rootView.findViewById(R.id.search_swipe_refresh)

        swipe!!.setOnRefreshListener {
            swipe!!.isRefreshing = false
            if (checkStatus() && str != null) {
                searchTask = SearchTask()
                searchTask!!.execute(str)
            }
        }

        toolbar.setOnClickListener { searchView!!.onActionViewExpanded() }


        searchView!!.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                if (query == getString(R.string.live_keyword)) {
                    val app = activity!!.application as NFMoviesApplication
                    app.isAuth = true
                    return true
                }
                if (checkStatus()) {
                    searchView!!.onActionViewCollapsed()
                    Toast.makeText(context, getString(R.string.searching) + query, Toast.LENGTH_SHORT).show()
                    str = query
                    searchTask = SearchTask()
                    searchTask!!.execute(query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                return false
            }
        })

        return rootView
    }

    private fun checkStatus(): Boolean {
        if (searchTask == null) return true
        if (searchTask!!.status == AsyncTask.Status.FINISHED) return true
        return if (str == null && str!!.isEmpty()) false else false
    }

    override fun onSupportVisible() {
        showSoftInput(searchView)
        searchView!!.onActionViewExpanded()
    }

    private inner class SearchTask : AsyncTask<String, String, List<Category>>() {

        override fun doInBackground(vararg strings: String): List<Category> {
            val categories = ArrayList<Category>()
            val nf = NfMovies.instance.search(strings[0])
            if (nf.movies.isNotEmpty()) categories.add(nf)
            val tz = Tuanzhang.instance.search(strings[0])
            if (tz.movies.isNotEmpty()) categories.add(tz)
            val dd = DDRK.instance.search(strings[0])
            if (dd.movies.isNotEmpty()) categories.add(dd)
            val db = Duboku.instance.search(strings[0])
            if (db.movies.isNotEmpty()) categories.add(db)
            return categories
        }

        override fun onPreExecute() {
            progressBar!!.visibility = View.VISIBLE
        }

        override fun onPostExecute(categories: List<Category>) {
            progressBar!!.visibility = View.INVISIBLE
            if (categories.isEmpty()) return
            if (adapter == null) {
                adapter = CategoryRecyclerViewAdapter(context!!, categories, this@SearchFragment, null)
                resultRv!!.adapter = adapter
                val manager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
                resultRv!!.layoutManager = manager
            } else {
                adapter!!.setCategories(categories)
                adapter!!.notifyDataSetChanged()
            }

        }
    }

    companion object {


        fun newInstance(): SearchFragment {
            return SearchFragment()
        }
    }

}// Required empty public constructor
