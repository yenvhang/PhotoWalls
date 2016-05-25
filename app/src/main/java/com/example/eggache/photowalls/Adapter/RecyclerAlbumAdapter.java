package com.example.eggache.photowalls.Adapter;

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
import com.example.eggache.photowalls.Model.AlbumModel;
import com.example.eggache.photowalls.Net.BitmapCache;
import com.example.eggache.photowalls.PlaceHolderDrawableHelper;
import com.example.eggache.photowalls.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by eggache on 2016/5/20.
 */
public class RecyclerAlbumAdapter extends RecyclerView.Adapter<RecyclerAlbumAdapter.FramHolder>  {
    private final ImageLoader imageLoader;
    private List<AlbumModel> albumModels =new ArrayList<AlbumModel>();
    int width;
    public RecyclerAlbumAdapter( RequestQueue mQueue,int width){
        this.width=width;
        imageLoader =new ImageLoader(mQueue,new BitmapCache());
    }

    @Override
    public FramHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view =LayoutInflater.from(parent.getContext())
                .inflate(R.layout.album_gridview,parent,false);
        FramHolder framHolder =new FramHolder(view);

        return framHolder ;
    }

    @Override
    public void onBindViewHolder(FramHolder holder, final int position) {

        Log.e("onBindViewHolder",position+"");
        AlbumModel item = albumModels.get(position);
        holder.position =position;
        holder.imageView.setBackground(PlaceHolderDrawableHelper.getBackgroundDrawable(position));
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) holder.imageView.getLayoutParams();
        params.width=width;
        params.height = LinearLayout.LayoutParams.WRAP_CONTENT;
        String url =item.getUrl();
        if("null".equals(url.substring(url.lastIndexOf("/")+1))) {
            params.height = params.width;
            holder.imageView.setImageBitmap(null);
        }
        else {
            holder.imageView.setImageUrl(item.getUrl(), imageLoader);

        }
        holder.imageView.setTag(item.getUrl());

        holder.imageView.setLayoutParams(params);




        holder.textView.setText(albumModels.get(position).getName());
    }

    @Override
    public int getItemCount() {
        return albumModels.size();
    }

    class FramHolder extends RecyclerView.ViewHolder implements View.OnClickListener,View.OnLongClickListener{
        TextView textView;
        NetworkImageView imageView;
        LinearLayout linearLayout;
        int position;
        public FramHolder(View itemView) {
            super(itemView);
            imageView = (NetworkImageView) itemView.findViewById(R.id.IV_album_img);
            textView = (TextView) itemView.findViewById(R.id.TV_album_name);
            linearLayout = (LinearLayout) itemView.findViewById(R.id.LL_groupView);
            linearLayout.setOnClickListener(this);
            linearLayout.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            mOnItemClickListerner.onItemClick(v,position);
        }

        @Override
        public boolean onLongClick(View v) {
            mOnItemClickListerner.onItemLongClick(v,position);
            return false;
        }
    }
    public static interface  OnItemClickListerner {
        void onItemClick(View view, int position);

        void onItemLongClick(View view, int position);
    }

    public void setOnItemClickLitener(OnItemClickListerner mOnItemClickListerner) {
        this.mOnItemClickListerner = mOnItemClickListerner;
    }
    OnItemClickListerner mOnItemClickListerner;
    public void addAlbumModel(AlbumModel albumModel){
        albumModels.add(albumModel);
    }

    public List<AlbumModel> getAlbumModels(){
        return albumModels;
    }
}
