package com.example.eggache.photowalls.Util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;
import android.util.LruCache;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 对图片进行管理的工具类。
 *
 * @author Tony
 */
public class ImageLoader {

    /**
     * 图片缓存技术的核心类，用于缓存所有下载好的图片，在程序内存达到设定值时会将最少最近使用的图片移除掉。
     */
    private static LruCache<String, Bitmap> mMemoryCache;
    private static LruCache<String,Integer> mDetailMemoryCache;
    /**
     * ImageLoader的实例。
     */
    private static ImageLoader mImageLoader;
    private static Context context;
    private ImageLoader() {
        // 获取应用程序最大可用内存
        int maxMemory = (int) Runtime.getRuntime().maxMemory();
        int cacheSize = maxMemory / 8;
        int detailCacheSize = maxMemory / 8;
        // 设置图片缓存大小为程序最大可用内存的1/8
        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getByteCount();
            }
        };

        mDetailMemoryCache = new LruCache<String, Integer>(detailCacheSize);
    }
    /**
     * 获取ImageLoader的实例。
     *
     * @return ImageLoader的实例。
     */
    public static ImageLoader getInstance(Context context) {
        if (mImageLoader == null) {
            mImageLoader = new ImageLoader();
            ImageLoader.context=context;
        }
        return mImageLoader;
    }

    /**
     * 将一张图片存储到LruCache中。
     *
     * @param key
     *            LruCache的键，这里传入图片的URL地址。
     * @param bitmap
     *            LruCache的键，这里传入从网络上下载的Bitmap对象。
     */
    public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemoryCache(key) == null) {
            mMemoryCache.put(key, bitmap);
        }
    }

    public void addDetailToMemoryCache(String key,Integer value){

            mDetailMemoryCache.put(key,value);

    }

    /**
     * 从LruCache中获取一张图片，如果不存在就返回null。
     *
     * @param key
     *            LruCache的键，这里传入图片的URL地址。
     * @return 对应传入键的Bitmap对象，或者null。
     */
    public Bitmap getBitmapFromMemoryCache(String key) {
        return mMemoryCache.get(key);
    }
    public Integer getDetailFromMemoryCache(String key) {
        return mDetailMemoryCache.get(key);
    }
    public static int getInSampleSizeFromStream(InputStream ins,int reqWidth){
        final BitmapFactory.Options option = new BitmapFactory.Options();
        option.inJustDecodeBounds=true;
        BitmapFactory.decodeStream(ins,null,option);
        option.inSampleSize = calculateInSampleSize(option, reqWidth);

        return option.outHeight/option.inSampleSize>=1?option.outHeight/option.inSampleSize:option.outHeight;
    }
    public static int calculateInSampleSize(BitmapFactory.Options options,
                                            int reqWidth) {
        // 源图片的宽度
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (width > reqWidth) {
            // 计算出实际宽度和目标宽度的比率
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = widthRatio;
        }
        return inSampleSize;
    }
    public static Bitmap decodeSmallBitmapFromStream(InputStream ins,int reqWidth,int height){

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds=false;
        options.outHeight=height;
        options.outWidth=reqWidth;
        return BitmapFactory.decodeStream(ins,null,options);

    }
    public static Bitmap decodeSampledBitmapFromResource(String pathName,
                                                         int reqWidth) {
        // 第一次解析将inJustDecodeBounds设置为true，来获取图片大小
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(pathName, options);
        // 调用上面定义的方法计算inSampleSize值
        options.inSampleSize = calculateInSampleSize(options, reqWidth);
        // 使用获取到的inSampleSize值再次解析图片
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(pathName, options);
    }

    //从网络加载图片
    public static Bitmap loadBitmapFromUrl(String url){

        return null;
    }

    public String getImageFilePath(String mImageUrl) {

        int lastSlashIndex = mImageUrl.lastIndexOf("/");
        String imageName = mImageUrl.substring(lastSlashIndex + 1);
        imageName=hashKeyForDisk(imageName);
        File imageFile =new File(Environment.getExternalStorageDirectory().getPath()+ "/PhotoWallFalls/");
        if(!imageFile.exists()){
            imageFile.mkdirs();
        }

        return imageFile.getPath()+File.separator+imageName;

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
                imageFile =(getDiskCacheDir(context,hashKeyForDisk(mImageUrl)));
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
                        imageFile.getPath(), 0);
                if (bitmap != null) {
                    mImageLoader.addBitmapToMemoryCache(mImageUrl, bitmap);
                }
            }
        return bitmap;
    }

    public String hashKeyForDisk(String key) {
        String cacheKey;
        try {

            final MessageDigest mDigest = MessageDigest.getInstance("MD5");
            mDigest.update(key.getBytes());
            cacheKey = bytesToHexString(mDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            cacheKey = String.valueOf(key.hashCode());
        }
        return cacheKey.toLowerCase();
    }

    private String bytesToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }

    public File getDiskCacheDir(Context context, String uniqueName) {
        String cachePath;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            cachePath = context.getExternalCacheDir().getPath();
        } else {
            cachePath = context.getCacheDir().getPath();
        }
        return new File(cachePath + File.separator + uniqueName);
    }


}
