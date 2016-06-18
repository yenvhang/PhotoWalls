package com.example.eggache.photowalls.Activity;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
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
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.example.eggache.photowalls.Adapter.OnHideViewClickListener;
import com.example.eggache.photowalls.Adapter.OnItemClickListener;
import com.example.eggache.photowalls.Adapter.RecyclePhotoAdapter;
import com.example.eggache.photowalls.Adapter.RecyclerAlbumAdapter;
import com.example.eggache.photowalls.Model.AlbumModel;
import com.example.eggache.photowalls.Model.ImageModel;
import com.example.eggache.photowalls.MyApplication;
import com.example.eggache.photowalls.R;
import com.example.eggache.photowalls.Service.MyIntentService;
import com.example.eggache.photowalls.Util.CheckFirstManager;
import com.example.eggache.photowalls.Util.ImageLoader;
import com.example.eggache.photowalls.Util.LoginManager;
import com.example.eggache.photowalls.Util.SourcePath;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.squareup.picasso.Picasso;

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

public class MainActivity extends ToolBarActivity implements View.OnClickListener {
    private static final String TAG = "MainActivity";
    RecyclerView recyclerView;

    public int width;
    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    SwipeRefreshLayout mSwipeRefreshLayout;
    public final static String CODE_URL = "url";
    public final static String CODE_PIC_HEIGHT = "photo_height";
    public static final String CODE_PIC_WIDTH="photo_width";
    public static final String CODE_PIC_POSITION="photo_position";
    private RecyclePhotoAdapter recyclePhotoAdapter;

    public static final int  CODE_REQUEST_CAMERA      = 1;
    private static final int CODE_REQUEST_PHOTO       = 2;
    private static final int CODE_REQUEST_LOGIN       = 3;
    private static final int CODE_REQUEST_FACE_CAMERA = 4;
    private static final int CODE_REQUEST_FACE_ALBUM  = 5;
    public static final int  CODE_REQUEST_CLIP =6;

    private static List<AlbumModel> albumModels = new ArrayList<>();
    private static final String URL = "192.168.155.1";

    private MaterialDialog upload;

    private File filepath;
    private Spinner spinner;
    private RecyclerAlbumAdapter recyclerAlbumAdapter;
    private int MODEL = 0;//按图片查看
    private TextView user_info;
    private LoginManager loginManager;


    private Response.Listener<String> createAlbumSuccessListener;
    private int albumPosition;
    private SwipeRefreshLayout.OnRefreshListener refreshListener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            Refresh();
        }
    };
    private int pageNum = 0;
    private FloatingActionButton uploadButton;
    private StaggeredGridLayoutManager staggeredGridLayoutManager;
    private PopupWindow popupwindow;
    public static final String PHOTO_PATH_KEY ="photo_path";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent =new Intent(this,MyIntentService.class);
        startService(intent);
        this.width =super.width/2+2;
        initView();
        setUpRecyclerView();
        setUpSnackbar();
        setUpFloatingActionsMenu();
        setUpRefreshLayout();
    }
    private void setUpRefreshLayout() {
        mSwipeRefreshLayout.setOnRefreshListener(refreshListener);

        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(true);

            }
        });
        Refresh();
    }

    private void setUpFloatingActionsMenu() {
        //上传图片功能入口
        uploadButton.setTitle("上传图片");
        uploadButton.setIcon(R.drawable.upload);
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if ((loginManager.getLoginInfo()) == null) {
                    loginManager.login(MainActivity.this);

                    return;

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
                        from(MainActivity.this).inflate(R.layout.upload_select_view,null);
                mMaterialDialog.setContentView(linearLayout);
                mMaterialDialog.show();

                final Button fromCamera = (Button) linearLayout.findViewById(R.id.btn_camera);
                final Button fromPhoto = (Button) linearLayout.findViewById(R.id.btn_photo);
                final Button fromNet = (Button) linearLayout.findViewById(R.id.btn_net);
                //从相机获取图片
                fromCamera.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getImageFromCamera(CODE_REQUEST_CAMERA);
                        mMaterialDialog.dismiss();
                    }
                });
                //从相册获取图片
                fromPhoto.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getImageFromLocalAlbum(CODE_REQUEST_PHOTO);
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

    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setUpDrawerLayout() {
        setupDrawerContent(mNavigationView);
        RelativeLayout relativeLayout = (RelativeLayout) mNavigationView.getHeaderView(0);
        ImageView img_login = (ImageView) relativeLayout.findViewById(R.id.IV_login);


        user_info = (TextView) relativeLayout.findViewById(R.id.id_username);
        img_login.setOnClickListener(this);
        loginManager = LoginManager.getInstance();

        String username;
        if ((username = loginManager.getLoginInfo()) != null) {
            user_info.setText(username);

        } else {
            user_info.setText(loginManager.getDefaultInfo());
        }


        Picasso.with(this).load(loginManager.geturl()).placeholder(R.drawable.defaulthead).into(img_login);

        img_login.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                mDrawerLayout.closeDrawers();
                setUpFacePopupWindown();
                return false;
            }
        });

    }

    private void setUpFacePopupWindown() {
        Log.e(TAG,"POPUPWINDOW");
        View contentView =LayoutInflater.from(this).inflate(R.layout.popupwindow_face_select_way,null);
        if(popupwindow!=null){
            popupwindow.dismiss();
        }
        popupwindow =new PopupWindow(this);
        popupwindow.setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);
        popupwindow.setWidth(LinearLayout.LayoutParams.MATCH_PARENT);
        popupwindow.setContentView(contentView);
        popupwindow.setBackgroundDrawable(new ColorDrawable(0x00000000));
        popupwindow.setOutsideTouchable(true);
        popupwindow.setFocusable(true);
        popupwindow.showAtLocation(recyclerView, Gravity.BOTTOM,0,0);

        contentView.findViewById(R.id.TV_from_camera).setOnClickListener(this);
        contentView.findViewById(R.id.TV_from_photo).setOnClickListener(this);
        contentView.findViewById(R.id.TV_cancel).setOnClickListener(this);
    }

    private void setUpToolBar() {

        ActionBarDrawerToggle mDrawerToggler =new ActionBarDrawerToggle(
                this,mDrawerLayout,toolbar,
                R.string.action_bar_home_description,
                R.string.action_bar_home_description_format);
        mDrawerToggler.syncState();
        mDrawerLayout.setDrawerListener(mDrawerToggler);

    }

    private void setUpSnackbar() {
        new CheckFirstManager().isFirstLaunch("guide_on_recyclerView", new CheckFirstManager.callback() {
            @Override
            public void first() {
                Snackbar.make(recyclerView, "点击图片可查看图片，长按图片可修改信息", Snackbar.LENGTH_INDEFINITE)
                        .setAction("知道了", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                            }
                        }).show();
            }
        });

    }

    private void loadMore() {
        mSwipeRefreshLayout.setRefreshing(true);
        if(MODEL==0) {
            getImageResourceFromNet();
        }
        else{
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }

    private int getLastCompletelyVisiblePosition() {

        int[] lastPosition = staggeredGridLayoutManager.findLastCompletelyVisibleItemPositions(new int[staggeredGridLayoutManager.getSpanCount()]);
        return getMaxPosition(lastPosition);
    }


    private void deleteAlbum(final int position) {
        final ProgressDialog progressDialog =new ProgressDialog(this);
        progressDialog.show();
        StringRequest deleteAlbumRequest = new StringRequest(Request.Method.POST, "http://" + URL + ":8080/PhotoBase/users/deletealbum", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                    progressDialog.dismiss();
                    recyclerAlbumAdapter.notifyItemChanged(position);
                    recyclerAlbumAdapter.getAlbumModels().remove(position);
                    recyclerAlbumAdapter.notifyItemRangeChanged(position, recyclerAlbumAdapter.getItemCount());

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> maps = new HashMap<>();
                maps.put("name", recyclerAlbumAdapter.getAlbumModels().get(position).getName());
                return maps;
            }
        };
        mQueue.add(deleteAlbumRequest);
        mQueue.start();

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


                                     albumodel.setName(object.getString("name"));
                                     albumodel.setCreateby(object.getString("createby"));
                                     albumodel.setDescription(object.getString("description"));
                                     albumodel.setUrl(object.getString("url"));
                                     if (!recyclerAlbumAdapter.getAlbumModels().contains(albumodel))
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
        JsonArrayRequest getImageSourceJSONArrayRequest = new JsonArrayRequest("http://" + URL + ":8080/PhotoBase/pictures/getpictures?pageNum=" + pageNum, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                if (response.length() != 0)
                    pageNum++;
                for (int i = 0; i < response.length(); i++) {

                    try {
                        JSONObject object = response.getJSONObject(i);

                        newImageModel(recyclePhotoAdapter, object.getInt("id"), (String) object.get("url"),
                                (String) object.get("description"),
                                object.getInt("width"),
                                object.getInt("height"),object.getJSONObject("albums").getString("name"));

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }

                recyclePhotoAdapter.notifyDataSetChanged();
                mSwipeRefreshLayout.setRefreshing(false);

            }


        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, "网络连接错误", Toast.LENGTH_LONG).show();
                mSwipeRefreshLayout.setRefreshing(false);

            }
        });
        getImageSourceJSONArrayRequest.setTag(this);
        mQueue.add(getImageSourceJSONArrayRequest);
    }


    private void Refresh() {
        pageNum = 0;
        if (MODEL == 0) {
            makeFlowView(2);
            pageNum = 0;
            recyclePhotoAdapter.getImageModels().clear();
            getImageResourceFromNet();

        } else if (MODEL == 1) {

            makeFlowView(2);
            getAlbumResourceFromNet();
        } else {
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

        mDrawerLayout = (DrawerLayout) findViewById(R.id.id_drawer_layout);
        mNavigationView = (NavigationView) findViewById(R.id.id_nv_menu);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.activity_main_swipe_refresh_layout);
        recyclerView = (RecyclerView) findViewById(R.id.recycleview_photo);
        uploadButton = (FloatingActionButton) findViewById(R.id.action_a);

    }

    private void setUpRecyclerView() {
        makeFlowView(2);
        recyclePhotoAdapter = new RecyclePhotoAdapter(MainActivity.this, width);
        recyclerView.setAdapter(recyclePhotoAdapter);
        recyclerView.addOnScrollListener(getOnScrollListenerOnRecyclerView());
        recyclePhotoAdapter.setOnItemClickLitener(getOnItemClickListenerOnRecyclerView());
        recyclePhotoAdapter.setOnHideViewClickListener(getOnHideViewClickListener());
    }

    private OnHideViewClickListener getOnHideViewClickListener() {
        return new OnHideViewClickListener() {
            @Override
            public void onClick(View view, final int position) {
                LoginManager loginManager = LoginManager.getInstance();

                switch (view.getId()){
                    case R.id.btn_update_album_photo:

                        if (loginManager.getLoginInfo() == null) {
                            loginManager.login(MainActivity.this);
                            return;
                        }
                            int id =recyclePhotoAdapter.getImageModels().get(position).getId();
                            String album =recyclePhotoAdapter.getImageModels().get(position).getAlbum();

                            updateAlbumPhotoTask(id,album);
                            break;
                    case R.id.TV_photoManager_delet:
                        if (loginManager.getLoginInfo() == null) {
                            loginManager.login(MainActivity.this);
                            return;
                        }
                        final MaterialDialog mMaterialDialog =new MaterialDialog(MainActivity.this);
                        showConfirmDialog(position, "确定删除图片？", mMaterialDialog, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                deleteImageTask(position);
                                mMaterialDialog.dismiss();
                            }
                        });

                        break;
                    case R.id.TV_photoManager_update:
                        if (loginManager.getLoginInfo() == null) {
                            loginManager.login(MainActivity.this);
                            return;
                        }
                        final MaterialDialog updateDialog = new MaterialDialog(MainActivity.this);
                        final EditText editText = new EditText(MainActivity.this);
                        editText.setText(recyclePhotoAdapter.getImageModels().get(position).getDescription());
                        updateDialog.setContentView(editText);


                        updateDialog.setPositiveButton("确定", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                updatePhotoInfoTask(position, editText.getText().toString());
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
                        break;
                }

            }
        };
    }

    private void showConfirmDialog(final int position, String msg, final MaterialDialog mMaterialDialog, View.OnClickListener OKListener) {

        mMaterialDialog.setTitle("确认信息")
                .setMessage(msg)
                .setPositiveButton("确定",OKListener)
                .setNegativeButton("取消", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mMaterialDialog.dismiss();

                    }
                });

        mMaterialDialog.show();
    }

    private OnItemClickListener getOnItemClickListenerOnRecyclerView() {
        final Intent intent = new Intent(MainActivity.this, ShowPhotoActivity.class);
        return new OnItemClickListener() {
            @Override
            //点击 进入图片
            public void onItemClick(View view, int position) {

                String url = recyclePhotoAdapter.getImageModels().get(position).getUrl();
                int height =recyclePhotoAdapter.getImageModels().get(position).getHeight();
                int width =recyclePhotoAdapter.getImageModels().get(position).getWidth();
                intent.putExtra(CODE_URL, url);
                intent.putExtra(CODE_PIC_HEIGHT,height);
                intent.putExtra(CODE_PIC_WIDTH,width);
                intent.putExtra(CODE_PIC_POSITION,position);
                startActivity(intent);

            }

            //管理图片
            @Override
            public void onItemLongClick(View view, final int position) {
                LoginManager loginManager = LoginManager.getInstance();
                if (loginManager.getLoginInfo() == null) {
                    loginManager.login(MainActivity.this);
                    return;
                }

                final MaterialDialog photoManagerDialog = new MaterialDialog(MainActivity.this);
                LinearLayout linearlayout = (LinearLayout) LayoutInflater.from(MainActivity.this).inflate(R.layout.dialog_photomanager, null);
                Button update_abbum_photo = (Button) linearlayout.findViewById(R.id.btn_update_album_photo);
                Button delete = (Button) linearlayout.findViewById(R.id.TV_photoManager_delet);
                Button update = (Button) linearlayout.findViewById(R.id.TV_photoManager_update);
                Button cancel = (Button) linearlayout.findViewById(R.id.TV_photoManager_cancel);
                photoManagerDialog.setContentView(linearlayout);
                photoManagerDialog.show();

                update_abbum_photo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int id =recyclePhotoAdapter.getImageModels().get(position).getId();
                        String album =recyclePhotoAdapter.getImageModels().get(position).getAlbum();
                        updateAlbumPhotoTask(id,album);
                        photoManagerDialog.dismiss();
                    }
                });

                delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final MaterialDialog mMaterialDialog =new MaterialDialog(MainActivity.this);
                        showConfirmDialog(position, "确定删除图片？",mMaterialDialog, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                deleteImageTask(position);
                                mMaterialDialog.dismiss();
                            }
                        });
                        photoManagerDialog.dismiss();
                    }
                });

                update.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final MaterialDialog updateDialog = new MaterialDialog(MainActivity.this);
                        final EditText editText = new EditText(MainActivity.this);
                        editText.setText(recyclePhotoAdapter.getImageModels().get(position).getDescription());
                        updateDialog.setContentView(editText);


                        updateDialog.setPositiveButton("确定", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                updatePhotoInfoTask(position, editText.getText().toString());
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
        };
    }

    private void updateAlbumPhotoTask(final int id, final String album) {
        final ProgressDialog ProgressDialog =new ProgressDialog(this);
        ProgressDialog.show();
        StringRequest request =new StringRequest(Request.Method.POST,  "http://" + URL + ":8080/PhotoBase/users/updatealbumphoto", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                ProgressDialog.dismiss();
                showMessageDialog("设置成功");

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                showMessageDialog("设置失败");
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> maps =new HashMap<>();
                maps.put("id", String.valueOf(id));
                maps.put("album",album);

                return maps;
            }
        };
        mQueue.add(request);
        mQueue.start();

    }

    private void showMessageDialog(String msg) {
        final MaterialDialog MaterialDialog =new MaterialDialog(MainActivity.this);
        MaterialDialog.setMessage(msg);
        MaterialDialog.setPositiveButton("确定", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MaterialDialog.dismiss();
            }
        });
        MaterialDialog.show();
    }


    private RecyclerView.OnScrollListener getOnScrollListenerOnRecyclerView() {

        return new RecyclerView.OnScrollListener() {



            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (getLastCompletelyVisiblePosition() + 1 == recyclePhotoAdapter.getItemCount()
                        && !mSwipeRefreshLayout.isRefreshing()) {

                    loadMore();


                }
            }
        };
    }

    private void makeFlowView(int columns) {
        staggeredGridLayoutManager =
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
                                "http://" + URL + ":8080/PhotoBase/users/create", createAlbumSuccessListener, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {

                                Toast.makeText(MainActivity.this, "网络连接错误", Toast.LENGTH_LONG).show();
                            }
                        }) {
                            @Override
                            protected Map<String, String> getParams() throws AuthFailureError {
                                Map<String, String> map = new HashMap<>();
                                map.put("name", name);
                                map.put("createby", "creep");
                                map.put("description", description);
                                return map;
                            }

                            @Override
                            public Map<String, String> getHeaders() throws AuthFailureError {
                                Map<String, String> headers = new HashMap<>();
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
            }
        });
        createAlbum.setNegativeButton("取消", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createAlbum.dismiss();
            }
        });
        return createAlbum;
    }



    /*
    从相机获取图片
     */
    private void getImageFromCamera(int code) {

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
            startActivityForResult(intent, code);
        } else {
            Toast.makeText(MainActivity.this, "请确认已经插入SD卡", Toast.LENGTH_LONG).show();
        }

    }

    /*
    从相册获取图片
     */
    private void getImageFromLocalAlbum(int code) {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, code);
    }

    /*
    设置要加载的图片属性
     */
    private void newImageModel(RecyclePhotoAdapter recyclePhotoAdapter, int id, String url, String description, int width, int height,String album) {
        //       Random random = new Random();
//        int height = (random.nextInt(5) + 5) * 100;
        ImageModel imageModel = new ImageModel();
        imageModel.setUrl(url);
        imageModel.setId(id);
        imageModel.setWidth(width);
        imageModel.setHeight(height);
        imageModel.setDescription(description);
        imageModel.setAlbum(album);
        Log.e(TAG,imageModel.getUrl());
        boolean isExist = false;
        for (int i = 0; i < recyclePhotoAdapter.getImageModels().size(); i++) {
            if (recyclePhotoAdapter.getImageModels().get(i).getId() == imageModel.getId()) {

                isExist = true;
                break;
            }
        }
        if (!isExist) {

            recyclePhotoAdapter.addDrawable(imageModel);
        }

    }


    /*
    上传图片
     */
    private void SetResultBitmap(final String picturePath) {


        upload = new MaterialDialog(this);
        final ScrollView scrollView = (ScrollView) LayoutInflater.from(this).inflate(R.layout.upload_send_view, null);
        upload.setContentView(scrollView);

        ImageView imageview = (ImageView) scrollView.findViewById(R.id.IV_selectedImage);
        int requiredWidth = width;
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


                    createAlbumSuccessListener = new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            getAlbumTask(getSpinnerAlbumListener);
                        }
                    };
                    final MaterialDialog createAlbum = createAlbumDialog();
                    createAlbum.show();


                    isfirst = false;

                } else {
                    try {
                        setSelectedAlbum((arrayAdapter.getItem(position)).toString());
                    } catch (Exception e) {
                        Log.e(TAG, Log.getStackTraceString(e));
                    }
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
                uploadImageTask(description, album, picturePath);
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

    }

    private void deleteImageTask(final int position) {
        final ImageModel photo = recyclePhotoAdapter.getImageModels().get(position);
        StringRequest deletePhotoReques = new StringRequest(Request.Method.POST, "http://" + URL + ":8080/PhotoBase/users/deleltephoto", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                recyclePhotoAdapter.notifyItemRemoved(position);
                recyclePhotoAdapter.getImageModels().remove(position);
                recyclePhotoAdapter.notifyItemRangeChanged(position, recyclePhotoAdapter.getItemCount());

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, "网络连接错误", Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> maps = new HashMap();
                maps.put("id", photo.getId() + "");
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

    private void updatePhotoInfoTask(final int position, final String description) {
        final ImageModel photo = recyclePhotoAdapter.getImageModels().get(position);
        StringRequest updatePhotoRequest = new StringRequest(Request.Method.POST, "http://" + URL + ":8080/PhotoBase/users/uodatephoto", new Response.Listener<String>() {
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
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> maps = new HashMap<>();

                maps.put("id", photo.getId() + "");
                maps.put("description", description);
                return maps;
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

    /**
     * 获得最大的位置
     *
     *
     *
     */
    private int getMaxPosition(int[] positions) {

        int maxPosition = Integer.MIN_VALUE;
        for (int position : positions) maxPosition = Math.max(maxPosition, position);
        return maxPosition;
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
                recyclerAlbumAdapter = new RecyclerAlbumAdapter(mQueue, width);
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

        recyclerAlbumAdapter.setOnHideViewClickListener(new OnHideViewClickListener() {
            @Override
            public void onClick(View view, final int position) {
                switch (view.getId()) {
                    case R.id.btn_addAlbum:
                        if ((loginManager.getLoginInfo()) == null) {
                            loginManager.login(MainActivity.this);

                            return;

                        }
                        createAlbumSuccessListener = new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                getAlbumResourceFromNet();
                            }
                        };
                        MaterialDialog createAlbumDialog = createAlbumDialog();
                        createAlbumDialog.show();
                        break;
                    case R.id.btn_deleteAlbum:
                        if ((loginManager.getLoginInfo()) == null) {
                            loginManager.login(MainActivity.this);

                            return;

                        }
                        final MaterialDialog MaterialDialog =new MaterialDialog(MainActivity.this);
                        showConfirmDialog(position, "删除相册将删除相册内的全部图片！",MaterialDialog , new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                deleteAlbum(position);
                                MaterialDialog.dismiss();
                            }
                        });

                        break;
                    case R.id.btn_updateAlbum:
                        if ((loginManager.getLoginInfo()) == null) {
                            loginManager.login(MainActivity.this);

                            return;

                        }
                        final MaterialDialog updateAlbumDialog = new MaterialDialog(MainActivity.this);
                        LinearLayout linearLayout = (LinearLayout) LayoutInflater.from(MainActivity.this).inflate(R.layout.dialog_updatealbum, null);
                        final EditText etAlbumName = (EditText) linearLayout.findViewById(R.id.ETalbum_name);
                        final EditText etDescription = (EditText) linearLayout.findViewById(R.id.ETalbum_description);

                        final AlbumModel albumModel = recyclerAlbumAdapter.getAlbumModels().get(position);
                        final String name = albumModel.getName();
                        final String description = albumModel.getDescription();
                        etAlbumName.setText(name);
                        etDescription.setText(description);
                        updateAlbumDialog.setContentView(linearLayout);


                        updateAlbumDialog.setPositiveButton("提交更新", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                if (etAlbumName.getText().toString().equals(name)
                                        && etDescription.getText().toString().equals(description)) {
                                    Toast.makeText(MainActivity.this, "没有任何更改", Toast.LENGTH_LONG).show();
                                    updateAlbumDialog.dismiss();
                                } else {


                                    albumModel.setName(etAlbumName.getText().toString());
                                    albumModel.setDescription(etDescription.getText().toString());
                                    updateAlbumInfoTask(albumModel, position,name);


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

                        break;
                }
            }
        });

        recyclerAlbumAdapter.setOnItemClickLitener(new OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                recyclerView.removeAllViews();
                recyclePhotoAdapter.getImageModels().clear();
                getImageFromAlbum(position);

                albumPosition = position;

                MODEL = 2;


            }


            @Override
            public void onItemLongClick(View view, final int position) {
                Toast.makeText(MainActivity.this, position + "", Toast.LENGTH_SHORT).show();
                final MaterialDialog albumManagerDialog = new MaterialDialog(MainActivity.this);
                LinearLayout linearLayout = (LinearLayout) LayoutInflater.from(MainActivity.this).inflate(R.layout.dialog_albummanager, null);
                albumManagerDialog.setContentView(linearLayout);
                albumManagerDialog.show();
                Button add = (Button) linearLayout.findViewById(R.id.btn_addAlbum);
                Button delete = (Button) linearLayout.findViewById(R.id.btn_deleteAlbum);
                Button update = (Button) linearLayout.findViewById(R.id.btn_updateAlbum);
                Button cancel = (Button) linearLayout.findViewById(R.id.btn_cancel);
                add.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        createAlbumSuccessListener = new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                getAlbumResourceFromNet();
                            }
                        };
                        MaterialDialog createAlbumDialog = createAlbumDialog();
                        createAlbumDialog.show();
                        albumManagerDialog.dismiss();

                    }
                });
                delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {


                        try {

                            deleteAlbum(position);

                            albumManagerDialog.dismiss();
                        } catch (Exception e) {
                            Log.e(TAG, Log.getStackTraceString(e));
                        }

                    }
                });
                try {
                    update.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            final MaterialDialog updateAlbumDialog = new MaterialDialog(MainActivity.this);
                            LinearLayout linearLayout = (LinearLayout) LayoutInflater.from(MainActivity.this).inflate(R.layout.dialog_updatealbum, null);
                            final EditText etAlbumName = (EditText) linearLayout.findViewById(R.id.ETalbum_name);
                            final EditText etDescription = (EditText) linearLayout.findViewById(R.id.ETalbum_description);

                            final AlbumModel albumModel = recyclerAlbumAdapter.getAlbumModels().get(position);
                            final String name = albumModel.getName();
                            final String description = albumModel.getDescription();
                            etAlbumName.setText(name);
                            etDescription.setText(description);
                            updateAlbumDialog.setContentView(linearLayout);


                            updateAlbumDialog.setPositiveButton("提交更新", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    if (etAlbumName.getText().toString().equals(name)
                                            && etDescription.getText().toString().equals(description)) {
                                        Toast.makeText(MainActivity.this, "没有任何更改", Toast.LENGTH_LONG).show();
                                        updateAlbumDialog.dismiss();
                                    } else {


                                        albumModel.setName(etAlbumName.getText().toString());
                                        albumModel.setDescription(etDescription.getText().toString());
                                        updateAlbumInfoTask(albumModel, position,name);


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
                } catch (Exception e) {
                    Log.e(TAG, Log.getStackTraceString(e));
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

    private void updateAlbumInfoTask(final AlbumModel albumModel, final int position, final String name) {
        StringRequest updateAlbumRequest = new StringRequest(Request.Method.POST, "http://" + URL + ":8080/PhotoBase/users/updatealbum", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {


                recyclerAlbumAdapter.notifyItemChanged(position);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> maps = new HashMap<>();
                maps.put("updatename",name);
                Log.e(TAG,name+"!!");
                maps.put("name", albumModel.getName());
                maps.put("description", albumModel.getDescription());
                maps.put("createtime", albumModel.getTimestamp() + "");
                maps.put("createby", albumModel.getCreateby());
                maps.put("url", albumModel.getUrl());

                return maps;
            }
        };
        mQueue.add(updateAlbumRequest);
    }


    private void getImageFromAlbum(int position) {
        AlbumModel albumModel = recyclerAlbumAdapter.getAlbumModels().get(position);
        final String album = albumModel.getName();


        String baseurl = "http://" + URL + ":8080/PhotoBase/pictures/getpicturesbualbum";

        String url = null;
        try {
            url = baseurl + "?album=" + URLEncoder.encode(album, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        final JsonArrayRequest requestPhotoInAlbum =
                new JsonArrayRequest(url, new Response.Listener<JSONArray>() {


                    public void onResponse(JSONArray response) {
                        recyclePhotoAdapter.getImageModels().clear();
                        Log.e(TAG,recyclePhotoAdapter.getImageModels().size()+"size");
                        for (int i = 0; i < response.length(); i++) {
                            try {
                                JSONObject object = response.getJSONObject(i);
                                newImageModel(recyclePhotoAdapter, object.getInt("id"),
                                        object.getString("url"), object.getString("description"),
                                        object.getInt("width"), object.getInt("height"),object.getJSONObject("albums").getString("name"));

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
                        Toast.makeText(MainActivity.this, "请求图片超时", Toast.LENGTH_LONG).show();
                    }
                }) {
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String, String> heads = new HashMap();
                        heads.put("Charset", "UTF-8");

                        return heads;
                    }
                };

        mQueue.add(requestPhotoInAlbum);


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.IV_login:
                Intent i = new Intent(MainActivity.this, LoginActivity.class);
                startActivityForResult(i, CODE_REQUEST_LOGIN);
                break;
            case R.id.TV_from_camera:
                popupwindow.dismiss();
                getImageFromCamera(CODE_REQUEST_FACE_CAMERA);

                break;
            case R.id.TV_from_photo:
                popupwindow.dismiss();
                getImageFromLocalAlbum(CODE_REQUEST_FACE_ALBUM);

                break;
            case R.id.TV_cancel:
                popupwindow.dismiss();
                break;

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case CODE_REQUEST_CAMERA:
                if (resultCode == RESULT_OK) {
                        SetResultBitmap(filepath.getAbsolutePath());
                }
                break;
            case CODE_REQUEST_PHOTO:
                if (resultCode == RESULT_OK) {
                    String picturePath = GetImagePathFromAlbum(data);
                    SetResultBitmap(picturePath);
                }
                break;
            case CODE_REQUEST_FACE_CAMERA:

                if(resultCode== RESULT_OK){
                    Log.e(TAG,"1");
                    Intent i =new Intent(MainActivity.this,ClipImageActivity.class);
                    Log.e(TAG,"2");
//                    i.putExtra(PHOTO_PATH_KEY,filepath.getAbsolutePath());
                    String picturepath =filepath.getAbsolutePath();

                    i.putExtra(PHOTO_PATH_KEY,picturepath);
                    Log.e(TAG,"3");
                  startActivityForResult(i,CODE_REQUEST_CLIP);
                    Log.e(TAG,"4");
                }
                break;
            case CODE_REQUEST_FACE_ALBUM:
                Log.e(TAG,"1");
                if(resultCode== RESULT_OK){
                    Log.e(TAG,"2");
                    String picturePath = GetImagePathFromAlbum(data);
                    Log.e(TAG,"3");
                    Intent i =new Intent(MainActivity.this,ClipImageActivity.class);
                    i.putExtra(PHOTO_PATH_KEY,picturePath);
                    startActivityForResult(i,CODE_REQUEST_CLIP);
                }
                break;

            case CODE_REQUEST_CLIP:

                if(resultCode ==RESULT_OK) {
                    final ProgressDialog progressDialog = new ProgressDialog(this);
                    progressDialog.show();
                    String photo =MyApplication.getInstace().getPhoto();
                    RequestParams params = new RequestParams();
                    String username =LoginManager.getInstance().getLoginInfo();
                    params.put("photo", photo);
                    params.put("username",username);
                    String url = "http://" + URL + ":8080/PhotoBase/users/uploadface";

                    AsyncHttpClient client = new AsyncHttpClient();
                    client.post(url, params, new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                            LoginManager.getInstance().seturl(new String(responseBody));
                            progressDialog.dismiss();
                            setUpDrawerLayout();
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                            Toast.makeText(MainActivity.this,"访问异常",Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }

                    });
                }
                break;
            case CODE_REQUEST_LOGIN:
                if (data != null) {
                    String name = data.getExtras().getString("username");
                    user_info.setText(name);
                }
                break;
        }


    }

    private String GetImagePathFromAlbum(Intent data) {
        Uri selectedImage = data.getData();
        final String[] filePathColumn = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(selectedImage,
                filePathColumn, null, null, null);
        assert cursor != null;
        cursor.moveToFirst();
        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        String picturePath = cursor.getString(columnIndex);
        cursor.close();
        return picturePath;
    }

    @Override
    protected void onRestart() {
        Log.e(TAG, "onRestart");

        super.onRestart();
    }

    @Override
    protected void onResume() {
        Log.e(TAG, "onResume");
        mQueue.start();
        setUpDrawerLayout();

        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.e(TAG, "onPause");
        mQueue.stop();
        mQueue.cancelAll(this);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mQueue.cancelAll(this);
        Log.e(TAG, "onDestroy");

        mQueue.stop();

        super.onDestroy();
    }


    @Override
    protected int provideContentViewID() {
        return R.layout.activity_main;
    }
}
