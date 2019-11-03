package com.xuvjso.nfmovies.UI;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.*;
import android.widget.PopupWindow;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.xuvjso.nfmovies.Adapter.PopupMenuAdapter;
import com.xuvjso.nfmovies.Listener.PopupMenuItemClickListener;
import com.xuvjso.nfmovies.R;

import java.util.List;

public class PopupMenu extends PopupWindow {
    private ImageButton closeButton;
    private RecyclerView menuRv;
    private PopupMenuItemClickListener clickListener;
    private Context context;

    public PopupMenu(Context context, View contentView, List<String> items, PopupMenuItemClickListener clickListener) {
        super(contentView, WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT, true);
        closeButton = contentView.findViewById(R.id.menu_close_button);
        menuRv = contentView.findViewById(R.id.menu_recyclerview);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        PopupMenuAdapter adapter = new PopupMenuAdapter(context, items, clickListener);
        menuRv.setAdapter(adapter);
        menuRv.setLayoutManager(new LinearLayoutManager(context, RecyclerView.VERTICAL, false));
    }

    public void show(View parent) {
        showAtLocation(parent, Gravity.TOP | Gravity.RIGHT, 0,0);
    }

}
