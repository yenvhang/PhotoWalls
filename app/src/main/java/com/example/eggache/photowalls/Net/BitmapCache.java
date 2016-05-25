package com.example.eggache.photowalls.Net;


import android.graphics.Bitmap;
import android.util.LruCache;

import com.android.volley.toolbox.ImageLoader;

/**
 * Created by eggache on 2016/5/20.
 */
public class BitmapCache implements ImageLoader.ImageCache {

    private final LruCache<String, Bitmap> mLruCache;

    public BitmapCache(){
        int maxSize = (int) (Runtime.getRuntime().maxMemory()/10);
        mLruCache =new LruCache<String,Bitmap>(maxSize){
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getByteCount();
            }
        };
    }
    @Override
    public Bitmap getBitmap(String url) {
        return mLruCache.get(url);
    }

    @Override
    public void putBitmap(String url, Bitmap bitmap) {
        mLruCache.put(url,bitmap);
    }
}
