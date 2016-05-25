package com.example.eggache.photowalls.Util;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;

import com.example.eggache.photowalls.Activity.LoginActivity;
import com.example.eggache.photowalls.MyApplication;

import me.drakeet.materialdialog.MaterialDialog;

/**
 * Created by eggache on 2016/5/24.
 */
public class LoginManager {
    private final static LoginManager loginManager =new LoginManager();
    private SharedPreferences sharedPreferences;

    private LoginManager(){};
    public static LoginManager getInstance(){return loginManager;}
    private String defaultInfo =null;
    public String getDefaultInfo(){return defaultInfo;}
    public  String getLoginInfo(){
        Context context =MyApplication.getInstace();
        sharedPreferences =context.getSharedPreferences(LoginActivity.CODE_LOGIN_USER,
                context.MODE_PRIVATE);


        if(sharedPreferences!=null) {
            String username = sharedPreferences.getString("username", "未登录");
            defaultInfo =username;
            if(sharedPreferences.getAll().size()==0)
                return null;
            return username;
        }
        return null;
    }

    public void login(final Context context ){
    try {
            final MaterialDialog materialDialog = new MaterialDialog(context);
            materialDialog.setTitle("登录").setMessage("请先登录").setCanceledOnTouchOutside(false)
                    .setPositiveButton("确定", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(context, LoginActivity.class);
                            context.startActivity(intent);
                            materialDialog.dismiss();
                        }
                    }).setNegativeButton("取消", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    materialDialog.dismiss();
                }
            }).show();
        }
        catch(Exception e){

            Log.e("LoginManager",Log.getStackTraceString(e));
        }
    }


}
