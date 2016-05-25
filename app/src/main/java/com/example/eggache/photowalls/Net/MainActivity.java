//package com.example.eggache.photowalls.Net;
//
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.os.AsyncTask;
//import android.os.Bundle;
//import android.support.v7.app.ActionBarActivity;
//import android.support.v7.widget.LinearLayoutManager;
//import android.support.v7.widget.RecyclerView;
//import android.util.Log;
//import android.view.Menu;
//import android.view.MenuItem;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ImageView;
//import android.widget.LinearLayout;
//
//import com.android.volley.RequestQueue;
//import com.android.volley.Response;
//import com.android.volley.VolleyError;
//import com.android.volley.toolbox.ImageRequest;
//import com.android.volley.toolbox.Volley;
//import com.example.eggache.photowalls.R;
//
//public class MainActivity extends ActionBarActivity {
//    private RecyclerView mRecyclerView;
//    private RecyclerView.Adapter mAdapter;
//    private RecyclerView.LayoutManager mLayoutManager;
//    String[] strings = {"123", "456", "789", "111"};
//    Bitmap[] bitmaps;
//    int finishedNumbers = 0;//已经下载完的图片数
//    RequestQueue requestQueue;
//    private boolean isFirstBoot = true;//判断第一次启动，开始下载图片
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//        bitmaps = new Bitmap[Images.imageThumbUrls.length];
//        for (int i = 0; i < Images.imageThumbUrls.length; i++) {
//            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.polaroids);
//            bitmaps[i] = bitmap;
//        }
//        requestQueue = Volley.newRequestQueue(MainActivity.this);//获取RequestQueue实例
//         mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
//        // use this setting to improve performance if you know that changes
//        // in content do not change the layout size of the RecyclerView
//        mRecyclerView.setHasFixedSize(true);
//        // use a linear layout manager
//        mLayoutManager = new LinearLayoutManager(this);
//        mRecyclerView.setLayoutManager(mLayoutManager);
//        // specify an adapter (see also next example)
//        mAdapter = new MyAdapter(strings);
//        mAdapter = new ImageAdapter(bitmaps);
//        mRecyclerView.setAdapter(mAdapter);
//        mRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
//                super.onScrollStateChanged(recyclerView, newState);
//                if (mRecyclerView.getScrollState() == RecyclerView.SCROLL_STATE_IDLE) {
//                    Log.i("SJJ", "SCROLL_STATE_IDLE");
//                    requestQueue.start();//如果没有滚动，状态是idle，那就下载图片
//                } else {
//                    requestQueue.stop();
//                    //如果开始滚动，停止下载
//                }
//            }
//
//            @Override
//            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
//                super.onScrolled(recyclerView, dx, dy);
//            }
//        });
//        if (isFirstBoot) {
//            downLoadPhoto(finishedNumbers);
//            //开始下载，从第0张开始
//            isFirstBoot = false;
//        }
//    }
//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_main, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }
//
//    public void downLoadPhoto(int n) {
//        ImageRequest imageRequest = new ImageRequest(Images.imageThumbUrls[n], new Response.Listener<Bitmap>() {
//            @Override
//            public void onResponse(Bitmap bitmap) {
//                bitmaps[finishedNumbers] = bitmap;
//                finishedNumbers++;
//                mAdapter.notifyDataSetChanged();
//                if (finishedNumbers < Images.imageThumbUrls.length) downLoadPhoto(finishedNumbers);
//            }
//        }, 0, 0, Bitmap.Config.RGB_565, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError volleyError) {
//                Log.e("SJJ", volleyError.toString());
//            }
//        });
//        requestQueue.add(imageRequest);
//    }
//
//    class DownLoadPhotoTask extends AsyncTask<String, Integer, Bitmap> {
//        Bitmap bm;
//
//        @Override
//        protected Bitmap doInBackground(String... params) {
//            RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
//            ImageRequest imageRequest = new ImageRequest(params[0], new Response.Listener<Bitmap>() {
//                @Override
//                public void onResponse(Bitmap bitmap) {
//                    bm = bitmap;
//                }
//            }, 0, 0, Bitmap.Config.RGB_565, new Response.ErrorListener() {
//                @Override
//                public void onErrorResponse(VolleyError volleyError) {
//                    Log.e("SJJ", volleyError.toString());
//                }
//            });
//            requestQueue.add(imageRequest);
//            return bm;
//        }
//
//        @Override
//        protected void onPostExecute(Bitmap bitmap) {
//            super.onPostExecute(bitmap);
//            bitmaps[finishedNumbers] = bitmap;
//            finishedNumbers++;
//            mAdapter.notifyDataSetChanged();
//        }
//    }
//
//    class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.MyViewHolder> {
//        Bitmap[] bitmaps;
//
//        class MyViewHolder extends RecyclerView.ViewHolder {
//            ImageView imageView;
//
//            public MyViewHolder(View itemView) {
//                super(itemView);
//                imageView = (ImageView) itemView.findViewById(R.id.photo_info);
//            }
//        }
//
//        ImageAdapter(Bitmap[] bitmaps) {
//            this.bitmaps = bitmaps;
//        }
//
//        @Override
//        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//            LinearLayout linearLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.layout_photp_item, parent, false);
//            MyViewHolder viewHolder = new MyViewHolder(linearLayout);
//            return viewHolder;
//        }
//
//        @Override
//        public void onBindViewHolder(MyViewHolder holder, int position) {
//            holder.imageView.setImageBitmap(bitmaps[position]);
//        }
//
//        @Override
//        public int getItemCount() {
//            return bitmaps.length;
//        }
//    }