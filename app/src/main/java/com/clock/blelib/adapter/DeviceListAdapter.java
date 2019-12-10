/*
 * Copyright (C), 2019. by [clock - Individual developer] ,All rights reserved.
 * The article is original, writing is also the strength to live, reference please indicate the source and author.
 */

package com.clock.blelib.adapter;

import android.content.DialogInterface;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.clock.blelib.MainActivity;
import com.clock.blelib.R;
import com.clock.blelib.bleissus.blemodel.BLEManager;
import com.clock.blelib.bleissus.blemodel.heart.HeaterDevice;
import com.clock.blelib.bleissus.blemodel.led.LedDevice;
import com.clock.blelib.util.WidgetUitl;
import com.clock.bluetoothlib.logic.network.connection.BLEAppDevice;
import com.clock.bluetoothlib.logic.network.connection.BLEState;

import java.util.ArrayList;
import java.util.List;

public class DeviceListAdapter extends RecyclerView.Adapter<DeviceListAdapter.VH> {

    private final String TAG = "DeviceListAdapter";
    private MainActivity mMain3Activity;
    private List<BLEAppDevice> mDatas;
    private OnItemClickListener mOnItemClickListener = null;

    public DeviceListAdapter() {
    }
    public DeviceListAdapter(MainActivity activity, List<BLEAppDevice> data) {
        mMain3Activity = activity;
        mDatas = data;
    }

    public void update() {
        notifyDataSetChanged();
    }

    //在Adapter中实现3个方法
    @Override
    public int getItemCount() {
        return mDatas.size();
    }

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        if (parent == null) {
            Log.e(TAG, "parent null");
            return null;
        }
        View v;
        v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_device_list_device, parent, false);

        return new VH(v);
    }

    @Override
    public void onBindViewHolder(final VH holder, final int position) {
        Log.v(TAG, "position: " + position + mDatas.get(position));

        String name = "default";
        if(mDatas.get(position).mDeviceTypeId == HeaterDevice.DEVICE_TYPE_ID) {
            name = mDatas.get(position).mBleName + " " + "--Heater";
        }
        else if(mDatas.get(position).mDeviceTypeId == LedDevice.DEVICE_TYPE_ID) {
            name = mDatas.get(position).mBleName + " " + "--Led";
        }
        holder.tv_name.setText(name);

        if (mDatas.get(position).mBLEConnState == BLEState.Connect.Active) {
            holder.btn_conn.setText("断开");
        }
        else {
            holder.btn_conn.setText("连接");
        }

        holder.btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                byte[] value = new byte[]{(byte) 0xAA, 0x0d, 0x07, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};//
                BLEManager.getInstance().sendBLETransmitData(value, mDatas.get(position).mDeviceId);
            }
        });

        holder.btn_conn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDatas.get(position).mBLEConnState == BLEState.Connect.Active) {
                    manualDisConnDevice(position);
                }
                else {
                    manualConnDevice(position);
                }
            }
        });

        holder.btn_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteDialog(position);
            }
        });

    }

    public void setItemClickListener(OnItemClickListener lis) {
        mOnItemClickListener = lis;
    }


    private void showDeleteDialog(final int pos) {

        WidgetUitl.showLiteCommDialog(mMain3Activity,
                "delete device", "Sure to delete this device?", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                BLEManager.getInstance().removeDevice(mDatas.get(pos).mDeviceId);
                mDatas.remove(pos);
                notifyDataSetChanged();
            }
        });
    }

    private void manualConnDevice(final int position) {
        mMain3Activity.mHandlerList.removeCallbacksAndMessages(null);
        mMain3Activity.mHandlerList.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.e(TAG, "延迟200后，尝试去手动重连：" + mDatas.get(position).mBleAddress);
                int ret;
                if (mDatas.get(position).isVirtualDevice == true) {
                    List<String> temp = new ArrayList();
                    temp.add(mDatas.get(position).mBleAddress);
                    ret = BLEManager.getInstance().autoScanConnVirtualDevice(temp);
                }
                else {
                    ret = BLEManager.getInstance().manualConnDevice(mDatas.get(position).mDeviceId);
                }

                if (ret == -3) {
                    Log.w(TAG, "Other device releasing ,please wait");
                    mMain3Activity.showToastNoUiBase("ble system is releasing ,please wait!");
                    return;
                }
                if (ret == -20) {
                    Log.w(TAG, "device is releasing ,please wait!");
                    mMain3Activity.showToastNoUiBase("device is releasing ,please wait!");
//                                mMain3Activity.mCurrDevice.mBLEConnState = BLEState.Connect.Active;
//                                notifyDataSetChanged();
                    return;
                }
                if (ret == -30) {
                    Log.w(TAG, "device is connecting ,please wait!");
                    mMain3Activity.showToastNoUiBase("device is connecting ,please wait!");
                    return;
                }
                if (ret == -10) {
                    Log.w(TAG, "device is Invalid ,please wait!");
                    mMain3Activity.showToastNoUiBase("device error ,please retry!");
                    return;
                }

                if (ret != 0) {
                    Log.w(TAG, "Other device is connecting ,please wait!");
                    mMain3Activity.showToastNoUiBase("Other device is connecting ,please wait!");
                    return;
                }
                mMain3Activity.showProgressBase(true);
            }
        }, 200);
    }

    private void manualDisConnDevice(final int position) {
        WidgetUitl.showLiteCommDialog(mMain3Activity,
                "disconnect device", "Sure to disconnect this device?", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int ret;
                        ret = BLEManager.getInstance().forceDisConnDevice(mDatas.get(position).mDeviceId);
                        if (ret == -3) {
                            Log.w(TAG, "Other device releasing ,please wait");
                            mMain3Activity.showToastNoUiBase("ble system is releasing ,please wait!");
                            return;
                        }
                        if (ret == -20) {
                            Log.w(TAG, "Other device is releasing ,please wait!");
                            mMain3Activity.showToastNoUiBase("device is releasing ,please wait!");
                            return;
                        }
                        if (ret == -30) {
                            Log.w(TAG, "Other device is connecting ,please wait!");
                            mMain3Activity.showToastNoUiBase("device is connecting ,please wait!");
                            return;
                        }
                        if (ret == -10) {
                            Log.w(TAG, "device is Invalid ,please wait!");
                            mMain3Activity.showToastNoUiBase("device error ,please retry!");
                            return;
                        }

                        if (ret != 0) {
                            Log.w(TAG, "Other device is connecting ,please wait!");
                            mMain3Activity.showToastNoUiBase("Other device is connecting ,please wait!");
                            return;
                        }
                        notifyDataSetChanged();
                    }
                });
    }


    static class VH extends RecyclerView.ViewHolder{
        private final TextView tv_name;
        private final Button btn_send;
        private final Button btn_conn;
        private final Button btn_delete;

        private VH(View v) {
            super(v);
            tv_name = v.findViewById(R.id.tv_name);
            btn_send = v.findViewById(R.id.btn_send);
            btn_conn = v.findViewById(R.id.btn_conn);
            btn_delete = v.findViewById(R.id.btn_delete);
        }
    }
}
