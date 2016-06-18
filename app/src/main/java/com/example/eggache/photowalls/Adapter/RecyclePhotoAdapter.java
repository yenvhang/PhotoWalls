package com.example.eggache.photowalls.Adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.daimajia.swipe.SwipeLayout;
import com.example.eggache.photowalls.Model.ImageModel;
import com.example.eggache.photowalls.MyApplication;
import com.example.eggache.photowalls.R;
import com.example.eggache.photowalls.Util.PlaceHolderDrawableHelper;

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
    private OnItemClickListener mOnItemClickListerner;
    private RequestQueue mQueue;
    private OnHideViewClickListener mOnHideViewClickListener;

    public RecyclePhotoAdapter(Context context, int width){
        this.context =context;
        this.width=width;
        this.mQueue =MyApplication.getmRequestQueue();
        this.imageLoader =MyApplication.getImageLoader();



    }





    public void setOnItemClickLitener(OnItemClickListener mOnItemClickListerner) {
        this.mOnItemClickListerner = mOnItemClickListerner;
    }

    public void setOnHideViewClickListener(OnHideViewClickListener mOnHideViewClickListener){
        this.mOnHideViewClickListener = mOnHideViewClickListener;
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
          holder.imageView.setLayoutParams(params);
          holder.imageView.setImageUrl(item.getUrl(), imageLoader);
          holder.imageView.setTag(item.getUrl());


          registerUIListener(holder, position);

//        }


          holder.textView.setText(item.getDescription());
      }
      catch (Exception e){
          Log.e(TAG,Log.getStackTraceString(e));
      }

    }

    private void registerUIListener(FramHolder holder, final int position) {
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

        holder.update_aLbum_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              mOnHideViewClickListener.onClick(v,position);
            }
        });

        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnHideViewClickListener.onClick(v,position);
            }
        });

        holder.update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnHideViewClickListener.onClick(v,position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return imageModels.size();
    }

    class FramHolder extends RecyclerView.ViewHolder{
        private final Button delete;
        private final Button update;
        private final Button update_aLbum_photo;
        TextView textView;
        NetworkImageView imageView;

        public FramHolder(View itemView) {
            super(itemView);
            SwipeLayout swipeLayout = (SwipeLayout) itemView;
                    imageView = (NetworkImageView) swipeLayout.findViewById(R.id.photoIV);
            textView = (TextView) swipeLayout.findViewById(R.id.descriptionTV);
            update_aLbum_photo = (Button) swipeLayout.findViewById(R.id.btn_update_album_photo);
            delete = (Button) swipeLayout.findViewById(R.id.TV_photoManager_delet);
            update = (Button) swipeLayout.findViewById(R.id.TV_photoManager_update);
            //set show mode.
            swipeLayout.setShowMode(SwipeLayout.ShowMode.LayDown);

//add drag edge.(If the BottomView has 'layout_gravity' attribute, this line is unnecessary)
            swipeLayout.addDrag(SwipeLayout.DragEdge.Left, swipeLayout.findViewById(R.id.bottom_wrapper));
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
