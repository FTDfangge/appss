package com.vetrack.vetrack;

import android.annotation.SuppressLint;
import android.content.Context;
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
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.vetrack.vetrack.Utils.FileOperation;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SensorTestActivity extends AppCompatActivity {

    private static final String TAG = "SensorTest";

    private TextView tvSensors;
    private TextView tvPath;

    private final int RECORD_DATA = 1;

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mMagnetic;
    private Sensor mGyroscope;
    private Sensor mGravity;
    private Sensor mLinear_accelerometer;

    private SensorListener mSensorListener;
    //采样间隔
    private static int COLLECT_INTERVAL = 20;
    //文件保存路径
    private static final String path = Environment.getExternalStorageDirectory().getPath() + "/vetrack/data/";
    private SimpleDateFormat df1 = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss", Locale.getDefault());
    private String filePath;

    private Boolean recordStart = false;
    private long timestamp;
    private static final float NS2S = 1.0f / 1000000000.0f;
    private float[] angle = new float[3];

    private float[] acc = new float[3];
    private float[] mag = new float[3];
    private float[] gyr = new float[3];
    private float[] gra = new float[3];
    private float[] l_acc = new float[3];
    private float ori;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor_test);
        tvSensors = findViewById(R.id.tv_sensors);
        tvPath = findViewById(R.id.tv_path);
        Button start = findViewById(R.id.start);
        Button stop = findViewById(R.id.stop);

        // 初始化传感器
        mSensorListener = new SensorListener();
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagnetic = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mGravity = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        mLinear_accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Start!", Toast.LENGTH_SHORT).show();
                String fileName = df1.format(new Date()) + ".csv";
                File file = FileOperation.makeFilePath(path, fileName);
                filePath = file.getAbsolutePath();
                tvPath.setText(filePath);
                recordStart = true;
                onResume();
                Toast.makeText(SensorTestActivity.this, "记录开始", Toast.LENGTH_LONG).show();
                //建立一个线程，用来定时记录数据
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while (recordStart) {
                            try {
                                Thread.sleep(COLLECT_INTERVAL);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            Message message = new Message();
                            message.what = RECORD_DATA;
                            handler.sendMessage(message);
                        }
                    }
                }).start();
            }
        });
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recordStart = false;
                Toast.makeText(getApplicationContext(), "Stop!", Toast.LENGTH_SHORT).show();
                onPause();
            }
        });
    }

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

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case RECORD_DATA:
                    String data_save = String.valueOf(System.currentTimeMillis()) + "," + l_acc[0] + "," + l_acc[1] + "," + l_acc[2] + ","
                            + ori + "," + gra[0] + "," + gra[1] + "," + gra[2] + ","
                            + gyr[0] + "," + gyr[1] + "," + gyr[2] + ","
                            + mag[0] + "," + mag[1] + "," + mag[2];
                    //Log.i(TAG,"1:"+String.valueOf(System.currentTimeMillis()));
                    String[] content = {data_save, filePath};
                    WriteWork writeWork = new WriteWork();
                    writeWork.execute(content);
                    //Log.i(TAG,"2:"+String.valueOf(System.currentTimeMillis()));
                    break;
            }
        }
    };

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
            ArrayList<String> tData = new ArrayList<>();
            tData.add("time:" + String.valueOf(System.currentTimeMillis()));
            tData.add("l_acc:" + l_acc[0] + "," + l_acc[1] + "," + l_acc[2]);
            tData.add("orientation:" + ori);
            tData.add("gra:" + gra[0] + "," + gra[1] + "," + gra[2]);
            tData.add("gyr:" + gyr[0] + "," + gyr[1] + "," + gyr[2]);
            tData.add("mag:" + mag[0] + "," + mag[1] + "," + mag[2]);
            String data = listToString(tData);
            //Log.i(TAG,data);
            tvSensors.setText(data);
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

    private static String listToString(List<String> list) {
        if (list == null) {
            return null;
        }
        StringBuilder result = new StringBuilder();
        boolean first = true;
        //第一个前面不拼接","
        for (String string : list) {
            if (first) {
                first = false;
            } else {
                result.append("\r\n");
            }
            result.append(string);
        }
        return result.toString();
    }

    private static class WriteWork extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... strings) {
            FileOperation fileOperation = new FileOperation();
            fileOperation.writeCsv(strings[0], strings[1]);
            return null;
        }
    }
}
