package com.example.eggache.photowalls.Adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.eggache.photowalls.Model.ImageModel;
import com.example.eggache.photowalls.Model.Photo;
import com.example.eggache.photowalls.PlaceHolderDrawableHelper;
import com.example.eggache.photowalls.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by eggache on 2016/5/11.
 */
public class RecyclePhotoAdapter extends RecyclerView.Adapter<RecyclePhotoAdapter.FramHolder> {
    public boolean isScrolling;
    private static final String TAG = "Recycle";
    private List<Photo> photos;
    Context context;
    int width;
    private List<ImageModel> imageModels = new ArrayList<>();
    private OnItemClickListerner mOnItemClickListerner;
    public RecyclePhotoAdapter(List<Photo> photos, Context context,int width){
        this.photos =photos;
        this.context =context;
        this.width=width;

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
        f.imageView.setTag(f);
        return f;
    }

    @Override
    public void onBindViewHolder(final FramHolder holder, final int position) {
        Log.e(TAG,position+"");
        holder.textView.setText(position+"");
        ImageModel item =imageModels.get(position);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) holder.imageView.getLayoutParams();
        Log.e(TAG,"item.getWidth()"+item.getWidth()+"item.getHeight()"+item.getHeight());
        double ratio = item.getHeight()*1.0 /(item.getWidth()*1.0)>=1?
                item.getHeight()*1.0 /item.getWidth():1;
        Log.e(TAG,"ratio"+ratio);
        params.height = (int) (width *ratio);
        Log.e(TAG," params.height"+ params.height);
        holder.imageView.setLayoutParams(params);
        holder.imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        Picasso.with(context).load(item.getUrl())
                .placeholder(PlaceHolderDrawableHelper
                        .getBackgroundDrawable(position)).into(holder.imageView);

//        Photo.setImageBitmap(context,holder.imageView,position,width,isScrolling);
        holder.imageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mOnItemClickListerner.onItemLongClick(v,holder.getLayoutPosition());
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return imageModels.size();
    }

    class FramHolder extends RecyclerView.ViewHolder{
        TextView textView;
        ImageView imageView;

        public FramHolder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.photoIV);
            textView = (TextView) itemView.findViewById(R.id.descriptionTV);
        }

    }
    public void addDrawable(ImageModel imageModel) {
        float ratio = (float) imageModel.getHeight() / (float) imageModel.getWidth();
        imageModel.setRatio(ratio);
        this.imageModels.add(imageModel);
    }

    public List<ImageModel> getImageModels(){
        return imageModels;
    }
}
