package com.vetrack.vetrack;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.vetrack.vetrack.Function.DrawPoint;
import com.vetrack.vetrack.Function.VetrackSystem;
import com.vetrack.vetrack.Model.TraceInfo;
import com.vetrack.vetrack.Service.Network;
import com.vetrack.vetrack.Utils.FileOperation;
import com.vetrack.vetrack.Utils.Setting;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

/**
 * Prediction and Roll Back Mode
 * In this mode, the system will try to predict whether the vehicle is going through landmarks,
 * so that it will be more real-time.
 * Additionally, optional strategy of rolling back can be add into back-end.
 */
public class RealtimeActivity extends AppCompatActivity {

    private static final String TAG = "RollbackActivity";
    private static final int STRAT_TRACK = 0x00010001;
    private static final int DRAW_POINT = 0x00010002;
    private static final int ERROR = 0x00010000;
    private final int READ_DATA = 0x00010003;
    private final int REFRESH_MAP = 0x00010004;
    private String path = Environment.getExternalStorageDirectory().getPath() + "/vetrack";

    private ImageView imageView;
    private DrawPoint drawPoint;
    private Bitmap resultBm;

    private boolean run;

    private VetrackSystem vetrackSystem;
    TraceInfo traceInfo;

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mMagnetic;
    private Sensor mGyroscope;
    private Sensor mGravity;
    private Sensor mLinear_accelerometer;
    private SensorListener mSensorListener;
    private static int COLLECT_INTERVAL = Setting.COLLECT_INTERVAL;//采样间隔
    private long timestamp;
    private static final float NS2S = 1.0f / 1000000000.0f;
    private float[] angle = new float[3];
    private float[] acc = new float[3];
    private float[] mag = new float[3];
    private float[] gyr = new float[3];
    private float[] gra = new float[3];
    private float[] l_acc = new float[3];
    private float ori;
    private int privacyState = -1, drivingState = 0; //0->drive 1->park
    private String longDataString;
    private SensorService sensorService;
    private Bitmap bitmapFromServer;

    private Button privacyBtn, pdBtn; //pdBtn means park or drive button
    //文件保存路径
    private String filePath;
    //    private String videoName;
    private SimpleDateFormat df1 = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss", Locale.getDefault());
    private File dataFile;
    Boolean recordStart = false;
//    int parking, pose, trace;//test

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rollback);

        setWindow();
        sensorService = new SensorService(RealtimeActivity.this);
        imageView = findViewById(R.id.imaV_map_r);

        privacyBtn = findViewById(R.id.privacySwitchBtn);
        privacyState = Network.getInstance().getPrivacyState();
        if (privacyState == 0) {
            privacyBtn.setText("公开模式");
        } else {
            privacyBtn.setText("隐私模式");
        }
        privacyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (privacyState == 0) {
                    privacyState = 1;
                    Network.getInstance().setPrivacyState(1);
                    privacyBtn.setText("隐私模式");
                } else {
                    privacyState = 0;
                    Network.getInstance().setPrivacyState(0);
                    privacyBtn.setText("公开模式");
                }
            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String map = Network.getInstance().download("getMap");

//                        byte[] img = map.getBytes();
//                        bitmapFromServer = BitmapFactory.decodeByteArray(img, 0, img.length);


                    byte[] bitmapByte = Base64.decode(map, Base64.DEFAULT);
                    bitmapFromServer = BitmapFactory.decodeByteArray(bitmapByte, 0, bitmapByte.length);
                    Log.d("mapSize", String.valueOf(bitmapFromServer == null));
//                    ImageData.SavaImage(bitmapFromServer,savePath,"vetrack");
                    if (bitmapFromServer != null) {
                        Message message = new Message();
                        message.what = REFRESH_MAP;
                        handler.sendMessage(message);
                    }

                } catch (NullPointerException e) {

                }


            }
        }).start();

        pdBtn = findViewById(R.id.pdBtn);
        pdBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (drivingState == 0) { //press to park
                    pdBtn.setText("在此停车");
                    if (Network.getInstance().getPrivacyState() == 0) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                File dataFile = recordData();
                                String fileName = df1.format(new Date()) + ".csv";
                                try {
                                    Network.getInstance().upload(fileName, longDataString);
                                    Toast.makeText(RealtimeActivity.this, "file Uploaded", Toast.LENGTH_LONG).show();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }).start();
                    }
                    // TODO: 4/1/2021 draw a car point here

                } else {
                    pdBtn.setText("开车离开");
                    // TODO: 4/1/2021 remove car
                }

            }
        });


        // 初始化传感器
        mSensorListener = new SensorListener();
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (mSensorManager != null) {
            mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            mMagnetic = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
            mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
            mGravity = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
            mLinear_accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        }
        // 初始化画布
        drawPoint = new DrawPoint(RealtimeActivity.this, Setting.n_ps);


        traceInfo = new TraceInfo();
        if (!run) {
//      Str
            run = true;
            traceInfo.setInit_x(80);
            traceInfo.setInit_y(80);
            traceInfo.setInit_theta(180);
            traceInfo.setInit_sn(1);
            Toast.makeText(RealtimeActivity.this,
                            "Initializing!",
                            Toast.LENGTH_SHORT).show();
            new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                vetrackSystem = new VetrackSystem();
                                vetrackSystem.initialMap(path);
                                vetrackSystem.initialTrack(traceInfo);
                                myHandler.sendEmptyMessage(STRAT_TRACK);
                            } catch (IOException | ClassNotFoundException e) {
                                e.printStackTrace();
                                myHandler.sendEmptyMessage(ERROR);
                                Log.e("Init","Error");
                            }
                        }
                    }).start();

        } else {
            run = false;
            vetrackSystem.stop();

        }
    }


    @SuppressLint("HandlerLeak")
    Handler myHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case STRAT_TRACK:

                    Toast.makeText(RealtimeActivity.this,
                            "Start Tracking!",
                            Toast.LENGTH_SHORT).show();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            double[][] robots;
                            int countData = 0;
                            recordStart = true;
                            dataFile = recordData();
//                            String tracePath = path + "/trace";//test
//                            Matrix dataAll = Utils.readMatrix(tracePath + "/" + parking + pose + trace + ".csv");//test
//                            int dataPtr = 0;//test
                            while (run) { //&& dataPtr < dataAll.getRowDimension()
                                try {
                                    Thread.sleep(COLLECT_INTERVAL);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                try {
                                    double[] data = {System.currentTimeMillis(),//real time
                                            l_acc[0], l_acc[1], l_acc[2], ori,
                                            gra[0], gra[1], gra[2],
                                            gyr[0], gyr[1], gyr[2],
                                            mag[0], mag[1], mag[2]};

                                    robots = vetrackSystem.processData(data, 0, 0);
//                                robots = vetrackSystem.processData(dataAll.getArray()[dataPtr++]);//test
                                    if (countData % 2 == 0) {
                                        resultBm = drawPoint.setPoints(robots[0], robots[1], robots[6]);

                                        myHandler.sendEmptyMessage(DRAW_POINT);
                                    }


                                    countData++;
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    Log.e("Run","ERROR");
                                    myHandler.sendEmptyMessage(ERROR);
                                }
                            }
                            run = false;
                            vetrackSystem.stop();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    Toast.makeText(RealtimeActivity.this,
                                            "Finish!",
                                            Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }).start();
                    break;
                case DRAW_POINT:
                    imageView.setImageBitmap(resultBm);
                    break;
                case ERROR:
                    run = false;
                    Toast.makeText(RealtimeActivity.this,
                            "Something goes wrong and system terminate!",
                            Toast.LENGTH_LONG).show();
            }
        }
    };


    @Override
    protected void onResume() {
        super.onResume();
        // 注册传感器监听函数
        mSensorManager.registerListener(mSensorListener, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(mSensorListener, mMagnetic, SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(mSensorListener, mGyroscope, SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(mSensorListener, mGravity, SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(mSensorListener, mLinear_accelerometer, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // 注销监听函数
        mSensorManager.unregisterListener(mSensorListener);
    }

    private class SensorListener implements SensorEventListener {

        @Override
        public void onSensorChanged(SensorEvent event) {
            switch (event.sensor.getType()) {
                case Sensor.TYPE_MAGNETIC_FIELD:
                    mag[0] = event.values[0];
                    mag[1] = event.values[1];
                    mag[2] = event.values[2];
                    break;
                case Sensor.TYPE_GYROSCOPE:
                    if (timestamp != 0) {
                        final float dT = (event.timestamp - timestamp) * NS2S;

                        angle[0] += event.values[0] * dT;
                        angle[1] += event.values[1] * dT;
                        angle[2] += event.values[2] * dT;

                        gyr[0] = (float) Math.toDegrees(angle[0]);
                        gyr[1] = (float) Math.toDegrees(angle[1]);
                        gyr[2] = (float) Math.toDegrees(angle[2]);
                    }
                    timestamp = event.timestamp;
                    break;
                case Sensor.TYPE_GRAVITY:
                    gra[0] = event.values[0];
                    gra[1] = event.values[1];
                    gra[2] = event.values[2];
                    break;
                case Sensor.TYPE_LINEAR_ACCELERATION:
                    l_acc[0] = event.values[0];
                    l_acc[1] = event.values[1];
                    l_acc[2] = event.values[2];
                    break;
                case Sensor.TYPE_ACCELEROMETER:
                    // 读取加速度传感器数值，values数组0,1,2分别对应x,y,z轴的加速度
                    acc[0] = event.values[0];
                    acc[1] = event.values[1];
                    acc[2] = event.values[2];
                    break;
            }
            ori = calculateOrientation();
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            Log.i(TAG, "onAccuracyChanged");
        }
    }

    private float calculateOrientation() {
        float[] values = new float[3];
        float[] R = new float[16];
        SensorManager.getRotationMatrix(R, null, acc, mag);
        SensorManager.getOrientation(R, values);
        // 要经过一次数据格式的转换，转换为度
        values[0] = (float) Math.toDegrees(values[0]);
        if (values[0] < 0)
            values[0] += 360;
        return values[0];
    }

    private void setWindow() {
        //保持屏幕常亮
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //竖屏
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    private File recordData() {
        String fileName = df1.format(new Date()) + ".csv";
        File file = FileOperation.makeFilePath(path, fileName);
        filePath = file.getAbsolutePath();
        String dataFormat = "Sys_time,laccx,y,z,grax,y,z,gyrx,y,z,accx,y,z,magx,y,z,ori," +
                "lon,lat,speed,bearing,gps_time";
        String[] content = {dataFormat, filePath};

        RealtimeActivity.WriteWork writeWork = new RealtimeActivity.WriteWork();
        writeWork.execute(content);
        String show = filePath + "\n" + dataFormat;

//        Toast.makeText(MainActivity.this, "记录开始", Toast.LENGTH_LONG).show();
        //建立一个线程，用来定时记录数据
        new Thread(new Runnable() {
            @Override
            public void run() {
                sensorService.registerSensor();
                int count = 0;
                while (recordStart) {
                    try {
                        Thread.sleep(COLLECT_INTERVAL - 1);
                        Map<String, float[]> sensorValue = sensorService.getValue();
                        l_acc = (float[]) sensorValue.get("lacc");
                        gra = (float[]) sensorValue.get("gra");
                        gyr = (float[]) sensorValue.get("gyr");
                        acc = (float[]) sensorValue.get("acc");
                        mag = (float[]) sensorValue.get("mag");
                        float[] ori_temp = (float[]) sensorValue.get("ori");
                        if (ori_temp != null) {
                            ori = ori_temp[0];
                        }
                        String dataString = System.currentTimeMillis() + ","
                                + l_acc[0] + "," + l_acc[1] + "," + l_acc[2] + ","
                                + gra[0] + "," + gra[1] + "," + gra[2] + ","
                                + gyr[0] + "," + gyr[1] + "," + gyr[2] + ","
                                + acc[0] + "," + acc[1] + "," + acc[2] + ","
                                + mag[0] + "," + mag[1] + "," + mag[2] + ","
                                + ori + ",";
//                                + gpsService.getDataString();
//                                String data = sensorService.getDataString() + gpsService.getDataString();
                        longDataString += dataString;
                        String[] content = {dataString, filePath};
                        RealtimeActivity.WriteWork writeWork = new RealtimeActivity.WriteWork();
                        writeWork.execute(content);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (++count == 25) {
                        Message message = new Message();
                        message.what = READ_DATA;
                        handler.sendMessage(message);
                        count = 0;
                    }
                }
            }
        }).start();

        return file;
    }

    private static class WriteWork extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... strings) {
            FileOperation fileOperation = new FileOperation();
            fileOperation.writeCsv(strings[0], strings[1]);
            return null;
        }
    }

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case READ_DATA:
                    ArrayList<String> tData = new ArrayList<>();
                    String time = String.valueOf(System.currentTimeMillis());
                    tData.add("time:" + time);
//                    tData.add("\n" + "locate:" + gpsService.getDataString());
                    tData.add("\n" + "acc:" + acc[0] + "," + acc[1] + "," + acc[2]);
                    tData.add("\n" + "mag:" + mag[0] + "," + mag[1] + "," + mag[2]);
                    tData.add("\n" + "gyr:" + gyr[0] + "," + gyr[1] + "," + gyr[2]);
                    tData.add("\n" + "gra:" + gra[0] + "," + gra[1] + "," + gra[2]);
                    tData.add("\n" + "l_acc:" + l_acc[0] + "," + l_acc[1] + "," + l_acc[2]);
//                    tData.add("press:" + pre);
                    tData.add("\n" + "orientation:" + ori);
                    break;

                case REFRESH_MAP:
                    imageView.setImageBitmap(bitmapFromServer);
                    Log.d("MAP", "MAP refreshed--XF");
                    break;

            }
        }
    };


}
