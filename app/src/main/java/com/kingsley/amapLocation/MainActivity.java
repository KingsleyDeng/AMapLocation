package com.kingsley.amapLocation;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.CircleOptions;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;


//import com.grpt.Nb.R;
//import com.grpt.Nb.utils.ToastUtils;


/**
 * 拖拽定位位置,DragLocation and drop
 */
public class MainActivity extends Activity implements
        AMap.OnMarkerClickListener,
        AMap.OnMapLoadedListener,
        AMap.OnMapClickListener,
        LocationSource,
        AMapLocationListener,
        GeocodeSearch.OnGeocodeSearchListener,
        AMap.OnCameraChangeListener {

    private MapView mMapView;
    private AMap mAMap;
    private Marker mGPSMarker;             //定位位置显示
    private AMapLocation location;
    private LocationSource.OnLocationChangedListener mListener;
    //声明AMapLocationClient类对象
    public AMapLocationClient mLocationClient = null;
    //声明mLocationOption对象
    public AMapLocationClientOption mLocationOption = null;
    //你编码对象
    private GeocodeSearch geocoderSearch;

    private String custAddr;
    private Double custLon;
    private Double custLat;
    private String actualAddr;
    private Double actualLon;
    private Double actualLat;
    private ImageView img_back;
    private String city;
    private MarkerOptions markOptions;
    private LatLng latLng;
    private String addressName;

    /**
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initMap(savedInstanceState);
        img_back = (ImageView) findViewById(R.id.img_back);
        //
        //        actualLat = getIntent().getDoubleExtra("lat", 0.00);
        //        actualLon = getIntent().getDoubleExtra("lon", 0.00);
        //        actualAddr = getIntent().getStringExtra("drr");
        //        city = getIntent().getStringExtra("city");
        //        LatLng la = new LatLng(actualLat, actualLon);
        //
        //        setMarket(la, city, custAddr);

        img_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // if(!TextUtils.isEmpty(custAddr))
                // FieldAttendanceActivity.getParameter(custAddr, custLon, custLat, actualAddr, actualLon, actualLat);
                finish();
            }
        });

    }

    private void initMap(Bundle savedInstanceState) {
        mMapView = (MapView) findViewById(R.id.map_view);
        mMapView.onCreate(savedInstanceState);
        geocoderSearch = new GeocodeSearch(this);
        mAMap = mMapView.getMap();
        // 设置定位监听
        mAMap.setOnMapLoadedListener(this);
        mAMap.setOnMarkerClickListener(this);
        mAMap.setOnMapClickListener(this);

        mAMap.setLocationSource(this);
        //设置地图拖动监听
        mAMap.setOnCameraChangeListener(this);
        // 绑定marker拖拽事件
//      mAMap.setOnMarkerDragListener(this);

        //逆编码监听事件
//              GeocodeSearch.OnGeocodeSearchListener,
        geocoderSearch.setOnGeocodeSearchListener(this);

        //导航箭头定位的小图标，这里自定义一张图片
        MyLocationStyle myLocationStyle = new MyLocationStyle();
        myLocationStyle.myLocationIcon(BitmapDescriptorFactory.fromResource(R.drawable.navigation));// 设置小蓝点的图标
        //myLocationStyle.strokeColor(Color.BLACK);// 设置圆形的边框颜色
        myLocationStyle.radiusFillColor(Color.argb(100, 0, 0, 180));// 设置圆形的填充颜色
        // myLocationStyle.anchor(int,int)//设置小蓝点的锚点
        myLocationStyle.strokeWidth(0f);// 设置圆形的边框粗细
        myLocationStyle.anchor(0.5f, 0.7f);
        mAMap.setMyLocationStyle(myLocationStyle);

        mAMap.moveCamera(CameraUpdateFactory.zoomTo(19)); //缩放比例

        //添加一个圆
        CircleOptions circleOptions = new CircleOptions();
        circleOptions.radius(20.0f);
        mAMap.addCircle(circleOptions);

        //设置amap的属性
        UiSettings settings = mAMap.getUiSettings();
        settings.setMyLocationButtonEnabled(true);// 设置默认定位按钮是否显示
        mAMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false

    }

    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        location = aMapLocation;
        if (mListener != null && location != null) {
            if (location != null && location.getErrorCode() == 0) {
                mListener.onLocationChanged(location);// 显示系统箭头

                LatLng la = new LatLng(location.getLatitude(), location.getLongitude());

                setMarket(la, location.getCity(), location.getAddress());
                this.actualAddr = location.getAddress();
                this.actualLon = location.getLongitude();
                this.actualLat = location.getLatitude();

                mLocationClient.stopLocation();
                //                this.location = location;
                // 显示导航按钮
                //                btnNav.setVisibility(View.VISIBLE);
            }
        } else {
//            ToastUtils.showToast("定位失败");
        }
    }

    /**
     * 激活定位
     */
    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        mListener = onLocationChangedListener;
        //初始化定位
        mLocationClient = new AMapLocationClient(getApplicationContext());
        //设置定位回调监听
        mLocationClient.setLocationListener(this);

        //初始化定位参数
        mLocationOption = new AMapLocationClientOption();
        //设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        //设置是否返回地址信息（默认返回地址信息）
        mLocationOption.setNeedAddress(true);
        //设置是否只定位一次,默认为false
        mLocationOption.setOnceLocation(false);
        //设置是否强制刷新WIFI，默认为强制刷新
        mLocationOption.setWifiActiveScan(true);
        //设置是否允许模拟位置,默认为false，不允许模拟位置
        mLocationOption.setMockEnable(false);
        //设置定位间隔,单位毫秒,默认为2000ms
        mLocationOption.setInterval(2000 * 10);
        //给定位客户端对象设置定位参数
        mLocationClient.setLocationOption(mLocationOption);
        //启动定位
        mLocationClient.startLocation();
    }

    /**
     * 停止定位
     */
    @Override
    public void deactivate() {
        mListener = null;
        if (mLocationClient != null) {
            mLocationClient.stopLocation();
            mLocationClient.onDestroy();
        }
    }

    @Override
    public void onMapClick(LatLng latLng) {
        //        mAMap.clear();
        //        this.custLat = latLng.latitude;
        //        this.custLon = latLng.longitude;
        //
        LatLng latLonPoint = new LatLng(latLng.latitude, latLng.longitude);
        if (!TextUtils.isEmpty(latLonPoint.toString())) {
            getAddress(latLonPoint);
        } else {
            //        ToastUtils.showToast("拜访地址获取失败");
        }
        //        MarkerOptions otMarkerOptions = new MarkerOptions();
        //        otMarkerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.bd_location_icon01));
        //        otMarkerOptions.position(latLng);
        //        mAMap.addMarker(otMarkerOptions);
        //        mAMap.moveCamera(CameraUpdateFactory.changeLatLng(latLng));
    }

    @Override
    public void onMapLoaded() {

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if (marker.isInfoWindowShown()) {
            marker.hideInfoWindow();

        } else {
            marker.showInfoWindow();
        }
        return false;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
        // aMapEx.onRegister();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 销毁定位
        if (mLocationClient != null) {
            mLocationClient.stopLocation();
            mLocationClient.onDestroy();
        }
        //        if (aMapEx != null) {
        //            aMapEx.onUnregister();
        //        }
        mMapView.onDestroy();
    }

    private void setMarket(LatLng latLng, String title, String content) {
        if (mGPSMarker != null) {
            mGPSMarker.remove();
        }
        //获取屏幕宽高
        WindowManager wm = this.getWindowManager();
        int width = (wm.getDefaultDisplay().getWidth()) / 2;
        int height = ((wm.getDefaultDisplay().getHeight()) / 2) - 80;
        markOptions = new MarkerOptions();
        markOptions.draggable(true);//设置Marker可拖动
        markOptions.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.poi_marker_pressed))).anchor(0.5f, 0.7f);
        //设置一个角标
        mGPSMarker = mAMap.addMarker(markOptions);
        //设置marker在屏幕的像素坐标
        mGPSMarker.setPosition(latLng);
        mGPSMarker.setTitle(title);
        mGPSMarker.setSnippet(content);
        //设置像素坐标
        mGPSMarker.setPositionByPixels(width, height);
        if (!TextUtils.isEmpty(content)) {
            mGPSMarker.showInfoWindow();
        }
        mMapView.invalidate();
    }

    // 当marker开始被拖动时回调此方法, 这个marker的位置可以通过getPosition()方法返回。
    // 这个位置可能与拖动的之前的marker位置不一样。
    // marker 被拖动的marker对象。
/*  @Override
    public void onMarkerDragStart(Marker marker) {
        Log.e("marker","marker正在拖拽");
    }
    // 在marker拖动完成后回调此方法, 这个marker的位置可以通过getPosition()方法返回。
    // 这个位置可能与拖动的之前的marker位置不一样。
    // marker 被拖动的marker对象。
    @Override
    public void onMarkerDrag(Marker marker) {
        latLng=marker.getPosition();
        double latitude= latLng.latitude;
        double longitude= latLng.longitude;
        Log.e("latitude",latitude+"");
        Log.e("longitude",longitude+"");
        getAddress(latLng);
    }
    // 在marker拖动过程中回调此方法, 这个marker的位置可以通过getPosition()方法返回。
    // 这个位置可能与拖动的之前的marker位置不一样。
    // marker 被拖动的marker对象。
    @Override
    public void onMarkerDragEnd(Marker marker) {
        Log.e("marker","marker拖拽完成");
        setMarket(latLng, location.getCity(), addressName);
        // 销毁定位
        if (mLocationClient != null)
        {
            mLocationClient.stopLocation();
            mLocationClient.onDestroy();
        }
    }*/
    @Override
    public void onCameraChange(CameraPosition cameraPosition) {

    }

    @Override
    public void onCameraChangeFinish(CameraPosition cameraPosition) {
        latLng = cameraPosition.target;
        double latitude = latLng.latitude;
        double longitude = latLng.longitude;
        Log.e("latitude", latitude + "");
        Log.e("longitude", longitude + "");
        getAddress(latLng);


    }

    /**
     * 根据经纬度得到地址
     */
    public void getAddress(final LatLng latLng) {
        LatLonPoint latLonPoint = new LatLonPoint(latLng.latitude, latLng.longitude);
        // 第一个参数表示一个Latlng，第二参数表示范围多少米，第三个参数表示是火系坐标系还是GPS原生坐标系
        RegeocodeQuery query = new RegeocodeQuery(latLonPoint, 500, GeocodeSearch.AMAP);
        geocoderSearch.getFromLocationAsyn(query);// 设置同步逆地理编码请求
    }

    /**
     * 逆地理编码回调
     */
    @Override
    public void onRegeocodeSearched(RegeocodeResult result, int rCode) {
        if (rCode == 1000) {
            if (result != null && result.getRegeocodeAddress() != null
                    && result.getRegeocodeAddress().getFormatAddress() != null) {

                addressName = result.getRegeocodeAddress().getFormatAddress(); // 逆转地里编码不是每次都可以得到对应地图上的opi
                Log.e("逆地理编码回调  得到的地址：", addressName);

//              mAddressEntityFirst = new AddressSearchTextEntity(addressName, addressName, true, convertToLatLonPoint(mFinalChoosePosition));
                setMarket(latLng, location.getCity(), addressName);

            }
        }
    }

    /**
     * 地理编码查询回调
     */
    @Override
    public void onGeocodeSearched(GeocodeResult result, int rCode) {
    }
}