package com.futuretech.nfmovies.UI

import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.*
import android.widget.PopupWindow
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.futuretech.nfmovies.Adapter.PopupMenuAdapter
import com.futuretech.nfmovies.Listener.PopupMenuItemClickListener
import com.futuretech.nfmovies.R

class PopupMenu(context: Context, contentView: View, items: List<String>, clickListener: PopupMenuItemClickListener) : PopupWindow(contentView, WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT, true) {
    private val closeButton: ImageButton
    private val menuRv: RecyclerView
    private val clickListener: PopupMenuItemClickListener? = null
    private val context: Context? = null

    init {
        closeButton = contentView.findViewById(R.id.menu_close_button)
        menuRv = contentView.findViewById(R.id.menu_recyclerview)
        closeButton.setOnClickListener { dismiss() }
        val adapter = PopupMenuAdapter(context, items, clickListener)
        menuRv.adapter = adapter
        menuRv.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        animationStyle = R.style.popup_animation
    }

    fun show(parent: View) {
        showAtLocation(parent, Gravity.TOP or Gravity.END, 0, 0)
    }

}
