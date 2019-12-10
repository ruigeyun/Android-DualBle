/*
 * Copyright (C), 2019. by [clock - Individual developer] ,All rights reserved.
 * The article is original, writing is also the strength to live, reference please indicate the source and author.
 */

package com.clock.blelib.adapter;

import android.bluetooth.BluetoothDevice;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.clock.blelib.R;

import java.util.ArrayList;
import java.util.List;

public class ScanListAdapter extends RecyclerView.Adapter<ScanListAdapter.VH> {

    private final String TAG = "ScanListAdapter";
    private List<BluetoothDevice> mDatas = new ArrayList<BluetoothDevice>();
    private OnItemClickListener mOnItemClickListener = null;

    public ScanListAdapter() {
    }
    public ScanListAdapter(List<BluetoothDevice> data) {
        mDatas = data;
    }

    public void update(List<BluetoothDevice> data) {
        mDatas = data;
        notifyDataSetChanged();
        Log.i(TAG, "mDatas: " + mDatas);
    }

    public void add(BluetoothDevice data) {
        mDatas.add(data);
        notifyDataSetChanged();
    }

    //在Adapter中实现3个方法
    @Override
    public int getItemCount() {
        return mDatas.size();
    }

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_scan, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(VH holder, final int position) {
        Log.v(TAG, "position: " + position + mDatas.get(position));

        holder.tv_name.setText(mDatas.get(position).getName() + "  " + mDatas.get(position).getAddress());

        holder.item_scan_root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.w(TAG, "onClick: " + position);
                if(mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(position);
                }
            }
        });

        holder.item_scan_root.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Log.w(TAG, "on long Click: " + position);
                if(mOnItemClickListener != null) {
//                    mOnItemClickListener.onItemLongClick(position);
                }
                return false;
            }
        });
    }

    public void setItemClickListener(OnItemClickListener lis) {
        mOnItemClickListener = lis;
    }

    public static class VH extends RecyclerView.ViewHolder {
        public final TextView tv_name;
        public final LinearLayout item_scan_root;

        public VH(View v) {
            super(v);
            tv_name = v.findViewById(R.id.tv_name);
            item_scan_root = v.findViewById(R.id.item_scan_root);
        }
    }
}
