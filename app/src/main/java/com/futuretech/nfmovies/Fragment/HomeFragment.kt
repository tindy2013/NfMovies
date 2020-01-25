package com.futuretech.nfmovies.Fragment


import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.futuretech.nfmovies.Adapter.CategoryRecyclerViewAdapter
import com.futuretech.nfmovies.Entity.Category
import com.futuretech.nfmovies.Entity.Movie
import com.futuretech.nfmovies.Listener.CategoryMoreClickListener
import com.futuretech.nfmovies.Listener.PopupMenuItemClickListener
import com.futuretech.nfmovies.UI.PopupMenu
import com.futuretech.nfmovies.R
import com.futuretech.nfmovies.Utils.APIUtil
import kotlinx.android.synthetic.main.fragment_home.*
import me.zhanghai.android.materialprogressbar.MaterialProgressBar

import java.util.ArrayList


class HomeFragment : BaseFragment(), CategoryMoreClickListener, PopupMenuItemClickListener {
    private var siteMenu: TextView? = null
    private var rootView: View? = null
    private var adapter: CategoryRecyclerViewAdapter? = null
    private var progressBar: MaterialProgressBar? = null
    private var refreshLayout: SwipeRefreshLayout? = null
    private var refreshTask: RefreshTask? = null
    private var categoryRv: RecyclerView? = null
    private var menus: MutableList<String>? = null
    private var categories: MutableList<List<Category>>? = null
    private var popupMenu: PopupMenu? = null
    private var current: Int = 0

    override fun onSupportInvisible() {
        if (refreshTask != null) refreshTask!!.cancel(true)
        progressBar!!.visibility = View.INVISIBLE
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.fragment_home, container, false)
        current = 0
        initView()
        return rootView
    }

    override fun onLazyInitView(savedInstanceState: Bundle?) {
        super.onLazyInitView(savedInstanceState)
        show(0)
    }

    private fun initView() {
        menus = ArrayList()
        menus!!.add(getString(R.string.nfmovies))
        menus!!.add(getString(R.string.apkgm))
        menus!!.add(getString(R.string.ddrk))
        menus!!.add(getString(R.string.duboku))
        categories = ArrayList()
        for (i in 0..3) {
            categories!!.add(ArrayList())
        }

        siteMenu = rootView!!.findViewById(R.id.toolbar_menu)
        siteMenu!!.visibility = View.VISIBLE
        progressBar = rootView!!.findViewById(R.id.progressbar)

        categoryRv = rootView!!.findViewById(R.id.content_rv)

        siteMenu!!.text = menus!![current]
        siteMenu!!.setOnClickListener { v -> popupMenu!!.show(v) }

        adapter = CategoryRecyclerViewAdapter(context!!, this, this)
        categoryRv!!.adapter = adapter
        val manager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        categoryRv!!.layoutManager = manager


        val menuView = LayoutInflater.from(context).inflate(R.layout.popup_menu, null, false)
        popupMenu = PopupMenu(context!!, menuView, menus!!, this)

        refreshLayout = rootView!!.findViewById(R.id.home_swipe_refresh)
        refreshLayout!!.setOnRefreshListener {
            refreshLayout!!.isRefreshing = false
            newTask(current)
        }
    }

    private fun checkStatus(): Boolean {
        if (refreshTask == null) return true
        return refreshTask!!.status == AsyncTask.Status.FINISHED
    }

    private fun show(position: Int) {
        if (categories!!.size == 0) {
            newTask(position)
            return
        }

        if (position < categories!!.size && categories!![position] != ArrayList<Movie>()) {
            adapter!!.setCategories(categories!![position])
            adapter!!.notifyDataSetChanged()
        }

        if (categories!![position].isEmpty()) {
            newTask(position)
        }

        siteMenu!!.text = menus!![position]
        current = position

    }

    private fun newTask(position: Int) {
        if (checkStatus()) {
            refreshTask = RefreshTask()
            refreshTask!!.execute(TaskType(position))
        }
        return
    }

    override fun onMoreClick(url: String) {

    }

    override fun onPopupMenuItemClick(position: Int) {
        if (position != current) {
            if (refreshTask != null) refreshTask!!.cancel(true)
            siteMenu!!.text = menus!![position]
            refreshTask = null
        }
        popupMenu!!.dismiss()
        show(position)
    }

    private inner class RefreshTask : AsyncTask<TaskType, String, TaskType>() {
        override fun onPreExecute() {
            progressBar!!.visibility = View.VISIBLE
        }

        override fun onPostExecute(taskType: TaskType) {
            progressBar!!.visibility = View.INVISIBLE
            if (taskType.categories.isEmpty()) {
                Toast.makeText(context, R.string.load_failed, Toast.LENGTH_SHORT).show()
                return
            }
            categories!![taskType.position] = taskType.categories
            adapter!!.setCategories(taskType.categories)
            adapter!!.notifyDataSetChanged()
        }

        override fun onCancelled() {
            progressBar!!.visibility = View.INVISIBLE
            super.onCancelled()
        }

        override fun onCancelled(taskType: TaskType) {
            if (taskType.categories.isEmpty()) categories!![taskType.position] = taskType.categories
            progressBar!!.visibility = View.INVISIBLE
        }

        override fun doInBackground(vararg taskTypes: TaskType): TaskType {
            var cs: List<Category> = ArrayList()
            try {
                cs = APIUtil.getClient(menus!![taskTypes[0].position])!!.categories
            } catch (e: Exception) {
                e.printStackTrace()
            }

            taskTypes[0].categories = cs
            return taskTypes[0]
        }
    }

    private inner class TaskType(var position: Int) {
        var categories: List<Category>

        init {
            this.categories = ArrayList()
        }
    }

    override fun onBackPressedSupport(): Boolean {
        if (refreshTask != null && refreshTask!!.status != AsyncTask.Status.FINISHED) {
            refreshTask!!.cancel(true)
            Toast.makeText(context, R.string.load_cancelling, Toast.LENGTH_SHORT).show()
            return true
        }

        return false

    }

    companion object {

        fun newInstance(): HomeFragment {
            val fragment = HomeFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }
}
