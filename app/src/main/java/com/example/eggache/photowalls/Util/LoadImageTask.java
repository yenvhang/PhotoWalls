package com.example.eggache.photowalls.Util;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.ImageView;

import com.example.eggache.photowalls.Model.Photo;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by eggache on 2016/5/11.
 */
public class LoadImageTask extends AsyncTask<String, Void, Bitmap> {
    //记录 正在加载图片或等待加载图片的task

    private String mImageUrl;
    private ImageLoader imageLoader;
    private ImageView imageview;
    private Context context;
    int width;
    public LoadImageTask(Context context,String imgUrl, ImageView imageview,int width){
        this.mImageUrl = imgUrl;
        this.imageview =imageview;
        this.context=context;
        imageLoader =ImageLoader.getInstance(context);
        this.width =width;
    }

    @Override
    protected Bitmap doInBackground(String... params) {
        Bitmap bitmap ;
        bitmap =downloadImage(mImageUrl);



        return bitmap;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        if(bitmap!=null){

            imageview.setImageBitmap(bitmap);

        }
        Photo.taskCollection.remove(this);
        super.onPostExecute(bitmap);
    }
    public Bitmap downloadImage(String mImageUrl) {
        Bitmap bitmap = null;
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            Log.d("TAG", "monted sdcard");
        } else {
            Log.d("TAG", "has no sdcard");
        }
        HttpURLConnection con = null;
        FileOutputStream fos = null;
        BufferedOutputStream bos = null;
        BufferedInputStream bis = null;
        File imageFile = null;
        try {
            URL url = new URL(mImageUrl);
            con = (HttpURLConnection) url.openConnection();
            con.setConnectTimeout(5 * 1000);
            con.setReadTimeout(15 * 1000);
            con.setDoInput(true);
            con.setDoOutput(true);
            bis = new BufferedInputStream(con.getInputStream());

            imageFile = (imageLoader.getDiskCacheDir(context, imageLoader.hashKeyForDisk(mImageUrl)));
            fos = new FileOutputStream(imageFile);
            bos = new BufferedOutputStream(fos);
            byte[] b = new byte[1024];
            int length;
            while ((length = bis.read(b)) != -1) {
                bos.write(b, 0, length);
                bos.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (bis != null) {
                    bis.close();
                }
                if (bos != null) {
                    bos.close();
                }
                if (con != null) {
                    con.disconnect();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (imageFile != null) {
            bitmap = ImageLoader.decodeSampledBitmapFromResource(
                    imageFile.getPath(),width);
            if (bitmap != null) {
                imageLoader.addBitmapToMemoryCache(mImageUrl, bitmap);
            }
        }
            return bitmap;

    }

}

