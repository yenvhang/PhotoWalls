package com.example.eggache.photowalls.Util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by eggache on 2016/5/20.
 */
public class SourcePath {
    public static String getTheCameraPhotoPath()  {


        Date date =new Date(System.currentTimeMillis());
        SimpleDateFormat dateFormat = new SimpleDateFormat("'IMG'_yyyyMMdd_HHmmss");
        return dateFormat.format(date)+".jpg";
//        File temp = new File(FilePath);
//        if (!temp.exists()) {
//            temp.createNewFile();
//
//        }
//        return temp;

    }
}
