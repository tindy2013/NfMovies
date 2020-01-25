package com.futuretech.nfmovies.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckedTextView
import androidx.recyclerview.widget.RecyclerView
import com.futuretech.nfmovies.Listener.PopupMenuItemClickListener
import com.futuretech.nfmovies.R

class PopupMenuAdapter(private val mContext: Context, private val mItems: List<String>, private val mListener: PopupMenuItemClickListener) : RecyclerView.Adapter<PopupMenuAdapter.ViewHolder>() {
    private var mCurrentItem = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.popup_menu_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.item.text = mItems[position]
        when(position)
        {
            0 -> {setMargins(holder.item, 0, 500, 0, 0)}
            mItems.size - 1 -> {setMargins(holder.item, 0, 0, 0, 800)}
            else -> {setMargins(holder.item, 0, 0, 0, 0)}
        }
        holder.item.isChecked = position == mCurrentItem
    }

    override fun getItemCount(): Int {
        return mItems.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val item: CheckedTextView

        init {
            item = itemView.findViewById(R.id.menu_item)
            item.setOnClickListener {
                val cur = mCurrentItem
                mCurrentItem = adapterPosition
                notifyItemChanged(cur)
                notifyItemChanged(mCurrentItem)
                mListener.onPopupMenuItemClick(mCurrentItem)
            }
        }
    }

    companion object {

        fun setMargins(v: View, l: Int, t: Int, r: Int, b: Int) {
            if (v.layoutParams is ViewGroup.MarginLayoutParams) {
                val p = v.layoutParams as ViewGroup.MarginLayoutParams
                p.setMargins(l, t, r, b)
                v.requestLayout()
            }
        }
    }
}
