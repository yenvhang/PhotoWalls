package com.example.eggache.photowalls;

import android.app.Application;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.example.eggache.photowalls.Model.ImageModel;
import com.taobao.android.dexposed.DexposedBridge;

import java.util.List;

/**
 * Created by eggache on 2016/5/14.
 */
public class MyApplication extends Application {
    private static MyApplication app;

    @Override
    public void onCreate() {
        super.onCreate();
        app=this;
        mRequestQueue =Volley.newRequestQueue(app);
        if(DexposedBridge.canDexposed(this)){

        }
    }

    public static MyApplication getInstace(){
        return app;

    }
    private  static RequestQueue mRequestQueue ;

    public  static RequestQueue getmRequestQueue(){
        return mRequestQueue;
    }

    private  List<ImageModel> imageModels;

    public  List<ImageModel> getImageModels() {
        return imageModels;
    }
    public void setImageModels(List<ImageModel> imageModels){
        this.imageModels =imageModels;
    }
}
