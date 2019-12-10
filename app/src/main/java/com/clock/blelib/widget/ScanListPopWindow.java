/*
 * Copyright (C), 2019. by [clock - Individual developer] ,All rights reserved.
 * The article is original, writing is also the strength to live, reference please indicate the source and author.
 */

package com.clock.blelib.widget;

import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import com.clock.blelib.R;
import com.clock.blelib.adapter.OnItemClickListener;
import com.clock.blelib.adapter.ScanListAdapter;
import com.clock.blelib.util.SystemUtil;

import java.util.List;

public class ScanListPopWindow extends PopupWindow implements OnItemClickListener {

    private final String TAG = "ScanListPopWindow";

    private View mMenuView;
    private Context mContext;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView mRecyclerView;
    private ScanListAdapter scanListAdapter;

    public int selectItem = -1;

    public ScanListPopWindow(Context context, View.OnClickListener itemsOnClick) {
        super(context);
        mContext = context;

        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mMenuView = inflater.inflate(R.layout.popwindow_scan_list, null);

        setAdapter();
        initData();

        this.setContentView(mMenuView);
        this.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        this.setHeight(SystemUtil.dip2px(mContext,200));
        this.setFocusable(true);
//        this.setAnimationStyle(R.style.popwin_anim_style);
    }

    private void setAdapter() {
        mRecyclerView = mMenuView.findViewById(R.id.rcview_scan_list);
        mLayoutManager = new LinearLayoutManager(mContext);
        mRecyclerView.setLayoutManager(mLayoutManager);
        scanListAdapter = new ScanListAdapter();
        mRecyclerView.setAdapter(scanListAdapter);
        scanListAdapter.setItemClickListener(this);
    }

    private void initData() {
        selectItem = -1;
    }

    @Override
    public void dismiss() {
        super.dismiss();
    }

    public void updateScanList(List<BluetoothDevice> ds) {
        scanListAdapter.update(ds);
    }

    @Override
    public void onItemClick(int position) {
        Log.d(TAG, "onItemClick: " + position);
        selectItem = position;
        dismiss();
    }

    @Override
    public void onItemLongClick(int position) {
        commonDialog(position);
    }

    public void commonDialog(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("警告");
        builder.setMessage("你确定删除此定时吗？");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d(TAG, "点击了确定");
//                deleteTimer(position);
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d(TAG,"点击了取消");
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}
