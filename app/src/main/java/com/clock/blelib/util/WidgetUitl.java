/*
 * Copyright (C), 2019. by [clock - Individual developer] ,All rights reserved.
 * The article is original, writing is also the strength to live, reference please indicate the source and author.
 */

package com.clock.blelib.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.widget.EditText;
import android.widget.TextView;

import com.clock.blelib.R;


public class WidgetUitl {

    public static void showLiteEditDialog(Activity activity, CharSequence title, EditText edit, final DialogInterface.OnClickListener listener) {
        final AlertDialog.Builder editDialog = new AlertDialog.Builder(activity);
        editDialog.setTitle("修改");
        editDialog.setIcon(R.mipmap.ic_launcher);

        edit.setSingleLine(true);
        //设置dialog布局
        editDialog.setView(edit);

        //设置按钮
        editDialog.setPositiveButton("确定", listener);

        editDialog.create().show();
    }

    public static void showLiteTextDialog(Activity activity, CharSequence title, TextView text, CharSequence textContent,
                                          final DialogInterface.OnClickListener listener) {
        final AlertDialog.Builder editDialog = new AlertDialog.Builder(activity);
        editDialog.setTitle(title);
        editDialog.setIcon(R.mipmap.ic_launcher);

        text.setSingleLine(true);
        text.setText(textContent);
        text.setPadding(30 ,20, 0, 0);
        //设置dialog布局
        editDialog.setView(text);

        //设置按钮
        editDialog.setPositiveButton("确定", listener);

        editDialog.create().show();
    }

    public static void showLiteCommDialog(Activity activity, CharSequence title, CharSequence textContent,
                                          final DialogInterface.OnClickListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(title);
        builder.setMessage(textContent);
        builder.setPositiveButton("确定", listener);
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public static void showLiteCommDialog2(Activity activity, CharSequence title, CharSequence textContent,
                                          final DialogInterface.OnClickListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(title);
        builder.setMessage(textContent);
        builder.setPositiveButton("确定", listener);
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
//                Log.d(TAG,"点击了取消");
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}
