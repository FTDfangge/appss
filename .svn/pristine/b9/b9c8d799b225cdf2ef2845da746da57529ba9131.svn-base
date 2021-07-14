package com.vetrack.vetrack;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SensorService {

    //传感器
    private Sensor mAccelerometer;
    private Sensor mMagnetic;
    private Sensor mGyroscope;
    private Sensor mPressure;
    private Sensor mGravity;
    private Sensor mLinear_accelerometer;

    private SensorManager mSensorManager;
    private SensorListener mSensorListener;
    //参数
    private long timestamp;
    private static final float NS2S = 1.0f / 1000000000.0f;
    private float[] angle = new float[3];
    private float[] acc = new float[3];
    private float[] mag = new float[3];
    private float[] gyr = new float[3];
    //    private float pre;
    private float[] gra = new float[3];
    private float[] l_acc = new float[3];
    private float ori;
    private final String[] key = {"lacc", "gra", "gyr", "acc", "mag", "pre", "ori"};
    private Map<String, float[]> value;
    private String dataString = "";

//    private ArrayList<String> dataList = new ArrayList<>();
//    private ArrayList<float[][]> accAndGra = new ArrayList<>();

    public SensorService(Context context) {
        // 初始化传感器
        mSensorListener = new SensorListener();
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        initSensor();
        value = new ConcurrentHashMap<>();
    }

    private class SensorListener implements SensorEventListener {
        @Override
        public void onSensorChanged(SensorEvent event) {
            switch (event.sensor.getType()) {
                case Sensor.TYPE_ACCELEROMETER:
                    // 读取加速度传感器数值，values数组0,1,2分别对应x,y,z轴的加速度
                    acc[0] = event.values[0];
                    acc[1] = event.values[1];
                    acc[2] = event.values[2];
                    break;
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
//                case Sensor.TYPE_PRESSURE:
//                    pre = event.values[0];
//                    break;
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
            }
            ori = calculateOrientation();
            dataString = System.currentTimeMillis() + ","
                    + l_acc[0] + "," + l_acc[1] + "," + l_acc[2] + ","
                    + gra[0] + "," + gra[1] + "," + gra[2] + ","
                    + gyr[0] + "," + gyr[1] + "," + gyr[2] + ","
                    + acc[0] + "," + acc[1] + "," + acc[2] + ","
                    + mag[0] + "," + mag[1] + "," + mag[2] + ","
                    + ori + ",";
//            float[][] a_g = {acc, gra};
//            accAndGra.add(a_g);
//            synchronized (SensorService.this) {
//                if (dataList.size() > 70) {
//                    dataList.subList(0, 50).clear();
//                    accAndGra.subList(0, 50).clear();
//                }
//            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            Log.i("SensorService", "onAccuracyChanged");
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

    private void initSensor() {
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagnetic = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mPressure = mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        mGravity = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        mLinear_accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
    }

    public void registerSensor() {
//        int frequency = 5 * 1000;
        // 注册传感器监听函数
        mSensorManager.registerListener(mSensorListener, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(mSensorListener, mMagnetic, SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(mSensorListener, mGyroscope, SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(mSensorListener, mPressure, SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(mSensorListener, mGravity, SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(mSensorListener, mLinear_accelerometer, SensorManager.SENSOR_DELAY_GAME);
    }

    public void unregisterSensor() {
        mSensorManager.unregisterListener(mSensorListener);
    }
//
//    public ArrayList<String> getDataList() {
//        return dataList;
//    }
//
//    public ArrayList<float[][]> getAccAndGra() {
//        return accAndGra;
//    }

    public String getDataString() {
        return dataString;
    }

    public Map<String, float[]> getValue() {
        value.put(key[0], l_acc);
        value.put(key[1], gra);
        value.put(key[2], gyr);
        value.put(key[3], acc);
        value.put(key[4], mag);
        value.put(key[6], new float[]{ori});
        return value;
    }
}
