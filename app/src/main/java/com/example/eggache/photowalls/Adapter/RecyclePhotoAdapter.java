package com.example.eggache.photowalls.Adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.example.eggache.photowalls.Model.ImageModel;
import com.example.eggache.photowalls.MyApplication;
import com.example.eggache.photowalls.Net.BitmapCache;
import com.example.eggache.photowalls.PlaceHolderDrawableHelper;
import com.example.eggache.photowalls.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by eggache on 2016/5/11.
 */
public class RecyclePhotoAdapter extends RecyclerView.Adapter<RecyclePhotoAdapter.FramHolder> {
    private static final String TAG = "Recycle";
    private final ImageLoader imageLoader;
    Context context;
    int width;
    private List<ImageModel> imageModels = new ArrayList<>();
    private OnItemClickListerner mOnItemClickListerner;
    private RequestQueue mQueue;
    public RecyclePhotoAdapter(RequestQueue mQueue, Context context, int width){
        this.context =context;
        this.width=width;
        this.mQueue =mQueue;
         imageLoader = new ImageLoader(mQueue, new BitmapCache());



    }

    public interface OnItemClickListerner {
        void onItemClick(View view, int position);

        void onItemLongClick(View view, int position);
    }

    public void setOnItemClickLitener(OnItemClickListerner mOnItemClickListerner) {
        this.mOnItemClickListerner = mOnItemClickListerner;
    }





    @Override
    public FramHolder onCreateViewHolder(ViewGroup parent, int viewType) {
       View view =LayoutInflater.from(parent.getContext())
               .inflate(R.layout.gridview_item,parent,false);
        FramHolder f =new FramHolder(view);
//        f.imageView.setTag(f);
        return f;
    }

    @Override
    public void onBindViewHolder(final FramHolder holder, final int position) {
      try {
          ImageModel item = imageModels.get(position);

//        if(!item.getUrl().equals(holder.imageView.getTag())){

          LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) holder.imageView.getLayoutParams();
          params.width = width;

          params.height = (int) ((width*1.0/item.getWidth()*item.getHeight()));


          holder.imageView.setBackground(PlaceHolderDrawableHelper.getBackgroundDrawable(position));
          holder.imageView.setImageUrl(item.getUrl(), imageLoader);
          holder.imageView.setTag(item.getUrl());
          holder.imageView.setLayoutParams(params);





          holder.imageView.setOnLongClickListener(new View.OnLongClickListener() {
              @Override
              public boolean onLongClick(View v) {
                  mOnItemClickListerner.onItemLongClick(v, position);
                  return false;
              }
          });

          holder.imageView.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View v) {
                  mOnItemClickListerner.onItemClick(v, position);

              }
          });

//        }


          holder.textView.setText(item.getDescription());
      }
      catch (Exception e){
          Log.e(TAG,Log.getStackTraceString(e));
      }

    }

    @Override
    public int getItemCount() {
        return imageModels.size();
    }

    class FramHolder extends RecyclerView.ViewHolder{
        TextView textView;
        NetworkImageView imageView;

        public FramHolder(View itemView) {
            super(itemView);
            imageView = (NetworkImageView) itemView.findViewById(R.id.photoIV);
            textView = (TextView) itemView.findViewById(R.id.descriptionTV);
        }

    }
    public void addDrawable(ImageModel imageModel) {
//        float ratio = (float) imageModel.getHeight() / (float) imageModel.getWidth();
//        imageModel.setRatio(ratio);
        this.imageModels.add(imageModel);
        MyApplication.getInstace().setImageModels(imageModels);
    }

    public List<ImageModel> getImageModels(){
        return imageModels;
    }


}
