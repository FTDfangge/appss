package com.vetrack.vetrack;

import java.util.LinkedList;
import com.aiunit.common.protocol.ocr.OCRItem;
import com.aiunit.common.protocol.ocr.OCRItemCollection;
import com.aiunit.common.protocol.types.Point;
import com.aiunit.vision.common.ConnectionCallback;
import com.aiunit.vision.ocr.OCRInputSlot;
import com.aiunit.vision.ocr.OCROutputSlot;
import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;
import com.coloros.ocs.ai.cv.CVUnit;
import com.coloros.ocs.ai.cv.CVUnitClient;
import com.coloros.ocs.base.common.ConnectionResult;
import com.coloros.ocs.base.common.api.OnConnectionFailedListener;
import com.coloros.ocs.base.common.api.OnConnectionSucceedListener;
import com.vetrack.vetrack.Service.LocationService;
import com.vetrack.vetrack.Utils.Utils;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import java.util.LinkedList;
import java.util.List;

/***
 * 定位滤波demo，实际定位场景中，可能会存在很多的位置抖动，此示例展示了一种对定位结果进行的平滑优化处理
 * 实际测试下，该平滑策略在市区步行场景下，有明显平滑效果，有效减少了部分抖动，开放算法逻辑，希望能够对开发者提供帮助
 * 注意：该示例场景仅用于对定位结果优化处理的演示，里边相关的策略或算法并不一定适用于您的使用场景，请注意！！！
 *
 * @author baidu
 *
 */
public class OutsideMap extends Activity {

    private MapView mMapView = null;
    private BaiduMap mBaiduMap;
    private Button reset;
    private Button picture;  //AI Unit拍照Button
    private LocationService locService;
    private LinkedList<LocationEntity> locationList = new LinkedList<LocationEntity>(); // 存放历史定位结果的链表，最大存放当前结果的前5次定位结果
    private double currentLatitude, currentLongitude;
    private double latitudeEdge1 = 39.9860, latitudeEdge2 = 39.9876, longitudeEdge1 = 116.3207, longitudeEdge2 = 116.3222;
    private TextView latitudeTextView, longitudeTextView;

    //Ai Unit
    private  static int REQ_1 = 1;
    private String[] unit_Result;
    private CVUnitClient mCVClient;
    private int startCode = -1;
    private Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outside_map);
        requestPermissions();   //拍照权限请求

        mMapView = (MapView) findViewById(R.id.bmapView);
        latitudeTextView = findViewById(R.id.latitudeText);
        longitudeTextView = findViewById(R.id.longitudeText);
        reset = (Button) findViewById(R.id.clear);
        picture = (Button) findViewById(R.id.picture);
        mBaiduMap = mMapView.getMap();
        mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
        mBaiduMap.setMapStatus(MapStatusUpdateFactory.zoomTo(15));
        locService = ((LocationApplication) getApplication()).locationService;
        LocationClientOption mOption = locService.getDefaultLocationClientOption();
        mOption.setLocationMode(LocationClientOption.LocationMode.Battery_Saving);
        mOption.setCoorType("bd09ll");
        locService.setLocationOption(mOption);
        locService.registerListener(listener);
        locService.start();

        //初始化算法
        mCVClient = CVUnit.getOCRARDetectorClient
                (this.getApplicationContext()).addOnConnectionSucceedListener(new OnConnectionSucceedListener() {
            @Override
            public void onConnectionSucceed() {
                Log.i("TAG", " authorize connect: onConnectionSucceed");
            }
        }).addOnConnectionFailedListener(new OnConnectionFailedListener() {
            @Override
            public void onConnectionFailed(ConnectionResult connectionResult) {
                Log.e("TAG", " authorize connect: onFailure: " + connectionResult.getErrorCode());
            }
        });

        //连接AIUnitServer，重写方法
        mCVClient.initService(this, new ConnectionCallback() {
            @Override
            public void onServiceConnect() {
                Log.i("TAG", "initService: onServiceConnect");
                startCode = mCVClient.start();
            }

            @Override
            public void onServiceDisconnect() {
                Log.e("TAG", "initService: onServiceDisconnect: ");
            }
        });

        picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);  //启用相机
                startActivityForResult(intent, REQ_1);
            }
        });
//        测试使用代码（自动触发进入停车场）
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(OutsideMap.this);
        alertDialogBuilder.setMessage("检测到即将进入家乐福地下停车场，地下停车场GPS较弱，即将进入地下停车场定位系统，确定进入吗");
        alertDialogBuilder.setPositiveButton("确定", confirmClick);
        alertDialogBuilder.setNegativeButton("取消", cancelClick);
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private DialogInterface.OnClickListener confirmClick = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            Intent intent = new Intent(OutsideMap.this, RealtimeActivity.class);
            startActivity(intent);
            OutsideMap.this.finish();
        }
    };

    private DialogInterface.OnClickListener cancelClick = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            dialog.cancel();
        }
    };

    /***
     * 定位结果回调，在此方法中处理定位结果
     */
    BDAbstractLocationListener listener = new BDAbstractLocationListener() {

        @Override
        public void onReceiveLocation(BDLocation location) {
            // TODO Auto-generated method stub

            if (location != null && (location.getLocType() == 161 || location.getLocType() == 66)) {
                Message locMsg = locHander.obtainMessage();
                Bundle locData;
                locData = Algorithm(location);
                if (locData != null) {
                    locData.putParcelable("loc", location);
                    locMsg.setData(locData);
                    locHander.sendMessage(locMsg);
                }
            }
        }

    };

    /***
     * 平滑策略代码实现方法，主要通过对新定位和历史定位结果进行速度评分，
     * 来判断新定位结果的抖动幅度，如果超过经验值，则判定为过大抖动，进行平滑处理,若速度过快，
     * 则推测有可能是由于运动速度本身造成的，则不进行低速平滑处理 ╭(●｀∀´●)╯
     *
     * @param location
     * @return Bundle
     */
    private Bundle Algorithm(BDLocation location) {
        Bundle locData = new Bundle();
        double curSpeed = 0;
        if (locationList.isEmpty() || locationList.size() < 2) {
            LocationEntity temp = new LocationEntity();
            temp.location = location;
            temp.time = System.currentTimeMillis();
            locData.putInt("iscalculate", 0);
            locationList.add(temp);
        } else {
            if (locationList.size() > 5)
                locationList.removeFirst();
            double score = 0;
            for (int i = 0; i < locationList.size(); ++i) {
                LatLng lastPoint = new LatLng(locationList.get(i).location.getLatitude(),
                        locationList.get(i).location.getLongitude());
                LatLng curPoint = new LatLng(location.getLatitude(), location.getLongitude());
                double distance = DistanceUtil.getDistance(lastPoint, curPoint);
                curSpeed = distance / (System.currentTimeMillis() - locationList.get(i).time) / 1000;
                score += curSpeed * Utils.EARTH_WEIGHT[i];
            }
            if (score > 0.00000999 && score < 0.00005) { // 经验值,开发者可根据业务自行调整，也可以不使用这种算法
                location.setLongitude(
                        (locationList.get(locationList.size() - 1).location.getLongitude() + location.getLongitude())
                                / 2);
                location.setLatitude(
                        (locationList.get(locationList.size() - 1).location.getLatitude() + location.getLatitude())
                                / 2);
                locData.putInt("iscalculate", 1);
            } else {
                locData.putInt("iscalculate", 0);
            }
            LocationEntity newLocation = new LocationEntity();
            newLocation.location = location;
            newLocation.time = System.currentTimeMillis();
            locationList.add(newLocation);
        }
        return locData;
    }

    /***
     * 接收定位结果消息，并显示在地图上
     */
    private Handler locHander = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
            try {
                BDLocation location = msg.getData().getParcelable("loc");
                int iscal = msg.getData().getInt("iscalculate");
                if (location != null) {
                    LatLng point = new LatLng(location.getLatitude(), location.getLongitude());
                    currentLatitude = location.getLatitude();
                    currentLongitude = location.getLongitude();
                    Log.d("LOCATE", String.valueOf(currentLatitude) + "-" + String.valueOf(currentLongitude));
                    latitudeTextView.setText(String.valueOf(currentLatitude));
                    longitudeTextView.setText(String.valueOf(currentLongitude));

                    Log.d("LOCATE, la", String.valueOf((currentLatitude >= latitudeEdge1) && (currentLatitude <= latitudeEdge2)));
                    Log.d("LOCATE, lo", String.valueOf((currentLongitude >= longitudeEdge1) && (currentLongitude <= longitudeEdge2)));

                    if ((currentLatitude >= latitudeEdge1) && (currentLatitude <= latitudeEdge2) &&
                            (currentLongitude >= longitudeEdge1) && (currentLongitude <= longitudeEdge2)) {
                        //means user had been in the parking lot.

                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(OutsideMap.this);
                        alertDialogBuilder.setMessage("检测到即将进入家乐福地下停车场，地下停车场GPS较弱，即将进入地下停车场定位系统，确定进入吗");
                        alertDialogBuilder.setPositiveButton("确定", confirmClick);
                        alertDialogBuilder.setNegativeButton("取消", cancelClick);
                        AlertDialog alertDialog = alertDialogBuilder.create();
                        alertDialog.show();
                    }


                    // 构建Marker图标
                    BitmapDescriptor bitmap = null;
                    if (iscal == 0) {
                        bitmap = BitmapDescriptorFactory.fromResource(R.drawable.icon_openmap_mark); // 非推算结果
                    } else {
                        bitmap = BitmapDescriptorFactory.fromResource(R.drawable.icon_openmap_focuse_mark); // 推算结果
                    }

                    if (mBaiduMap != null)
                        mBaiduMap.clear();

                    // 构建MarkerOption，用于在地图上添加Marker
                    OverlayOptions option = new MarkerOptions().position(point).icon(bitmap);
                    // 在地图上添加Marker，并显示
                    mBaiduMap.addOverlay(option);
                    mBaiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(point));
                }
            } catch (Exception e) {
                // TODO: handle exception
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        locService.unregisterListener(listener);
        locService.stop();
        mMapView.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        mMapView.onResume();
        reset.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (mBaiduMap != null)
                    mBaiduMap.clear();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        // 在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        mMapView.onPause();
    }

    /**
     * 封装定位结果和时间的实体类
     *
     * @author baidu
     */
    class LocationEntity {
        BDLocation location;
        long time;
    }

    private void requestPermissions() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                if (permission != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[] {
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.CAMERA
                    }, 0x0010);
                }
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            if(requestCode == REQ_1){
                Bundle bundle = data.getExtras();
                bitmap = (Bitmap)bundle.get("data");

                if (bitmap != null){
                    Log.i("TAG", "bitmap is ok");
                }

                Log.i("TAG", "if startCode "+String.valueOf(startCode));
                if (startCode == 0)
                {
                    //设置输入
                    OCRInputSlot inputSlot = (OCRInputSlot) mCVClient.createInputSlot();
                    inputSlot.setTargetBitmap(bitmap);

                    //设置输出
                    OCROutputSlot outputSlot = (OCROutputSlot) mCVClient.createOutputSlot();

                    Log.i("TAG", "ready to output");
                    //运行算法
                    mCVClient.process(inputSlot, outputSlot);
                    OCRItemCollection ocrItemCollection = outputSlot.getOCRItemCollection();
                    List<OCRItem> ocrItemList = ocrItemCollection.getOrcItemList();
                    unit_Result = new String[ocrItemList.size()+1];
                    int i=0;
                    for (OCRItem ocrItem: ocrItemList) {
                        List<Point> boundingBox = ocrItem.getBoundingBox();
                        unit_Result[i] = ocrItem.getText();
                        Log.i("TAG", "text "+ unit_Result[i++]);
                    }


//                    //算法释放
//                    if (mCVClient != null) {
//                        mCVClient.stop();
//                    }
//
//                    //断开连接
//                    mCVClient.releaseService();
//                    mCVClient = null;
                } else {
                    Log.i("TAG", "startCode "+String.valueOf(startCode));
                }
            }
        }
    }
}
