package com.example.eggache.photowalls.Activity;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.WindowManager;

public class LoginReciever extends BroadcastReceiver {


    @Override
    public void onReceive(final Context context, Intent intent) {
        Log.e("LoginReciever","1");
//        MaterialDialog materialDialog =new MaterialDialog(context);
//        materialDialog.setTitle("登录").setMessage("请先登录").setCanceledOnTouchOutside(false)
//                .setPositiveButton("确定", new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        Intent intent =new Intent(context,LoginActivity.class);
//                        context.startActivity(intent);
//                    }
//                }).show();
        try {
            AlertDialog.Builder dialogbuilder = new AlertDialog.Builder(context);
            dialogbuilder.setTitle("登录").setMessage("请先登录")
                    .setCancelable(false)
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(context, LoginActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            context.startActivity(intent);
                        }
                    });
            AlertDialog alertDialog = dialogbuilder.create();
            alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
            alertDialog.show();
        }
        catch (Exception e){
            Log.e("LoginReciever",Log.getStackTraceString(e));
        }


    }
}
