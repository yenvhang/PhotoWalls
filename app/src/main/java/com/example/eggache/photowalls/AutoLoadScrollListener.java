package com.example.eggache.photowalls;

import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.example.eggache.photowalls.Model.Photo;
import com.example.eggache.photowalls.Util.LoadImageTask;

/**
 * Created by eggache on 2016/5/12.
 */
public class AutoLoadScrollListener extends RecyclerView.OnScrollListener {
    private String TAG="AutoLoadScroll";

    @Override
    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
        super.onScrollStateChanged(recyclerView, newState);
        switch (newState){
            case 0:
                Log.e(TAG,"STOP");

                break;
            case 1:

                for(LoadImageTask task:Photo.taskCollection){
                    task.cancel(true);
                }
                Log.e(TAG,"MOVE");
                break;
            case 2:
                Log.e(TAG,"MOVING");
                break;

        }
    }
}
