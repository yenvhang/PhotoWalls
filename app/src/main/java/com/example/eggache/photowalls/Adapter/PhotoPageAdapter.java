//package com.example.eggache.photowalls.Adapter;
//
//import android.support.v4.view.PagerAdapter;
//import android.support.v4.view.ViewPager;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ImageView;
//import android.widget.ScrollView;
//import android.widget.TextView;
//
//import com.example.eggache.photowalls.Model.ImageModel;
//import com.example.eggache.photowalls.MyApplication;
//import com.example.eggache.photowalls.PlaceHolderDrawableHelper;
//import com.example.eggache.photowalls.R;
//import com.squareup.picasso.Picasso;
//
//import java.util.List;
//
///**
// * Created by eggache on 2016/5/14.
// */
//public  class PhotoPageAdapter <T> extends PagerAdapter {
//    private List<T> list ;
//    public PhotoPageAdapter(List <T> list){
//        this.list=list;
//
//    }
//    @Override
//    public int getCount() {
//        return list.size();
//    }
//
//    @Override
//    public boolean isViewFromObject(View view, Object object) {
//        return view==object;
//    }
//
//    @Override
//    public Object instantiateItem(ViewGroup container, int position) {
//     //   ScrollView mscrollview = (ScrollView) container.findViewById(R.id.myscrollview);
//        ImageView imageView= (ImageView) mscrollview.findViewById(R.id.IV_showphoto);
//        TextView textView = (TextView) mscrollview.findViewById(R.id.TV_desctiption);
//        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
//        Picasso.with(MyApplication.getInstace()).load(((ImageModel)(list.get(position))).getUrl())
//                .placeholder(PlaceHolderDrawableHelper
//                        .getBackgroundDrawable(position)).into(imageView);
//        textView.setText(position);
//        container.addView(mscrollview,position);
//        return mscrollview;
//    }
//    @Override
//    public void destroyItem(ViewGroup container, int position, Object object) {
//        ((ViewPager)container).removeView((ImageView)object);
//    }
//}
