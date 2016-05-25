package com.example.eggache.photowalls;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.eggache.photowalls.Activity.LoginActivity;
import com.example.eggache.photowalls.Activity.ShowPhotoActivity;
import com.example.eggache.photowalls.Adapter.RecyclePhotoAdapter;
import com.example.eggache.photowalls.Adapter.RecyclerAlbumAdapter;
import com.example.eggache.photowalls.Model.AlbumModel;
import com.example.eggache.photowalls.Model.ImageModel;
import com.example.eggache.photowalls.Util.ImageLoader;
import com.example.eggache.photowalls.Util.LoginManager;
import com.example.eggache.photowalls.Util.SourcePath;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.msebera.android.httpclient.Header;
import me.drakeet.materialdialog.MaterialDialog;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "MainActivity";
    RecyclerView recyclerView;

    public int width;
    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    SwipeRefreshLayout mSwipeRefreshLayout;
    MaterialDialog mMaterialDialog = new MaterialDialog(MainActivity.this);
    public final static String CODE_URL = "url";
    public final static String CODE_POSITION = "postion";

    private RecyclePhotoAdapter recyclePhotoAdapter;
    private RequestQueue mQueue;
    public static final int CODE_REQUEST_CAMERA = 1;
    private static final int CODE_REQUES_PHOTO = 2;
    private static final int CODE_REQUEST_LOGIN =3;

    private static List<AlbumModel> albumModels = new ArrayList<AlbumModel>();
    private static final String URL = "192.168.155.1";

    private MaterialDialog upload;
    private FloatingActionsMenu floatingActionsMenu;

    private File filepath;
    private JsonArrayRequest getImageSourceJSONArrayRequest;
    private Spinner spinner;
    private RecyclerAlbumAdapter recyclerAlbumAdapter;
    private int MODEL = 0;//按图片查看
    private ImageView img_login;
    private TextView user_info;
    private LoginManager loginManager;
    private String loginUser;
    private View.OnClickListener addAlbumListener;
    private View.OnClickListener deleteAlbumListener;
    private View.OnClickListener updateAlbumListener;
    private Response.Listener<String> createAlbumSuccessListener;
    private int albumPosition;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.id_toolbar);
        setSupportActionBar(toolbar);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.id_drawer_layout);
        mNavigationView = (NavigationView) findViewById(R.id.id_nv_menu);
        final ActionBar ab = getSupportActionBar();

        ab.setHomeAsUpIndicator(R.drawable.menu);

        ab.setDisplayHomeAsUpEnabled(true);
        setupDrawerContent(mNavigationView);
        mQueue = Volley.newRequestQueue(this);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.activity_main_swipe_refresh_layout);
        width = (getWindowManager().getDefaultDisplay().getWidth() ) / 2-10;

        initView();



        //获取图片资源
        getImageResourceFromNet();

        //获取相册资源


        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Refresh();
            }

        });
    }

    private void getAlbumResourceFromNet() {
        recyclerAlbumAdapter.getAlbumModels().clear();
        getAlbumTask(new Response.Listener<JSONArray>() {
                         @Override
                         public void onResponse(JSONArray response) {

                             for (int i = 0; i < response.length(); i++) {
                                 try {
                                     JSONObject object = response.getJSONObject(i);
                                     AlbumModel albumodel = new AlbumModel();
                                     albumodel.setId(object.getInt("id"));

                                     albumodel.setName(object.getString("name"));
                                     albumodel.setCreateby(object.getString("createby"));
                                     albumodel.setDescription(object.getString("description"));
                                     albumodel.setUrl(object.getString("url"));
                                     if(!recyclerAlbumAdapter.getAlbumModels().contains(albumodel))
                                        recyclerAlbumAdapter.addAlbumModel(albumodel);
                                 } catch (Exception e) {
                                     Log.e(TAG, Log.getStackTraceString(e));
                                 }
                             }
                                recyclerView.removeAllViews();
                                makeFlowView(2);
                                recyclerView.setAdapter(recyclerAlbumAdapter);
                         }

                     }
        );
    }

    /*
            获取图片资源
             */
    private void getImageResourceFromNet() {
        recyclePhotoAdapter.getImageModels().clear();
        getImageSourceJSONArrayRequest = new JsonArrayRequest("http://" + URL + ":8080/PhotoBase/pictures/getpictures", new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                for (int i = 0; i < response.length(); i++) {

                    try {
                        JSONObject object = response.getJSONObject(i);

                        newImageModel(recyclePhotoAdapter,object.getInt("id"),(String) object.get("url"),
                                (String) object.get("description"),
                                object.getInt("width"),
                                object.getInt("height"));

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }

                recyclerView.setAdapter(recyclePhotoAdapter);



//                recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
//                    @Override
//                    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
//                    if(newState==0){
//
//                        mQueue.start();
//                    }
//                        else{
//
//                        mQueue.stop();
//                    }

//                    }
//                });


            }


        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, "网络连接错误", Toast.LENGTH_LONG).show();

            }
        });
        Log.e(TAG,"BIGIN");
        mQueue.add(getImageSourceJSONArrayRequest);
    }


    private void Refresh() {
        if (MODEL == 0) {
            makeFlowView(2);
            recyclerView.removeAllViews();
            getImageResourceFromNet();
            Log.e(TAG,"recyclePhotoAdapter:"+recyclePhotoAdapter.getImageModels().size()+"");

        } else if(MODEL==1){

            makeFlowView(2);
            getAlbumResourceFromNet();
        }
        else {
            makeFlowView(2);
            getImageFromAlbum(albumPosition);
        }
        mSwipeRefreshLayout.setRefreshing(false);
    }


    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(

                new NavigationView.OnNavigationItemSelectedListener() {

                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        menuItem.setChecked(true);
                        mDrawerLayout.closeDrawers();
                        return true;
                    }
                });
    }

    private void initView() {
        recyclerView = (RecyclerView) findViewById(R.id.recycleview_photo);
//        recyclerView.setHasFixedSize(true);

        makeFlowView(2);

        recyclePhotoAdapter = new RecyclePhotoAdapter(mQueue, MainActivity.this, width);

            RelativeLayout relativeLayout = (RelativeLayout) mNavigationView.getHeaderView(0);
            img_login = (ImageView) relativeLayout.findViewById(R.id.IV_login);
            user_info = (TextView) relativeLayout.findViewById(R.id.id_username);

            if (img_login != null)
                img_login.setOnClickListener(this);

            loginManager =LoginManager.getInstance();
            String username ;
            if((username=loginManager.getLoginInfo())!=null){
                user_info.setText(username);
            }
            else
                user_info.setText(loginManager.getDefaultInfo());




        floatingActionsMenu = (FloatingActionsMenu) findViewById(R.id.multiple_actions);
        FloatingActionButton fb1 = (FloatingActionButton) findViewById(R.id.action_a);

        //上传图片功能入口
        fb1.setTitle("上传图片");
        fb1.setIcon(R.drawable.upload);
        fb1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if((loginUser=loginManager.getLoginInfo())==null){
                    loginManager.login(MainActivity.this);

                    return ;

                }

                final MaterialDialog mMaterialDialog = new MaterialDialog(MainActivity.this);
                mMaterialDialog.setTitle("上传照片");
                mMaterialDialog.setNegativeButton("取消", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mMaterialDialog.dismiss();
                    }
                });

                //加载 获取图片方式 界面
                LinearLayout linearLayout = (LinearLayout) LayoutInflater.
                        from(MainActivity.this).inflate(R.layout.upload_select_view, null);
                mMaterialDialog.setContentView(linearLayout);
                mMaterialDialog.show();

                final Button fromCamera = (Button) linearLayout.findViewById(R.id.btn_camera);
                final Button fromPhoto = (Button) linearLayout.findViewById(R.id.btn_photo);
                final Button fromNet = (Button) linearLayout.findViewById(R.id.btn_net);
                //从相机获取图片
                fromCamera.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getImageFromCamera();
                        mMaterialDialog.dismiss();
                    }
                });
                //从相册获取图片
                fromPhoto.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getImageFromAlbum();
                        mMaterialDialog.dismiss();
                    }
                });
                //从网络获取图片
                fromNet.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(MainActivity.this, "开发中...", Toast.LENGTH_LONG).show();
                    }
                });

            }


        });






        //为点击图片设置事件
        recyclePhotoAdapter.setOnItemClickLitener(new RecyclePhotoAdapter.OnItemClickListerner() {
            @Override
            //点击 进入图片
            public void onItemClick(View view, int position) {
                Intent intent = new Intent(MainActivity.this, ShowPhotoActivity.class);
                String url = recyclePhotoAdapter.getImageModels().get(position).getUrl();
                ((MyApplication) getApplication()).setImageModels(recyclePhotoAdapter.getImageModels());
                intent.putExtra(CODE_URL, url);
                intent.putExtra(CODE_POSITION, position);
                startActivity(intent);

            }

            //管理图片
            @Override
            public void onItemLongClick(View view, final int position) {

                final MaterialDialog photoManagerDialog =new MaterialDialog(MainActivity.this);
                LinearLayout linearlayout = (LinearLayout) LayoutInflater.from(MainActivity.this).inflate(R.layout.dialog_photomanager,null);
                Button delete = (Button) linearlayout.findViewById(R.id.TV_photoManager_delet);
                Button update = (Button) linearlayout.findViewById(R.id.TV_photoManager_update);
                Button cancel = (Button) linearlayout.findViewById(R.id.TV_photoManager_cancel);
                photoManagerDialog.setContentView(linearlayout);
                photoManagerDialog.show();

                delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final MaterialDialog mMaterialDialog =new MaterialDialog(MainActivity.this);
                        mMaterialDialog.setTitle("确认信息")
                                .setMessage("确定删除图片？")
                                .setPositiveButton("确定", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                        mMaterialDialog.dismiss();

                                        deleteImageTask(position);

                                    }
                                })
                                .setNegativeButton("取消", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        mMaterialDialog.dismiss();

                                    }
                                });

                        mMaterialDialog.show();
                        photoManagerDialog.dismiss();
                    }
                });

               update.setOnClickListener(new View.OnClickListener() {
                   @Override
                   public void onClick(View v) {
                       final MaterialDialog updateDialog =new MaterialDialog(MainActivity.this);
                       final EditText editText =new EditText(MainActivity.this);
                       editText.setText(recyclePhotoAdapter.getImageModels().get(position).getDescription());
                       updateDialog.setContentView(editText);


                       updateDialog.setPositiveButton("确定", new View.OnClickListener() {
                           @Override
                           public void onClick(View v) {
                                updatePhotoInfoTask(position,editText.getText().toString());
                               updateDialog.dismiss();
                           }
                       });


                       updateDialog.setNegativeButton("取消", new View.OnClickListener() {
                           @Override
                           public void onClick(View v) {
                               updateDialog.dismiss();
                           }
                       });
                       updateDialog.show();
                       photoManagerDialog.dismiss();
                   }
               });


                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        photoManagerDialog.dismiss();
                    }
                });



            }
        });

    }

    private void makeFlowView(int columns) {
        StaggeredGridLayoutManager staggeredGridLayoutManager =
                new StaggeredGridLayoutManager(columns, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(staggeredGridLayoutManager);

    }

    /*
    创建添加相册对话框
     */
    private MaterialDialog createAlbumDialog() {
         final MaterialDialog createAlbum = new MaterialDialog(MainActivity.this);
        LinearLayout linearLayout = (LinearLayout) LayoutInflater.from(MainActivity.this).inflate(R.layout.createalbumview, null);
        final EditText etName = (EditText) linearLayout.findViewById(R.id.ETalbum_name);
        final EditText etDes = (EditText) linearLayout.findViewById(R.id.ETalbum_description);
        createAlbum.setContentView(linearLayout);
        createAlbum.setPositiveButton("创建", new View.OnClickListener() {


            @Override
            public void onClick(View v) {
                final String name = etName.getText().toString();
                final String description = etDes.getText().toString();


                StringRequest stringRequest = new
                        StringRequest(Request.Method.POST,
                                "http://" + URL + ":8080/PhotoBase/users/create",createAlbumSuccessListener, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {

                                Toast.makeText(MainActivity.this, "网络连接错误", Toast.LENGTH_LONG).show();
                            }
                        }) {
                            @Override
                            protected Map<String, String> getParams() throws AuthFailureError {
                                Map<String, String> map = new HashMap<String, String>();
                                map.put("name", name);
                                map.put("createby", "creep");
                                map.put("description", description);
                                return map;
                            }

                            @Override
                            public Map<String, String> getHeaders() throws AuthFailureError {
                                Map<String, String> headers = new HashMap<String, String>();
                                headers.put("Charset", "UTF-8");
                                return headers;
                            }
                        };
                if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(description)) {
                    try {
                        mQueue.add(stringRequest);
                        createAlbum.dismiss();

                    } catch (Exception e) {
                        Log.e(TAG, Log.getStackTraceString(e));
                    }
                } else {
                    Toast.makeText(MainActivity.this, "不能为空", Toast.LENGTH_LONG).show();

                }
                Log.e(TAG, "8");
            }
        });
        createAlbum.setNegativeButton("取消", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createAlbum.dismiss();
            }
        });
        return createAlbum;
    };

    /*
    从相机获取图片
     */
    private void getImageFromCamera() {

        String state = Environment.getExternalStorageState();
        if (state.equals(Environment.MEDIA_MOUNTED)) {
            Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
            String path = Environment.getExternalStorageDirectory() +
                    File.separator + MyApplication.getInstace().getPackageName();
            File dir = new File(path);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            filepath = new File(dir, SourcePath.getTheCameraPhotoPath());
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(filepath));
            startActivityForResult(intent, CODE_REQUEST_CAMERA);
        } else {
            Toast.makeText(MainActivity.this, "请确认已经插入SD卡", Toast.LENGTH_LONG).show();
        }

    }

    /*
    从相册获取图片
     */
    private void getImageFromAlbum() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, CODE_REQUES_PHOTO);
    }

    /*
    设置要加载的图片属性
     */
    private void newImageModel(RecyclePhotoAdapter recyclePhotoAdapter, int id, String url, String description,int width,int height) {
 //       Random random = new Random();
//        int height = (random.nextInt(5) + 5) * 100;
        ImageModel imageModel = new ImageModel();
        imageModel.setUrl(url);
        imageModel.setId(id);
       imageModel.setWidth(width);
        imageModel.setHeight(height);
        imageModel.setDescription(description);

            recyclePhotoAdapter.addDrawable(imageModel);
    }




    /*
    上传图片
     */
    private void SetResultBitmap(final String picturePath) {


        upload = new MaterialDialog(this);
        final ScrollView scrollView = (ScrollView) LayoutInflater.from(this).inflate(R.layout.upload_send_view, null);
        upload.setContentView(scrollView);

        ImageView imageview = (ImageView) scrollView.findViewById(R.id.IV_selectedImage);
        int requiredWidth = (getWindowManager().getDefaultDisplay().getWidth()) / 2;
        Button btn_send = (Button) scrollView.findViewById(R.id.btn_send);
        final EditText ET_photoDescription = (EditText) scrollView.findViewById(R.id.ET_description);
        Bitmap bitmap = ImageLoader.decodeSampledBitmapFromResource(picturePath, requiredWidth);
        spinner = (Spinner) (scrollView.findViewById(R.id.spinner_album));
        clearAlbums();
        final ArrayAdapter arrayAdapter =
                new ArrayAdapter(this, android.R.layout.simple_spinner_item, albumModels);

        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        final Response.Listener<JSONArray> getSpinnerAlbumListener = new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                clearAlbums();

                for (int i = 0; i < response.length(); i++) {
                    try {
                        JSONObject object = response.getJSONObject(i);
                        AlbumModel albumodel = new AlbumModel();
                        albumodel.setId(object.getInt("id"));
                        albumodel.setName(object.getString("name"));
                        albumodel.setDescription(object.getString("description"));



                        albumodel.setCreateby(object.getString("createby"));
                        albumodel.setUrl(object.getString("url"));
                        albumModels.add(albumodel);
                    } catch (Exception e) {
                        Log.e(TAG, Log.getStackTraceString(e));
                    }
                }
                spinner.setSelection(arrayAdapter.getCount() - 1);
            }
        };
        getAlbumTask(getSpinnerAlbumListener);
        spinner.setAdapter(arrayAdapter);
        spinner.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                try {
                    Class<?> clazz = AdapterView.class;
                    Field field = clazz.getDeclaredField("mOldSelectedPosition");
                    field.setAccessible(true);
                    field.setInt(spinner, -1);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return false;
            }
        });

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {


            boolean isfirst = true;

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if (position == 0) {



                    createAlbumSuccessListener =new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            getAlbumTask(getSpinnerAlbumListener);
                        }
                    };
                    final MaterialDialog createAlbum = createAlbumDialog();
                    createAlbum.show();



                    isfirst = false;

                } else {
                    Log.e(TAG, "9");
                    try {
                        setSelectedAlbum((arrayAdapter.getItem(position )).toString());
                    } catch (Exception e) {
                        Log.e(TAG, Log.getStackTraceString(e));
                    }
                    Log.e(TAG, "10");
                }

            }



            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        imageview.setImageBitmap(bitmap);

        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String description = ET_photoDescription.getEditableText().toString();
                String album = selectedAlbum;
                if (inputvalid()) {
                    uploadImageTask(description, album, picturePath);
                }
            }

            private boolean inputvalid() {
                return true;
            }
        });


        upload.setNegativeButton("取消", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                upload.dismiss();
            }
        });
        upload.show();
    }

    private void clearAlbums() {
        albumModels.clear();
        AlbumModel albumModel = new AlbumModel();
        albumModel.setName("添加相册");
        albumModels.add(0, albumModel);
        albumModel = null;
    }

    private void deleteImageTask(final int position){
        final ImageModel photo =recyclePhotoAdapter.getImageModels().get(position);
        StringRequest deletePhotoReques =new StringRequest(Request.Method.POST, "http://" + URL + ":8080/PhotoBase/users/deleltephoto", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
        //        recyclePhotoAdapter.notifyItemRemoved(position);
                recyclePhotoAdapter.getImageModels().remove(position);
               recyclePhotoAdapter.notifyItemRangeChanged(position,recyclePhotoAdapter.getItemCount());

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this,"网络连接错误",Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> maps =new HashMap();
                maps.put("id",photo.getId()+"");
                return maps;
            }

        };
        mQueue.add(deletePhotoReques);
        mQueue.start();
    }

    private void uploadImageTask(final String description, final String album, String picturePath) {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();

        File file = new File(picturePath);
        String path = file.getAbsolutePath();
        String name = path.substring(path.lastIndexOf("/") + 1);
        Log.e(TAG, "filename:" + name);
        try {
            params.put("image", file);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        params.put("name", name);
        params.put("description", description);
        params.put("album", album);

        client.post("http://" + URL + ":8080/PhotoBase/users/uploadimage", params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers,
                                  byte[] responseBody) {
                // 上传成功后要做的工作
                Toast.makeText(MainActivity.this, "上传成功", Toast.LENGTH_LONG).show();
                Refresh();
                progressDialog.dismiss();
                upload.dismiss();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers,
                                  byte[] responseBody, Throwable error) {
                // 上传失败后要做到工作
                Toast.makeText(MainActivity.this, "上传失败", Toast.LENGTH_LONG).show();
                progressDialog.dismiss();
            }

            @Override
            public void onProgress(long bytesWritten, long totalSize) {
                progressDialog.show();
                progressDialog.setProgress((int) (bytesWritten * 1.0 / totalSize * 100));
                super.onProgress(bytesWritten, totalSize);
            }
        });
    }

    private void updatePhotoInfoTask(final int position, final String description){
        final ImageModel photo =recyclePhotoAdapter.getImageModels().get(position);
        StringRequest updatePhotoRequest =new StringRequest(Request.Method.POST, "http://" + URL + ":8080/PhotoBase/users/uodatephoto", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                recyclePhotoAdapter.getImageModels().get(position)
                        .setDescription(description);
                recyclePhotoAdapter.notifyItemChanged(position);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> maps =new HashMap<String,String>();

                maps.put("id",photo.getId()+"");
                maps.put("description",description);
                return  maps;
            }
        };
        mQueue.add(updatePhotoRequest);
       mQueue.start();

    }

    private String selectedAlbum;

    private void setSelectedAlbum(String s) {
        selectedAlbum = s;
    }

    private void getAlbumTask(Response.Listener<JSONArray> sucessListener) {
        JsonArrayRequest jsonalbums = new
                JsonArrayRequest("http://" + URL + ":8080/PhotoBase/users/getalbums", sucessListener, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, "网络连接错误", Toast.LENGTH_LONG).show();
            }
        });
        mQueue.add(jsonalbums);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.browse_byalbum:
                MODEL = 1;
                recyclerAlbumAdapter = new RecyclerAlbumAdapter(mQueue,width);
                recyclerView.setAdapter(recyclerAlbumAdapter);
                recyclerAlbumAdapter.notifyDataSetChanged();
                initAlbumView();
                Refresh();
                break;
            case R.id.browse_byphtot:
                MODEL = 0;

                recyclerView.setAdapter(recyclePhotoAdapter);
                Refresh();




                break;
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);

                break;

        }
        return super.onOptionsItemSelected(item);
    }

    private void initAlbumView() {


        recyclerAlbumAdapter.setOnItemClickLitener(new RecyclerAlbumAdapter.OnItemClickListerner() {
            @Override
            public void onItemClick(View view, int position) {
                recyclerView.removeAllViews();
                recyclePhotoAdapter.getImageModels().clear();
                getImageFromAlbum(position);

                albumPosition =position;

                MODEL=2;



            }

            @Override
            public void onItemLongClick(View view, final int position) {
                Toast.makeText(MainActivity.this,position+"",Toast.LENGTH_SHORT).show();
                final MaterialDialog albumManagerDialog =new MaterialDialog(MainActivity.this);
                LinearLayout linearLayout = (LinearLayout) LayoutInflater.from(MainActivity.this).inflate(R.layout.dialog_albummanager,null);
                albumManagerDialog.setContentView(linearLayout);
                albumManagerDialog.show();
                Button add = (Button) linearLayout.findViewById(R.id.btn_addAlbum);
                Button delete = (Button) linearLayout.findViewById(R.id.btn_deleteAlbum);
                Button update = (Button) linearLayout.findViewById(R.id.btn_updateAlbum);
                Button cancel = (Button) linearLayout.findViewById(R.id.btn_cancel);
                add.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        createAlbumSuccessListener =new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                getAlbumResourceFromNet();
                            }
                        };
                        MaterialDialog createAlbumDialog =createAlbumDialog();
                        createAlbumDialog.show();
                        albumManagerDialog.dismiss();

                    }
                });
                delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {


                            recyclerAlbumAdapter.notifyItemRemoved(position);
                            recyclerAlbumAdapter.getAlbumModels().remove(position);
                            recyclerAlbumAdapter.notifyItemRangeChanged(position,recyclerAlbumAdapter.getItemCount());

                            //// TODO: 2016/5/24 从Album表中删除指定行
                            albumManagerDialog.dismiss();


                    }
                });
            try {
                update.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final MaterialDialog updateAlbumDialog =new MaterialDialog(MainActivity.this);
                        LinearLayout linearLayout = (LinearLayout) LayoutInflater.from(MainActivity.this).inflate(R.layout.dialog_updatealbum,null);
                        final EditText etAlbumName = (EditText) linearLayout.findViewById(R.id.ETalbum_name);
                        final EditText etDescription = (EditText) linearLayout.findViewById(R.id.ETalbum_description);

                        final AlbumModel albumModel =recyclerAlbumAdapter.getAlbumModels().get(position);
                        final String name =albumModel.getName();
                        final String description =albumModel.getDescription();
                        etAlbumName.setText(name);
                        etDescription.setText(description);
                        updateAlbumDialog.setContentView(linearLayout);


                        updateAlbumDialog.setPositiveButton("提交更新", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                if(etAlbumName.getText().toString().equals(name)
                                        &&etDescription.getText().toString().equals(description)){
                                Toast.makeText(MainActivity.this,"没有任何更改",Toast.LENGTH_LONG).show();
                                updateAlbumDialog.dismiss();
                                }
                                else{



                                    albumModel.setName(etAlbumName.getText().toString());
                                    albumModel.setDescription(etDescription.getText().toString());
                                    updateAlbumInfoTask(albumModel,position);


                                    updateAlbumDialog.dismiss();
                                }


                            }
                        });
                        updateAlbumDialog.setNegativeButton("取消", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                updateAlbumDialog.dismiss();

                            }
                        });
                        updateAlbumDialog.show();
                        albumManagerDialog.dismiss();
                    }
                });
            }
            catch (Exception e){
                Log.e(TAG,Log.getStackTraceString(e));
            }
                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        albumManagerDialog.dismiss();
                    }
                });

            }
        });
    }

    private void updateAlbumInfoTask(final AlbumModel albumModel, final int position) {
        StringRequest updateAlbumRequest =new StringRequest(Request.Method.POST, "http://" + URL + ":8080/PhotoBase/users/updatealbum", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {


                recyclerAlbumAdapter.notifyItemChanged(position);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> maps = new HashMap<String, String>();
                maps.put("id",albumModel.getId()+"");
                maps.put("name",albumModel.getName());
                maps.put("description",albumModel.getDescription());
                maps.put("createtime",albumModel.getTimestamp()+"");
                maps.put("createby",albumModel.getCreateby());
                maps.put("url",albumModel.getUrl());

                return  maps;
            }
        };
        mQueue.add(updateAlbumRequest);
    }


    private void getImageFromAlbum(int position) {
        AlbumModel albumModel =recyclerAlbumAdapter.getAlbumModels().get(position);
        final String album =albumModel.getName();


        String baseurl ="http://" + URL + ":8080/PhotoBase/pictures/getpicturesbualbum";

        String url = null;
        try {
            url = baseurl+"?album=" +URLEncoder.encode(album,"UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        final JsonArrayRequest requestPhotoInAlbum =
                new JsonArrayRequest(url,new Response.Listener<JSONArray>() {


                    public void onResponse(JSONArray response) {
                        recyclePhotoAdapter.getImageModels().clear();
                        for(int i=0;i<response.length();i++){
                            try {
                                JSONObject object =response.getJSONObject(i);
                                newImageModel(recyclePhotoAdapter, object.getInt("id"),
                                        object.getString("url"),object.getString("description"),
                                        object.getInt("width"),object.getInt("height"));
                                Log.e(TAG,object.getString("url"));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                        makeFlowView(2);
                        recyclerView.setAdapter(recyclePhotoAdapter);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(MainActivity.this,"请求图片超时",Toast.LENGTH_LONG).show();
                    }
                }){
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String,String>heads = new HashMap();
                        heads.put("Charset", "UTF-8");

                        return heads;
                    }
                };

        mQueue.add(requestPhotoInAlbum);



    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.IV_login:
                Intent i =new Intent(MainActivity.this, LoginActivity.class);
         startActivityForResult(i,CODE_REQUEST_LOGIN);
                break;


        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case CODE_REQUEST_CAMERA:
                if (resultCode == RESULT_OK) {
                    try {
                        Log.e(TAG, "getAbsolutePath" + filepath.getAbsolutePath());
                        Log.e(TAG, "getCanonicalPath" + filepath.getCanonicalPath());
                        Log.e(TAG, "getName" + filepath.getName());
                        Log.e(TAG, "getPath" + filepath.getPath());
                        SetResultBitmap(filepath.getAbsolutePath());


                    } catch (Exception e) {
                        Log.e(TAG, Log.getStackTraceString(e));
                    }
                }
                break;
            case CODE_REQUES_PHOTO:
                if (resultCode == RESULT_OK) {
                    Uri selectedImage = data.getData();
                    final String[] filePathColumn = {MediaStore.Images.Media.DATA};

                    Cursor cursor = getContentResolver().query(selectedImage,
                            filePathColumn, null, null, null);
                    cursor.moveToFirst();

                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    String picturePath = cursor.getString(columnIndex);
                    Log.e(TAG, picturePath);
                    cursor.close();

                    SetResultBitmap(picturePath);
                }
                break;
            case CODE_REQUEST_LOGIN:
                if(data!=null) {
                    String name = data.getExtras().getString("username");
                    user_info.setText(name);
                }
                    break;
        }


    }
}
