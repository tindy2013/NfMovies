package com.futuretech.nfmovies.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.futuretech.nfmovies.Listener.PopupMenuItemClickListener;
import com.futuretech.nfmovies.R;

import java.util.List;

public class PopupMenuAdapter extends RecyclerView.Adapter<PopupMenuAdapter.ViewHolder> {

    private List<String> mItems;
    private Context mContext;
    private PopupMenuItemClickListener mListener;
    private int mCurrentItem = 0;

    public static void setMargins (View v, int l, int t, int r, int b) {
        if (v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            p.setMargins(l, t, r, b);
            v.requestLayout();
        }
    }


    public PopupMenuAdapter(Context context, List<String> data, PopupMenuItemClickListener listener) {
        this.mItems = data;
        this.mContext = context;
        this.mListener = listener;
    }

    @NonNull
    @Override
    public PopupMenuAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view =  LayoutInflater.from(mContext).inflate(R.layout.popup_menu_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final PopupMenuAdapter.ViewHolder holder, int position) {
        holder.item.setText(mItems.get(position));
        if (position == 0) setMargins(holder.item, 0, 500, 0, 0);
        else if (position == mItems.size() - 1) setMargins(holder.item, 0, 0, 0, 800);
        else setMargins(holder.item, 0, 0, 0, 0);
        holder.item.setChecked(position == mCurrentItem);
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private CheckedTextView item;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            item = itemView.findViewById(R.id.menu_item);
            item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int cur = mCurrentItem;
                    mCurrentItem = getAdapterPosition();
                    notifyItemChanged(cur);
                    notifyItemChanged(mCurrentItem);
                    mListener.onPopupMenuItemClick(mCurrentItem);
                }
            });
        }
    }
}
