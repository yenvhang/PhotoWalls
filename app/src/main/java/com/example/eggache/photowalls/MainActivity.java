package com.example.eggache.photowalls;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.example.eggache.photowalls.Adapter.RecyclePhotoAdapter;
import com.example.eggache.photowalls.Model.ImageModel;
import com.example.eggache.photowalls.Model.Photo;
import com.example.eggache.photowalls.Source.Images;
import com.example.eggache.photowalls.Util.ImageLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    private static final String TAG ="MainActivity" ;
    RecyclerView recyclerView;
    private String mImageUrl;
    private ImageLoader imageLoader;
    private List<Photo> photos;
    public int width ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        width=(getWindowManager().getDefaultDisplay().getWidth()+20)/2;
        initData();
        initView();




        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    private void initData() {
        photos =new ArrayList<Photo>();

        for(int i=0;i< Images.imageUrls.length;i++){
            photos.add(new Photo("Journey To The West.Concept",Images.imageUrls[i]));
            
        }
    }

    private void initView() {

        recyclerView = (RecyclerView) findViewById(R.id.recycleview_photo);
        StaggeredGridLayoutManager staggeredGridLayoutManager =
                new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(staggeredGridLayoutManager);

        final RecyclePhotoAdapter recyclePhotoAdapter =new RecyclePhotoAdapter(photos,MainActivity.this,width);
        int startPosition =recyclePhotoAdapter.getItemCount();
        for(String url : Images.imageUrls) {
            Random random =new Random();

            int height =  (random.nextInt(5)+5)*100;

            ImageModel imageModel=new ImageModel();
            imageModel.setUrl(url);
            imageModel.setWidth(width);
            imageModel.setHeight(height);
            recyclePhotoAdapter.addDrawable(imageModel);
            imageModel=null;
        }
      //  recyclePhotoAdapter.notifyItemRangeInserted(startPosition, Images.imageUrls.length);
        recyclePhotoAdapter.setOnItemClickLitener(new RecyclePhotoAdapter.OnItemClickListerner() {
            @Override
            public void onItemClick(View view, int position) {

            }

            @Override
            public void onItemLongClick(View view, int position) {
                recyclePhotoAdapter.getImageModels().remove(position);
                recyclePhotoAdapter.notifyItemRemoved(position);

            }
        });
        recyclerView.setAdapter(recyclePhotoAdapter);
//        recyclerView.setHasFixedSize(true);

//        recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
//                super.onScrollStateChanged(recyclerView, newState);
//                switch (newState){
//                    case 0:
//
//                        recyclePhotoAdapter.isScrolling=false;
//                        recyclePhotoAdapter.notifyDataSetChanged();
//                        break;
//                    case 1:
//                        recyclePhotoAdapter.isScrolling=true;
//                        for(LoadImageTask task:Photo.taskCollection){
//                            task.cancel(true);
//
//                            Log.e(TAG,"任务被关闭");
//                        }
//
//                        break;
//                    case 2:
//
//                        break;
//
//                }
//            }
//        });

//        SpacesItemDecoration decoration=new SpacesItemDecoration(5);
//        recyclerView.addItemDecoration(decoration);




    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }



}
