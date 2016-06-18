package com.example.eggache.photowalls.Adapter;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.daimajia.swipe.SwipeLayout;
import com.example.eggache.photowalls.Model.AlbumModel;
import com.example.eggache.photowalls.Net.BitmapCache;
import com.example.eggache.photowalls.Util.PlaceHolderDrawableHelper;
import com.example.eggache.photowalls.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by eggache on 2016/5/20.
 */
public class RecyclerAlbumAdapter extends RecyclerView.Adapter<RecyclerAlbumAdapter.FramHolder>  {
    private final ImageLoader imageLoader;
    private List<AlbumModel> albumModels =new ArrayList<AlbumModel>();
    private OnHideViewClickListener mOnHideViewClickListener;
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
        params.height = params.width;
        String url =item.getUrl();

        if("null".equals(url.substring(url.lastIndexOf("/")+1))) {

            holder.imageView.setImageBitmap(null);
        }
        else {
            holder.imageView.setImageUrl(item.getUrl(), imageLoader);

        }
        holder.imageView.setTag(item.getUrl());
        holder.imageView.setLayoutParams(params);
        holder.imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        holder.textView.setText(albumModels.get(position).getName());

        registerUIListener(holder, position);
    }

    private void registerUIListener(FramHolder holder, final int position) {
        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnItemClickListerner.onItemClick(v,position);
            }
        });
        holder.imageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mOnItemClickListerner.onItemLongClick(v,position);
                return false;
            }
        });

        holder.add.setOnClickListener(new View.OnClickListener() {
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
        return albumModels.size();
    }

    class FramHolder extends RecyclerView.ViewHolder{
        private final Button add;
        private final Button delete;
        private final Button update;
        TextView textView;
        NetworkImageView imageView;

        int position;
        public FramHolder(View itemView) {
            super(itemView);
            SwipeLayout swipeLayout = (SwipeLayout) itemView;
            imageView = (NetworkImageView) swipeLayout.findViewById(R.id.IV_album_img);
            textView = (TextView) swipeLayout.findViewById(R.id.TV_album_name);
            add = (Button) swipeLayout.findViewById(R.id.btn_addAlbum);
            delete = (Button) swipeLayout.findViewById(R.id.btn_deleteAlbum);
            update = (Button) swipeLayout.findViewById(R.id.btn_updateAlbum);
            swipeLayout.setShowMode(SwipeLayout.ShowMode.LayDown);

//add drag edge.(If the BottomView has 'layout_gravity' attribute, this line is unnecessary)
            swipeLayout.addDrag(SwipeLayout.DragEdge.Left, swipeLayout.findViewById(R.id.bottom_wrapper));

        }


    }

    public void setOnHideViewClickListener(OnHideViewClickListener mOnHideViewClickListener){
        this.mOnHideViewClickListener = mOnHideViewClickListener;
    }



    public void setOnItemClickLitener(OnItemClickListener mOnItemClickListerner) {
        this.mOnItemClickListerner = mOnItemClickListerner;
    }
    OnItemClickListener mOnItemClickListerner;
    public void addAlbumModel(AlbumModel albumModel){
        albumModels.add(albumModel);
    }

    public List<AlbumModel> getAlbumModels(){
        return albumModels;
    }
}
