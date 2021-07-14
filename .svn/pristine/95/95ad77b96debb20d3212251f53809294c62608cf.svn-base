package com.vetrack.vetrack.Model;

import android.util.Log;

import com.vetrack.vetrack.Utils.Setting;

import java.util.ArrayList;

/**
 * Vetrack
 * Create on 2019/5/28.
 */
public class CarData {
    private int meanPtr;//记录均值处理到哪里了
    //private int wPtr;//记录w做差处理到哪里了
    private int time;//当前时间
    private ArrayList<double[]> rawData;//[0Time, 1l_ax, 2l_ay, 3l_az, 4ori, 5gx, 6gy, 7gz, 8gyrox, 9gyroy, 10gyroz, 11magx, 12magy, 13magz]
    private ArrayList<double[]> carData;//[ax ay az wz ori]
    private ArrayList<Double> w3;
    private int limit;
    private boolean realtime;

    private final int window = 7;
    private final static double g = 9.81;

    public ArrayList<double[]> getCarData() {
        return carData;
    }

    public void setCarData(ArrayList<double[]> carData) {
        this.carData = carData;
    }

    public int getSize() {
        return carData.size();
    }

    public int getTime() {
        return time;
    }

    public CarData(boolean realtime) {
        rawData = new ArrayList<>();
        carData = new ArrayList<>();
        w3 = new ArrayList<>();
        meanPtr = (int) Math.floor(window / 2);
        //wPtr = 1;
        time = 0;
        limit = Setting.realTimeWidth;
        this.realtime = realtime;
    }

    public void addData(double[] data) {
        rawData.add(data);

        double[] Z = new double[3];
        Z[0] = data[5] / g;
        Z[1] = data[6] / g;
        Z[2] = data[7] / g;

        double[] X = crossProduct(Z[0], Z[1], Z[2], 0, 1, 0);

        double Q = Math.sqrt(X[0] * X[0] + X[1] * X[1] + X[2] * X[2]);

        X[0] /= Q;
        X[1] /= Q;
        X[2] /= Q;

        double[] Y = crossProduct(Z[0], Z[1], Z[2], X[0], X[1], X[2]);

        Q = Math.sqrt(Y[0] * Y[0] + Y[1] * Y[1] + Y[2] * Y[2]);

        Y[0] /= Q;
        Y[1] /= Q;
        Y[2] /= Q;

        double[] acc = new double[3];
        acc[0] = data[1];
        acc[1] = data[2];
        acc[2] = data[3];

        //double[] w = new double[3];
        if (rawData.size() >= 2) {//只有当数据多于两条的时候才可以执行做差
            double[] gyro = new double[3];
            gyro[0] = data[8];
            gyro[1] = data[9];
            gyro[2] = data[10];

            double[] gyro2 = new double[3];
            gyro2[0] = rawData.get(rawData.size() - 2)[8];
            gyro2[1] = rawData.get(rawData.size() - 2)[9];
            gyro2[2] = rawData.get(rawData.size() - 2)[10];

            double w = (gyro[0] - gyro2[0]) * Z[0] + (gyro[1] - gyro2[1]) * Z[1] + (gyro[2] - gyro2[2]) * Z[2];

            if (w > 5) w -= 2 * Math.PI;
            if (w < -5) w += 2 * Math.PI;
            w /= 0.02;//0.02是时间，计算角速度需要除以时间
            //为了增加数据的稳定性，如果w为0，则令其等于上一个数值
            if (w == 0 && w3.size() > 1) w = w3.get(w3.size() - 1);
            w3.add(w);
        }
        double tempmean = 0;
        double[] tempWindow = new double[window];
        int w_mid = (int) Math.floor(window / 2);
        if (w3.size() >= window && meanPtr <= (w3.size() - w_mid)) {//当w长度超过window的时候，取均值，让wz的变化平滑一点
            for (int i = -w_mid, j = 0; i <= w_mid; i++, j++) {
                tempWindow[j] = w3.get(meanPtr + i);
            }
            double sum = 0;
            for (int i = 0; i < window; i++)
                sum += tempWindow[i];
            tempmean = sum / window;
            carData.get(meanPtr)[3] = tempmean;
            meanPtr++;
        }

        //ori
        double ori = data[4];
        ori = ori * Math.PI / 180;
        //ori = Math.floor(ori / 2 * Math.PI);

        double[] tempData = new double[5];

        tempData[0] = acc[0] * X[0] + acc[1] * X[1] + acc[2] * X[2];
        tempData[1] = -(acc[0] * Y[0] + acc[1] * Y[1] + acc[2] * Y[2]);
        tempData[2] = -(acc[0] * Z[0] + acc[1] * Z[1] + acc[2] * Z[2]);
        tempData[3] = w3.size() > 0 ? w3.get(w3.size() - 1) : 0;
        tempData[4] = ori;


        carData.add(tempData);
        time = carData.size() - 1;

        while (carData.size() > limit && realtime) {
            carData.remove(0);
        }
        while (rawData.size() > limit && realtime) {//limit the size of rawData
            rawData.remove(0);
            w3.remove(0);
            meanPtr--;
        }
    }

    private double[] crossProduct(double a1, double a2, double a3, double b1, double b2, double b3) {
        double[] c = new double[3];
        c[0] = a2 * b3 - a3 * b2;
        c[1] = a3 * b1 - a1 * b3;
        c[2] = a1 * b2 - a2 * b1;
        return c;
    }

    public double[] getLastCarData() {
        return carData.get(carData.size() - 1);
    }

    public void show_data() {
        for (int i = 0; i < carData.size(); i++) {
            Log.i("carData", "row" + (i + 1) + "\t" + carData.get(i)[0] + ",\t" + carData.get(i)[1] + ",\t" + carData.get(i)[2] + ",\t" + carData.get(i)[3] + ",\t"
                    + carData.get(i)[4] + ",\t" + carData.get(i)[5] + ",\t" + carData.get(i)[6] + ",\t" + carData.get(i)[7] + ",\t" + carData.get(i)[8] + ",\t");
        }
    }
}
