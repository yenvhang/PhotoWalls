package com.example.eggache.photowalls.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.Snackbar;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.example.eggache.photowalls.R;
import com.example.eggache.photowalls.Util.PlaceHolderDrawableHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by eggache on 2016/5/14.
 */
public class ShowPhotoActivity extends ToolBarActivity {
    private String TAG="ShowPhtotActivity";
    private NetworkImageView imageView;
    private ShareActionProvider myShareActionProvider;
    private File file;
    private Intent myShareIntent;
    private String url;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG,"1");
        initView();
        Intent intent = getIntent();
        if (intent != null) {
            int pic_height =intent.getExtras().getInt(MainActivity.CODE_PIC_HEIGHT);
            int pic_width  =intent.getExtras().getInt(MainActivity.CODE_PIC_WIDTH);
            int position   =intent.getExtras().getInt(MainActivity.CODE_PIC_WIDTH);
            if ((url = intent.getExtras().getString(MainActivity.CODE_URL))!= null) {
                DisplayMetrics metrics = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(metrics);
                int width = metrics.widthPixels;
                int height = (int) ((width * 1.0 / pic_width * pic_height));
                FrameLayout.LayoutParams layouts = (FrameLayout.LayoutParams) imageView.getLayoutParams();
                layouts.height = height;
                layouts.width = width;
                if(imageView!=null) {
                    imageView.setLayoutParams(layouts);
                    imageView.setBackground(PlaceHolderDrawableHelper.getBackgroundDrawable(position));
                    imageView.setImageUrl(url, imageLoader);

                }

            }



        }

    }

    private void getImagFile(final String url) {
//        AsyncTask<String,Void,Bitmap> asyncTask =new AsyncTask<String, Void, Bitmap>() {
//            @Override
//            protected Bitmap doInBackground(String... params) {

        if(imageLoader!=null) {
            imageLoader.get(url, new ImageLoader.ImageListener() {
                @Override
                public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                    if(response!=null){
                        progressDialog.dismiss();
                    Bitmap bitmap = response.getBitmap();
                    if (bitmap != null) {
                        File appDir = new File(Environment.getExternalStorageDirectory(),
                                "photoWallss");

                        if (!appDir.exists()) {
                            appDir.mkdirs();
                        }
                        String filename = url.substring(url.lastIndexOf("/") + 1) + ".jpg)";
                        file = new File(appDir, filename);
                        try {
                            if (file != null) {
                                FileOutputStream ous = new FileOutputStream(file);
                                if (bitmap != null)
                                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, ous);

                                if(ous!=null) {
                                    ous.flush();
                                    ous.close();
                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        myShareIntent = new Intent(Intent.ACTION_SEND);
                        myShareIntent.setType("image/*");
                        if (file != null&&myShareActionProvider!=null) {
                            myShareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));

                            myShareActionProvider.setShareIntent(myShareIntent);

                        }
                                          }
                    }
                }

                @Override
                public void onErrorResponse(VolleyError error) {
                }
            });


        }


    }

    @Override
    protected boolean canback() {
        return true;
    }

    @Override
    protected int provideContentViewID() {
        return R.layout.activity_show_photo;
    }

    private void initView() {
         imageView = (NetworkImageView) findViewById(R.id.NTVphoto);

    }


    @Override
    protected void onStop() {
        mQueue=null;
        imageLoader =null;
        imageCache =null;
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.e(TAG,"2");
        getMenuInflater().inflate(R.menu.menu_photo,menu);
        MenuItem shareItem = menu.findItem(R.id.action_share);
        myShareActionProvider =
                (ShareActionProvider) MenuItemCompat.getActionProvider(shareItem);
        if(url!=null) {
            progressDialog =new ProgressDialog(ShowPhotoActivity.this);
            progressDialog.show();
            getImagFile(url);
        }
        return true;
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_share:


                break;

            case R.id.action_save:

                if(file!=null) {
                    Log.e(TAG, String.valueOf(Uri.fromFile(file)));
                    Snackbar.make(imageView,"图片保存在"+file.getAbsolutePath(),Snackbar.LENGTH_LONG).show();
                }
                break;

        }

        return super.onOptionsItemSelected(item);
    }
}

