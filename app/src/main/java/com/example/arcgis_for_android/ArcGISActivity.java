package com.example.arcgis_for_android;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.*;
import android.widget.*;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.arcgis.until.GoogleMapLayer;
import com.arcgis.until.MapUtil;
import com.arcgis.until.ToastUtil;
import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.layers.WebTiledLayer;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.view.*;
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class ArcGISActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;

    /**
     * Android中实现点击两次返回按键退出应用的功能
     */
    private FragmentManager manager = getSupportFragmentManager();
    private long firstTime;// 记录点击返回时第一次的时间毫秒值
    /**
     * 重写该方法，判断用户按下返回按键的时候，执行退出应用方法
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){// 点击了返回按键
            if(manager.getBackStackEntryCount() != 0){
                manager.popBackStack();
            }else {
                exitApp(2000);// 退出应用
            }
            return true;// 返回true，防止该事件继续向下传播
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 退出应用
     * @param timeInterval 设置第二次点击退出的时间间隔
     */
    private void exitApp(long timeInterval) {
        // 第一次肯定会进入到if判断里面，然后把firstTime重新赋值当前的系统时间
        // 然后点击第二次的时候，当点击间隔时间小于2s，那么退出应用；反之不退出应用
        if(System.currentTimeMillis() - firstTime >= timeInterval){
            ToastUtil.showShortToast( "再按一次退出程序");
            firstTime = System.currentTimeMillis();
        }else {
            finish();// 销毁当前activity
            System.exit(0);// 完全退出应用
        }
    }

    /**
     * 抽屉菜单
     */
//    private static final String TAG = "MainActivity";
//    private Context mContext;
//    private DrawerLayout mDlMain;
//    private FrameLayout mFlContent;
//    private RelativeLayout mRlLeft, mRlRight;
//    private ListView mLvLeft;
//    private TextView mTvRight;
//    private String[] leftMenuNames = {"left_item1", "left_item2", "left_item3", "left_item4"};


    private MapView mMapView;
    private GraphicsOverlay graphicsOverlay = new GraphicsOverlay();

    public ArcGISActivity() {
    }


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_arc_gis);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setOnMenuItemClickListener(onMenuItemClick);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow,
                R.id.nav_tools, R.id.nav_share, R.id.nav_send)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
    /*
        调用ArcGIS服务
        mMapView = findViewById(R.id.mapView);
        if (mMapView != null) {
            ArcGISTiledLayer layer = new ArcGISTiledLayer("http://bjtljlkj.in.3322.org:33077/imgserver");
            Basemap basemap = new Basemap(layer);
            ArcGISMap map = new ArcGISMap(basemap);
        }
    */

    /*
        //天地图
        mMapView = findViewById(R.id.mapView);
        if (mMapView != null) {
            final WebTiledLayer webTiledLayer = TianDiTuMethodsClass.CreateTianDiTuTiledLayer(TianDiTuMethodsClass.LayerType.TIANDITU_VECTOR_MERCATOR);
            webTiledLayer.loadAsync();
            ArcGISMap map = new ArcGISMap(new Basemap(webTiledLayer));
            Point center = new Point(113.365548756,23.12648183, SpatialReference.create(4490));// CGCS2000
            mMapView.setViewpointCenterAsync(center,12);
            mMapView.setMap(map);
        }
    */

        //Google地图
        mMapView = findViewById(R.id.mapView);
        if (mMapView != null) {
            final WebTiledLayer webTiledLayer = GoogleMapLayer.CreateGoogleLayer(GoogleMapLayer.MapType.SATELLITE);
            webTiledLayer.loadAsync();
            ArcGISMap map = new ArcGISMap(new Basemap(webTiledLayer));
            mMapView.getGraphicsOverlays().add(graphicsOverlay);
            mMapView.setMap(map);
            ToastUtil.showShortToast("地图定位中...");
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Point point = new Point(107.40,33.42, SpatialReferences.getWgs84());
                    mMapView.setViewpointCenterAsync(point, 50000000);//第二个参数为缩放比例
                }
            }, 2000);
        }

        //定位按钮
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        //显示信息
        mMapView.setOnTouchListener(new DefaultMapViewOnTouchListener(this,mMapView){
            public boolean onSingleTapConfirmed(MotionEvent e) {
                final android.graphics.Point screenPoint = new android.graphics.Point((int) e.getX(), (int) e.getY());


                // identify graphics on the graphics overlay
                final ListenableFuture<IdentifyGraphicsOverlayResult> identifyGraphic =
                        mMapView.identifyGraphicsOverlayAsync(graphicsOverlay, screenPoint, 10.0, false, 2);

                identifyGraphic.addDoneListener(new Runnable() {

                    public void run() {
                        try {
                            IdentifyGraphicsOverlayResult grOverlayResult = identifyGraphic.get();
                            // get the list of graphics returned by identify graphic overlay
                            List<Graphic> graphics = grOverlayResult.getGraphics();
                            Callout mCallout = mMapView.getCallout();

                            if (mCallout.isShowing()) {
                                mCallout.dismiss();
                            }

                            if (!graphics.isEmpty()) {
                                // get callout, set content and show
                                String city = graphics.get(0).getAttributes().get("station").toString();
                                String country = graphics.get(0).getAttributes().get("device").toString();
                                TextView calloutContent = new TextView(getApplicationContext());
                                calloutContent.setText(city + ", " + country);
                                Point mapPoint = mMapView.screenToLocation(screenPoint);

                                mCallout.setLocation(mapPoint);
                                mCallout.setContent(calloutContent);
                                mCallout.show();

                            }
                        } catch (InterruptedException | ExecutionException ie) {
                            ie.printStackTrace();
                        }

                    }
                });

                return super.onSingleTapConfirmed(e);
            }
        });

        /**
         * 抽屉菜单
         */
//        mContext = this;
//        initView();
    }

    private Toolbar.OnMenuItemClickListener onMenuItemClick = new Toolbar.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.action_settings_1:
                    ToastUtil.showLongToast("地图定位中...");
                    Point marker = new Point(117.1681234, 39.1590724, SpatialReferences.getWgs84());
                    SimpleMarkerSymbol sms = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE,  Color.RED, 20);
                    Map attributes = new HashMap();
                    attributes.put("station","天津西");
                    attributes.put("device","S19");
                    Graphic g = new Graphic(marker, attributes, sms );
                    graphicsOverlay.getGraphics().add(g);
                    g.setSelected(true);
                    Point point = new Point(117.1681234,39.1590724, SpatialReferences.getWgs84());
                    mMapView.setViewpointCenterAsync(point, 12000);//第二个参数为缩放比例
                    break;
                case R.id.action_settings_2:
                    if (MapUtil.isGdMapInstalled()) {
                        MapUtil.openGaoDeNavi(ArcGISActivity.this, 0, 0, null, 39.1590724, 117.1681234, "天津西S19");
                    } else {
                        //这里必须要写逻辑，不然如果手机没安装该应用，程序会闪退，这里可以实现下载安装该地图应用
                        Toast.makeText(ArcGISActivity.this, "尚未安装高德地图", Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
            return true;
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_main, menu);
        getMenuInflater().inflate(R.menu.main2, menu);
        return true;
    }



    @Override
    protected void onPause(){
        mMapView.pause();
        super.onPause();
    }

    @Override
    protected void onResume(){
        super.onResume();
        mMapView.resume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.dispose();
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
    /**
     * 初始化抽屉菜单
     */
//    private void initView() {
//        mDlMain = (DrawerLayout) findViewById(R.id.activity_main);
//        mFlContent = (FrameLayout) findViewById(R.id.fl_content);
//        mRlLeft = (RelativeLayout) findViewById(R.id.rl_left);
//        mRlRight = (RelativeLayout) findViewById(R.id.rl_right);
//        mLvLeft = (ListView) findViewById(R.id.lv_left);
//        mLvLeft.setAdapter(new ArrayAdapter<String>(mContext, android.R.layout.simple_list_item_1, leftMenuNames));//给左边菜单写入数据
//        mTvRight = (TextView) findViewById(R.id.tv_right);
//        mTvRight.setText("right_content");//给右边菜单内容赋值
//    }
}
