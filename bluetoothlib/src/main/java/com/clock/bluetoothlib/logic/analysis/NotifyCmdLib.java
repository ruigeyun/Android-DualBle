/*
 * Copyright (C), 2019. by [clock - Individual developer] ,All rights reserved.
 * The article is original, writing is also the strength to live, reference please indicate the source and author.
 */

package com.clock.bluetoothlib.logic.analysis;


import android.util.Log;

import java.util.HashMap;

class NotifyCmdLib {
    private final String TAG = NotifyCmdLib.class.getSimpleName();
    private HashMap<Integer, String> cmdLib = new HashMap<Integer, String>();
    private HashMap<Integer, CmdInterface> ObjectLib = new HashMap<Integer, CmdInterface>();
    //	private Map<Integer, CmdInterface> ObjectLib = new ConcurrentHashMap<Integer, CmdInterface>(); // 没有并发情况
    private static NotifyCmdLib instance = new NotifyCmdLib();

    NotifyCmdAdapter notifyCmdAdapter;

    private NotifyCmdLib() {
    }

    public static NotifyCmdLib get() {
        return instance;
    }

    public void setAdapter(NotifyCmdAdapter adapter) {
        notifyCmdAdapter = adapter;
        notifyCmdAdapter.getNotifyCmds(cmdLib);
    }

    String getClassString(int key) {
        if (cmdLib.containsKey(key)) {
            return cmdLib.get(key);
        } else {
            //			Log.d(TAG, "getClassString null");
            return null;
        }
    }

    void putObjectToLib(int key, CmdInterface instance) {
        if (ObjectLib.containsKey(key)) {
            Log.d(TAG, "对象实例库中，已经存在该对象");
        } else {
            ObjectLib.put(key, instance);
        }
    }

    CmdInterface getObjectFromLib(int key) {
        if (ObjectLib.containsKey(key)) {
            return ObjectLib.get(key);
        } else {
            Log.d(TAG, "getObjectFromLib null");
            return null;
        }
    }

    public void removeAllParamLib(Integer param) {
        cmdLib.clear();
        ObjectLib.clear();
    }
}
