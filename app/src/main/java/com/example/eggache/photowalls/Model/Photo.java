package com.example.eggache.photowalls.Model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.example.eggache.photowalls.R;
import com.example.eggache.photowalls.Source.Images;
import com.example.eggache.photowalls.Util.ImageLoader;
import com.example.eggache.photowalls.Util.LoadImageTask;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * Created by eggache on 2016/5/11.
 */
public class Photo {
    private static final String TAG ="Photo" ;
    private String description;
    private  String  imgurl;
    private static ImageLoader imageLoader;
    //记录 正在加载图片或等待加载图片的task
    public static Set<LoadImageTask> taskCollection =new HashSet<LoadImageTask>();



    public static void setImageBitmap(Context context,ImageView imageview, int position,Integer width1,Boolean isScrolling) {
        Bitmap bitmap;
        Integer width =width1 ;
        Integer height;
        imageLoader = ImageLoader.getInstance(context);
        String imgurl = Images.imageUrls[position];
        if (null != (bitmap = imageLoader.getBitmapFromMemoryCache(imgurl))) {
            imageview.setScaleType(ImageView.ScaleType.FIT_XY);
            imageview.setImageBitmap(bitmap);
            return;

//        } else if (null != (width = imageLoader.getDetailFromMemoryCache(imgurl + "width")) &&
//                null != (height = imageLoader.getDetailFromMemoryCache(imgurl + "height"))) {
//
//            Photo.setImageFram(imageview, width, height);

        }
        else if (!isScrolling) {
            Log.e(TAG,"任务"+position+"开启");
//                LoadImageDetailTask loadImageDetailTask = new LoadImageDetailTask(context, imageview, 400);
//                loadImageDetailTask.execute(imgurl);
//


            imageview.setScaleType(ImageView.ScaleType.FIT_XY);

            LoadImageTask loadImageTask = new LoadImageTask(context, imgurl, imageview,width);
            taskCollection.add(loadImageTask);
            loadImageTask.execute(imgurl);


        }

        else{
            imageview.setImageResource(R.drawable.empty_photo);

        }

    }


    private static void setEmptyPhoto(ImageView imageview) {
        Random random =new Random();
        int height =(random.nextInt(5)+1)*50;
        imageview.measure(300,height);
        imageview.setBackgroundColor(Color.GREEN);
    }

    public Photo(String description, String image) {
        this.description = description;
        this.imgurl = image;
    }

    public  static void setImageFram(ImageView imageview,int reqWidth,int height){
        LinearLayout.LayoutParams param =new LinearLayout.LayoutParams(reqWidth,height);
        imageview.setLayoutParams(param);
        imageview.setBackgroundColor(Color.GREEN);

    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageurl() {
        return imgurl;
    }

    public void setImageurl(String image) {
        this.imgurl = image;
    }
}
