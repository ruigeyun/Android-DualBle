package com.clock.blelib;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.clock.blelib.adapter.DeviceListAdapter;
import com.clock.blelib.bleissus.blemodel.BLEManager;
import com.clock.blelib.event.AddNewDeviceEvent;
import com.clock.blelib.event.AddPreConnDeviceEvent;
import com.clock.blelib.event.AddScanDeviceEvent;
import com.clock.blelib.event.BleSendResultEvent;
import com.clock.blelib.event.ConnectDeviceEvent;
import com.clock.blelib.event.ConnectUnTypeDeviceEvent;
import com.clock.blelib.event.updateDeviceInfoEvent;
import com.clock.blelib.widget.ScanListPopWindow;
import com.clock.bluetoothlib.logic.network.connection.BLEAppDevice;
import com.clock.bluetoothlib.logic.network.connection.BLEState;
import com.clock.bluetoothlib.logic.utils.LogUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity implements View.OnClickListener {

    private static final int REQUEST_CODE = 10;
    private final int REQUEST_ENABLE_BT = 10086;
    private final String TAG = "MainActivity";

    public Handler mHandler = new Handler();
    public Handler mHandlerList = new Handler();
    private Activity mContext;

    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView mRecyclerView;
    private DeviceListAdapter mDeviceListAdapter;

    private ScanListPopWindow scanListPopWindow;
    private Button btn_scan;

    private List<BluetoothDevice> mBleScanList;
    private List<BLEAppDevice> mBleDeviceList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.w(TAG, "main onCreate");

        initData();
        setAdapter();
        initView();
        verifyPermissions(this);

        if(!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }
    private void initData() {
        mContext = this;
        mBleScanList = new ArrayList<>();
        mBleDeviceList = new ArrayList<>();
    }

    private void setAdapter() {
        mRecyclerView = findViewById(R.id.rcview_device_list);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mDeviceListAdapter = new DeviceListAdapter(this, mBleDeviceList);
        mRecyclerView.setAdapter(mDeviceListAdapter);
    }

    private void initView() {
        btn_scan = findViewById(R.id.btn_scan);
        btn_scan.setOnClickListener(this);

        scanListPopWindow = new ScanListPopWindow(getApplicationContext(), this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.btn_scan:
                Log.d(TAG, "onClick: ");
                BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                if (!bluetoothAdapter.isEnabled()) {
                    Toast.makeText(this, getString(R.string.please_open_blue), Toast.LENGTH_LONG).show();
                    return;
                }

                if (mBleDeviceList.size() >= 7) {
                    Toast.makeText(this, getString(R.string.max_num), Toast.LENGTH_LONG).show();
                    return;
                }

                mBleScanList.clear();
                BLEManager.getInstance().scanBLEDeviceManual();

                mHandler.removeCallbacksAndMessages(null);
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "扫描结束10s");
                        if (mBleScanList.size() <= 0/* && mBleDeviceList.size()<=0*/) {

                            scanListPopWindow.dismiss();
                        }
                        else {
                            showProgressBase(false);
//                            scanListPopWindow.dismiss();
                        }
                    }
                }, 10000);

                showProgressBase(true);
                showScanListPopView();
                break;

            default:
                break;
        }
    }

    private void showScanListPopView() {
        scanListPopWindow.dismiss();
        scanListPopWindow.setFocusable(true);
        scanListPopWindow.selectItem = -1;
        scanListPopWindow.showAtLocation(MainActivity.this.findViewById(R.id.main_root),
                Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL, 0, 0);
        bgAlpha((float) 0.618);
        scanListPopWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                bgAlpha((float) 1);
                if (scanListPopWindow.selectItem > -1) {
                    showProgressBase(true);
                    BluetoothDevice entry = mBleScanList.get(scanListPopWindow.selectItem);
                    BLEManager.getInstance().connScannedBleDevice(entry);
                }

                mHandler.removeCallbacksAndMessages(null);
                mBleScanList.clear();
                scanListPopWindow.updateScanList(mBleScanList); // pop windows 只是隐藏，列表内容还在
            }
        });
    }

    private void bgAlpha(float f){
        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        layoutParams.alpha = f;
        getWindow().setAttributes(layoutParams);
    }


    private void preInit() {
        BluetoothManager bluetoothManager = (BluetoothManager) MainActivity.this.getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = null;
        if (bluetoothManager != null) {
            bluetoothAdapter = bluetoothManager.getAdapter();
        }
        else {
            Log.w(TAG, "init: bluetoothManager null");
            exitApp();
        }

        if (bluetoothAdapter != null) {
            if (!bluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
            else {
                Log.w(TAG, "start initBle ...");
                BLEManager.getInstance().initBle(getApplicationContext());
            }
        }
    }


    private void verifyPermissions(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            List<String> permissionList = new ArrayList<>();
            //检测是否有写的权限
            int permission = ActivityCompat.checkSelfPermission(activity,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (permission != PackageManager.PERMISSION_GRANTED) {
                permissionList.add(Manifest.permission.READ_EXTERNAL_STORAGE);
                permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
            //检测是否蓝牙的权限
            permission = ContextCompat.checkSelfPermission(activity,
                    Manifest.permission.ACCESS_COARSE_LOCATION);
            if (permission != PackageManager.PERMISSION_GRANTED) {
                permissionList.add(Manifest.permission.ACCESS_COARSE_LOCATION);
            }
            if (permissionList.size() == 0) {
                preInit();
                return;
            }
            String[] permissions = permissionList.toArray(new String[permissionList.size()]);//将List转为数组
            Log.d("test", "权限请求数组len ：" + permissions.length);
            ActivityCompat.requestPermissions(activity, permissions, REQUEST_CODE);
        } else {
            preInit();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        Log.d("test", "权限应答数组len ：" + permissions.length + "  grantResults len ：" + grantResults.length);
        //用户允许改权限，0表示允许，-1表示拒绝 PERMISSION_GRANTED = 0， PERMISSION_DENIED = -1
        if (requestCode == REQUEST_CODE) {
            Log.d("test", "权限请求反馈 REQUEST_CODE");
            if (grantResults.length <= 0) { // 用户不给权限，用不了app
                Log.d("test", "用户不给权限，用不了app ");
                exitApp();
                return;
            }
            for (int i = 0; i < grantResults.length; i++) {
                Log.d("test", "i: " + i + " permissions: " +  permissions[i ] + " grantResults： " + grantResults[i]);
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    Log.d("test", "动态权限没打开: " + i);
                    exitApp();
                    return;
                }
            }
            preInit();
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i(TAG, "requestCode: " + requestCode + " resultCode: " + resultCode);
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                Log.d(TAG, "打开蓝牙成功！");
                Log.w(TAG, "start initBle ...");
                BLEManager.getInstance().initBle(getApplicationContext());
            }
            else if (resultCode == RESULT_CANCELED) {
                Log.d(TAG, "放弃打开蓝牙！");
                exitApp();
            }
        }
    }

    private void exitApp() {
        finish();
        android.os.Process.killProcess(android.os.Process.myPid()); // 获取PID
        System.exit(0);
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(AddScanDeviceEvent event) {
        Log.d(TAG, "onMessageEvent: " + event.toString());
        for (BLEAppDevice d: mBleDeviceList) {
            if (d.mBleAddress.equals(event.getBluetoothDevice().getAddress())) {
                return;
            }
        }
        mBleScanList.add(event.getBluetoothDevice());
        scanListPopWindow.updateScanList(mBleScanList);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(AddPreConnDeviceEvent event) {

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(AddNewDeviceEvent event) {
        Log.d(TAG, "AddDeviceListEven: " + event.toString());
        for (BLEAppDevice d : mBleDeviceList) {
            if (d.mDeviceId == event.device.mDeviceId) {
                Log.w(TAG, "设备添加重复：" + d.mBleAddress + " " + d.mBleName);
                return;
            }
        }

        // 新连接的设备，则添加进去
        if (!mBleDeviceList.contains(event.device)) {
            mBleDeviceList.add(event.device);
            LogUtil.i(TAG, "新连接的设备，添加到列表");
        }

        if (!BLEManager.getInstance().hasDeviceConnecting()) {
            showProgressBase(false);
        }

        mDeviceListAdapter.update();

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(updateDeviceInfoEvent event) {
        Log.d(TAG, "onMessageEvent updateDeviceListEven: ");
        mDeviceListAdapter.update();
        showProgressBase(false);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(ConnectDeviceEvent event) {
        Log.d(TAG, "onMessageEvent ConnectDeviceEven: ");
        if (event.type == 0) { // 连接失败 0
            Log.d(TAG, "ReconnectType " + event.device.mReconnectType);
            if (event.device.mReconnectType == BLEState.ReconnectType.Manual) {

            } else { // 当前设备自动重连 失败

            }

            showProgressBase(false);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(ConnectUnTypeDeviceEvent event) {
        Log.d(TAG, "onMessageEvent ConnectUnTypeDeviceEven: ");
        if (event.type < 0) { // 连接失败 0
            Log.w(TAG, "连接失败 0 ConnectUnTypeDeviceEven: ");
            showProgressBase(false);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(BleSendResultEvent event) {
        Log.w(TAG, "BleSendResultEven : " + event.result);
        showProgressBase(false);
    }

}
