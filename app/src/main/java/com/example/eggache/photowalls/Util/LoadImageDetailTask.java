package com.example.eggache.photowalls.Util;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

import com.example.eggache.photowalls.Model.Photo;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by eggache on 2016/5/12.
 */
public class LoadImageDetailTask extends AsyncTask<String, Void, Integer> {
    ImageView imageview;
    int reqWidth;
    private static ImageLoader imageLoader;
    public LoadImageDetailTask(Context context,ImageView imageview, int reqWidth){
        this.imageview =imageview;
        this.reqWidth =reqWidth;
        imageLoader =ImageLoader.getInstance(context);
    }
    @Override
    protected Integer doInBackground(String... params) {
        String mImageUrl =params[0];

        HttpURLConnection con =null;
        try {
            URL url = new URL(mImageUrl);
            con = (HttpURLConnection) url.openConnection();
            con.setConnectTimeout(5 * 1000);
            con.setReadTimeout(15 * 1000);
            con.setDoInput(true);

            InputStream ins =con.getInputStream();
            BitmapFactory.Options options =new BitmapFactory.Options();
            options.inJustDecodeBounds=true;
            BitmapFactory.decodeStream(ins,null,options);

            int samplesize =ImageLoader.calculateInSampleSize(options,reqWidth);
            int height =options.outHeight/samplesize>=1?options.outHeight/samplesize:options.outHeight;
            imageLoader.addDetailToMemoryCache(mImageUrl+"width",reqWidth);
            imageLoader.addDetailToMemoryCache(mImageUrl+"height",height);
            return height;


        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    protected void onPostExecute(Integer integer) {
        Photo.setImageFram(imageview,reqWidth,integer);
    }
}
