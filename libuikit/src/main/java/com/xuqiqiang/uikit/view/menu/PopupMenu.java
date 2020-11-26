package com.xuqiqiang.uikit.view.menu;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.xuqiqiang.uikit.R;
import com.xuqiqiang.uikit.utils.ArrayUtils;
import com.xuqiqiang.uikit.utils.DisplayUtils;
import com.xuqiqiang.uikit.view.listener.OnItemClickListener;
import com.xuqiqiang.uikit.view.popup.CommonPopupWindow;

import java.util.List;

/**
 * Created by xuqiqiang on 2019/12/06.
 */
public class PopupMenu implements CommonPopupWindow.ViewInterface {

    private final Activity mContext;
    private CommonPopupWindow popupWindow;
    private List<MenuItem> mMenuList;
    private OnItemClickListener mListener;

    public PopupMenu(Activity context) {
        mContext = context;
    }

    public PopupMenu init(List<MenuItem> list, OnItemClickListener listener) {
        this.mMenuList = list;
        this.mListener = listener;
        return this;
    }

    public boolean show() {
        if (ArrayUtils.isEmpty(mMenuList) || popupWindow != null && popupWindow.isShowing())
            return false;
        popupWindow = new CommonPopupWindow.Builder(mContext)
                .setView(R.layout.popup_list)
//                .setWidthAndHeight(ViewGroup.LayoutParams.MATCH_PARENT, upView.getMeasuredHeight())
                .setWidthAndHeight(ViewGroup.LayoutParams.MATCH_PARENT,
                        (int) DisplayUtils.dip2px(73 + mMenuList.size() * 55))
                .setBackGroundLevel(Color.BLACK, 0.5f)
                .setAnimationStyle(R.style.AnimUp)
                .setViewOnclickListener(this)
                .create();
        // Fix: BadTokenException: Unable to add window -- token null is not valid
        try {
            popupWindow.showAtLocation(mContext.findViewById(android.R.id.content), Gravity.BOTTOM, 0, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    public boolean show(List<MenuItem> list, OnItemClickListener listener) {
        this.init(list, listener);
        return this.show();
    }

    @Override
    public void getChildView(View view, int layoutResId) {

        RecyclerView recyclerView = view.findViewById(R.id.rv_content);

        LinearLayoutManager manager = new LinearLayoutManager(mContext);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(manager);

        recyclerView.setAdapter(new MenuAdapter());

        View btnCancel = view.findViewById(R.id.bt_cancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (popupWindow != null) {
                    popupWindow.dismiss();
                }
            }
        });
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mListener = listener;
    }

    public static class MenuItem {
        int image;
        String name;

        public MenuItem(String name) {
            this.name = name;
        }

        public MenuItem(String name, int image) {
            this.name = name;
            this.image = image;
        }
    }

    public class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.ViewHolder> {

        @NonNull
        @Override
        public MenuAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            int layoutId = R.layout.popup_item;
            View view = LayoutInflater.from(mContext).inflate(layoutId, parent, false);
            return new MenuAdapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MenuAdapter.ViewHolder holder, int position) {
            holder.tvName.setText(mMenuList.get(position).name);
            if (mMenuList.get(position).image != 0) {
                Drawable drawable = mContext.getResources().getDrawable(mMenuList.get(position).image);
                drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
                holder.tvName.setCompoundDrawables(drawable,
                        null, null, null);
//                holder.tvName.setCompoundDrawablePadding();
            } else {
                holder.tvName.setCompoundDrawables(null,
                        null, null, null);
            }
            if (position == mMenuList.size() - 1) {
                holder.divider.setVisibility(View.GONE);
            }
            holder.itemView.setTag(position);
        }

        @Override
        public int getItemCount() {
            return mMenuList.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            TextView tvName;
            View divider;

            ViewHolder(final View view) {
                super(view);
                tvName = view.findViewById(R.id.tv_name);
                divider = view.findViewById(R.id.divider);
                view.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                if (popupWindow != null) {
                    popupWindow.dismiss();
                }
                if (mListener != null) {
                    mListener.onItemClick(itemView, (Integer) itemView.getTag());
                }
            }
        }
    }
}
