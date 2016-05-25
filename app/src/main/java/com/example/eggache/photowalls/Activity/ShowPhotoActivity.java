package com.example.eggache.photowalls.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.eggache.photowalls.MainActivity;
import com.example.eggache.photowalls.Model.ImageModel;
import com.example.eggache.photowalls.MyApplication;
import com.example.eggache.photowalls.PlaceHolderDrawableHelper;
import com.example.eggache.photowalls.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by eggache on 2016/5/14.
 */
public class ShowPhotoActivity extends AppCompatActivity {
    private ViewPager viewpager;
    private String TAG="ShowPhtotActivity";
    LayoutInflater Inflater;
    private List<ImageModel> imagemodels;
    private ArrayList<View> list;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.showphoto);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initView();

        int position = 0;
        String url=null;
        Intent intent = getIntent();
        if (intent != null) {


            position =intent.getExtras().getInt(MainActivity.CODE_POSITION);
            if ((url = intent.getExtras().getString(MainActivity.CODE_URL))!= null){
                Inflater =LayoutInflater.from(ShowPhotoActivity.this);
               View item;
                imagemodels =((MyApplication)getApplication()).getImageModels();
                 list =new ArrayList<View>();
                for (int i = 0; i < imagemodels.size(); i++) {
                    item = Inflater.inflate(R.layout.showview_item, null);

                    list.add(item);
                }

                viewpager.setAdapter(new MyAdapter());
                viewpager.setCurrentItem(position);

            }



        }

    }

    private void initView() {
         viewpager = (ViewPager) findViewById(R.id.myviewpager);

    }

    class MyAdapter extends PagerAdapter{


        @Override
        public int getCount() {
            return imagemodels.size() ;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view ==object;
        }
        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            ((ViewPager)container).removeView(list.get(position % list.size()));

        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            ((ViewPager)container).addView(list.get(position % list.size()), 0);

            View view =list.get(position % list.size());
            TextView textView = (TextView) view.findViewById(R.id.TV_desctiption);
            textView.setText(imagemodels.get(position).getDescription());
            ImageView imageview = (ImageView) view.findViewById(R.id.IV_showphoto);
            imageview.setScaleType(ImageView.ScaleType.FIT_CENTER);
            Picasso.with(MyApplication.getInstace()).load(imagemodels.get(position).getUrl())
                    .placeholder(PlaceHolderDrawableHelper
                            .getBackgroundDrawable(position)).into(imageview);
            return view;

        }
    }


}
