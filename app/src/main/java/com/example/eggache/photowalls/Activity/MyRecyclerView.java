package com.example.eggache.photowalls.Activity;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

/**
 * Created by eggache on 2016/5/12.
 */
public class MyRecyclerView extends RecyclerView {
    public MyRecyclerView(Context context) {
        this(context,null);
    }
    public MyRecyclerView(Context context, @Nullable AttributeSet attrs) {
        this(context,attrs,0);
    }

    public MyRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {

        super(context, attrs, defStyle);
    }


}
