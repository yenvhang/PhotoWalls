package com.example.eggache.photowalls;

import android.app.Application;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.example.eggache.photowalls.Model.ImageModel;
import com.example.eggache.photowalls.Net.BitmapCache;
import com.example.eggache.photowalls.Net.ImageCacheUtil;
import com.example.eggache.photowalls.Util.CrashHandler;

import java.util.List;

/**
 * Created by eggache on 2016/5/14.
 */
public class MyApplication extends Application {
    private static MyApplication app;
    private static ImageLoader imageLoader;
    private static ImageLoader.ImageCache imageCache;
    private  String photo;
    @Override
    public void onCreate() {
        super.onCreate();
        app=this;
        mRequestQueue =Volley.newRequestQueue(app);
        imageCache =new BitmapCache();
        imageLoader = new ImageLoader(mRequestQueue,new ImageCacheUtil(app));

        //

        CrashHandler handler = CrashHandler.getInstance();
        handler.init(getApplicationContext());


    }

    public static ImageLoader.ImageCache getImageCache(){
        return imageCache;
    }
    public void setPhoto(String photo){
        this.photo =photo;
    }

    public String getPhoto(){
        return photo;
    }

    public static MyApplication getInstace(){
        return app;

    }
    private  static RequestQueue mRequestQueue ;

    public  static RequestQueue getmRequestQueue(){
        return mRequestQueue;
    }
    public static ImageLoader getImageLoader(){return imageLoader;}
    private  List<ImageModel> imageModels;

    public  List<ImageModel> getImageModels() {
        return imageModels;
    }
    public void setImageModels(List<ImageModel> imageModels){
        this.imageModels =imageModels;
    }
}
