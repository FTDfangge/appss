package com.vetrack.vetrack.Function;

import com.vetrack.vetrack.Model.LandMarks;
import com.vetrack.vetrack.Utils.Setting;
import com.vetrack.vetrack.Utils.Utils;

import java.util.ArrayList;

import static java.lang.Math.abs;


/**
 * Vetrack
 * Create on 2019/5/28.
 */
public class Detector {
    private ArrayList<Double> pred_bump;
    private ArrayList<Double> pred_corner;
    private ArrayList<Double> pred_turn;
    private ArrayList<Double> real_bump;
    private ArrayList<Double> real_corner;
    private ArrayList<Double> real_turn;
    private int ptr_bump = 0;//最后一个可信的bump的index
    private int ptr_corner = 0;//最后一个可信corner的index
    private int ptr_turn = 0;//最后一个可信turn的index
    private int pred_ptr_bump = 0;
    private int pred_ptr_corner = 0;
    private int pred_ptr_turn = 0;
    private static double eps = 0.0001;
    private boolean landMarking = false;//
    private int lastLandMarkEnd = 0;
    private ArrayList<Integer> extremum = new ArrayList<>(); //标记极值的位置，用来判断turn的左右方向
    private int turnWidth = 0;
    private int turn_leftPtr;
    private double turn_value;

    public Detector() {
        pred_bump = new ArrayList<Double>();
        pred_turn = new ArrayList<Double>();
        pred_corner = new ArrayList<Double>();
        real_bump = new ArrayList<Double>();
        real_corner = new ArrayList<Double>();
        real_turn = new ArrayList<Double>();
    }

    public void detector_threshold(ArrayList<double[]> carData) {
        ArrayList<Double> az = new ArrayList<>();
        ArrayList<Double> w = new ArrayList<>();
        for (double[] data : carData) {
            az.add(data[2]);
            w.add(data[3]);
        }
        real_bump = detectThreshold(az, Setting.bump_wSize, Setting.bump_thres, Setting.bump_width);
        real_corner = detectThreshold(w, Setting.corner_wSize, Setting.corner_thres, Setting.corner_width);
        real_turn = detectThreshold(w, Setting.turn_wSize, Setting.turn_thres, 80);
        turnDirect(w);
    }

    public int[] detector_threshold_RealTime(ArrayList<double[]> carData) {
        ArrayList<Double> az = new ArrayList<>();
        ArrayList<Double> w = new ArrayList<>();
        for (int i = 0; i < carData.size(); i++) {
            az.add(carData.get(i)[2]);
            w.add(carData.get(i)[3]);
        }
        detectThreshold(az, Setting.bump_wSize, Setting.bump_thres, Setting.bump_width);
        if (!extremum.isEmpty()) {
            ptr_bump = extremum.get(extremum.size() - 1);
            ptr_bump = ptr_bump < Setting.realTimeWidth / 2 ? 0 : ptr_bump;
        }
        detectThreshold(w, Setting.corner_wSize, Setting.corner_thres, Setting.corner_width);
        if (!extremum.isEmpty()) {
            ptr_corner = extremum.get(extremum.size() - 1);
            //pred_turn = detect_thres(w, Setting.turn_wSize, Setting.turn_thres, 80);
            ptr_corner = ptr_corner < Setting.realTimeWidth / 2 ? 0 : ptr_corner;
            int[] index = getTurnWidth(w, ptr_corner);
            turn_leftPtr = index[0];
            turn_value = w.get(ptr_corner) < 0 ? -1 : 1;
        }

        int[] result = new int[]{ptr_bump, ptr_corner};
        ptr_bump = ptr_corner = 0;
        return result;
    }

    LandMarks detector_predict(ArrayList<double[]> carData, int countData) {
        LandMarks landMarks = new LandMarks();
        ArrayList<Double> az = new ArrayList<>();
        ArrayList<Double> w = new ArrayList<>();
        for (int i = 0; i < carData.size(); i++) {
            az.add(carData.get(i)[2]);
            w.add(carData.get(i)[3]);
        }

        double bumpNow = 0, cornerNow = 0;
        ArrayList<Double> bump = Utils.removeZero(preProcess_avg(az, Setting.bump_wSize));
        if (!bump.isEmpty()) {
            bumpNow = bump.get(bump.size() - 1);
            if (bumpNow > Setting.bump_thres) {
                landMarks.setBump(true);
            }
        }
        ArrayList<Double> corner = Utils.removeZero(preProcess_avg(w, Setting.corner_wSize));
        if (!corner.isEmpty()) {
            cornerNow = corner.get(corner.size() - 1);
            if (cornerNow > Setting.corner_thres) {
                landMarks.setCorner(true);
            }
            //ArrayList<Double> turn = Utils.removeZero(preProcess_avg(w,Setting.turn_wSize));
            if (cornerNow > Setting.turn_thres) {
                if (gradientAvg(corner) < 0) {//防止在阈值周围抖动的现象
                    for (int i = 1; i < 10; i++) {
                        if (corner.get(corner.size() - i) < Setting.turn_thres)
                            return landMarks;
                    }
                }
                double value = w.get(w.size() - 1 - Setting.corner_wSize / 2) < 0 ? -1 : 1;
                landMarks.setTurn(value);
            }
        }
        if (bumpNow > Setting.bump_thres || cornerNow > Setting.corner_thres) {
            if (!landMarking) {
                landMarking = true;
            }
        } else {
            if (landMarking) {
                landMarking = false;
                lastLandMarkEnd = countData;
            }
        }
        return landMarks;
    }

    LandMarks detector_predict(ArrayList<double[]> carData, int countData, String sound) {


        LandMarks landMarks = new LandMarks();
        //sound event here
        //switch ()


        ArrayList<Double> az = new ArrayList<>();
        ArrayList<Double> w = new ArrayList<>();
        for (int i = 0; i < carData.size(); i++) {
            az.add(carData.get(i)[2]);
            w.add(carData.get(i)[3]);
        }

        double bumpNow = 0, cornerNow = 0;
        ArrayList<Double> bump = Utils.removeZero(preProcess_avg(az, Setting.bump_wSize));
        if (!bump.isEmpty()) {
            bumpNow = bump.get(bump.size() - 1);
            if (bumpNow > Setting.bump_thres) {
                landMarks.setBump(true);
            }
        }
        ArrayList<Double> corner = Utils.removeZero(preProcess_avg(w, Setting.corner_wSize));
        if (!corner.isEmpty()) {
            cornerNow = corner.get(corner.size() - 1);
            if (cornerNow > Setting.corner_thres) {
                landMarks.setCorner(true);
            }
            //ArrayList<Double> turn = Utils.removeZero(preProcess_avg(w,Setting.turn_wSize));
            if (cornerNow > Setting.turn_thres) {
                if (gradientAvg(corner) < 0) {//防止在阈值周围抖动的现象
                    for (int i = 1; i < 10; i++) {
                        if (corner.get(corner.size() - i) < Setting.turn_thres)
                            return landMarks;
                    }
                }
                double value = w.get(w.size() - 1 - Setting.corner_wSize / 2) < 0 ? -1 : 1;
                landMarks.setTurn(value);
            }
        }
        if (bumpNow > Setting.bump_thres || cornerNow > Setting.corner_thres) {
            if (!landMarking) {
                landMarking = true;
            }
        } else {
            if (landMarking) {
                landMarking = false;
                lastLandMarkEnd = countData;
            }
        }
        return landMarks;
    }

    private ArrayList<Double> detectThreshold(ArrayList<Double> data, int wSize, double threshold, int width) {

        if (!extremum.isEmpty()) extremum.clear();

        ArrayList<Double> detect = preProcess_avg(data, wSize);

        ArrayList<Double> result = new ArrayList<>();
        while (result.size() < data.size()) {//首先补齐长度
            result.add(0d);
        }

        int half = (int) Math.floor(wSize / 2);
        //求局部最大值
        for (int i = half; i < detect.size() - half; i++) {
            double max = 0;
            for (int j = i - half, c1 = 0; c1 < wSize; j++, c1++)
                max = detect.get(j) > max ? detect.get(j) : max;
            if (detect.indexOf(max) > (detect.size() - half - 7) || detect.indexOf(max) < (half + 7))
                continue;
            if (abs(max - detect.get(i)) < Math.pow(10, -30) && detect.get(i) > threshold) {
                extremum.add(i);
                for (int k = i - width / 2, c2 = 0; c2 < width; k++, c2++)
                    result.set(k, 1d);
            }
        }
        return result;
    }

    private ArrayList<Double> preProcess_avg(ArrayList<Double> rawData, int wSize) {
        ArrayList<Double> detect = new ArrayList<>();
        while (detect.size() < rawData.size()) {//首先补齐长度
            detect.add(0d);
        }
        int half = (int) Math.floor(wSize / 2);
        for (int i = half; i < rawData.size() - half; i++) {
            double sum = 0, avg;
            for (int j = i - half, count = 0; count < wSize; j++, count++) {
                sum += abs(rawData.get(j));
            }
            avg = sum / wSize;
            detect.set(i, avg);
        }
        return detect;
    }

    private ArrayList<Double> preProcess_sd(ArrayList<Double> rawData, int wSize) {
        ArrayList<Double> detect = new ArrayList<>();
        while (detect.size() < rawData.size()) {//首先补齐长度
            detect.add(0d);
        }
        int half = (int) Math.floor(wSize / 2);
        double[] temp_az = new double[wSize];
        for (int i = half; i < rawData.size() - half; i++) {
            double sum = 0, avg;
            for (int j = i - half, count = 0; count < wSize; j++, count++) {
                sum += rawData.get(j);
                temp_az[count] = rawData.get(j);
            }
            avg = sum / wSize;
            sum = 0;
            for (double z : temp_az) {
                sum += (z - avg) * (z - avg);
            }
            double az_temp = sum / wSize;
            detect.set(i, az_temp);
        }
        return detect;
    }

    private double gradientAvg(ArrayList<Double> arrayList) {
        int window = 30;
        double sum = 0;
        for (int i = arrayList.size() - 1; i > arrayList.size() - window; i--) {
            sum += arrayList.get(i) - arrayList.get(i - 1);
        }
        return sum / window;
    }

    private void turnDirect(ArrayList<Double> w) {
        if (!extremum.isEmpty()) {
            for (int i = 0; i < extremum.size(); i++) {
                int[] index = getTurnWidth(w, extremum.get(i));
                double value = w.get(extremum.get(i)) < 0 ? -1 : 1;

                //这里考虑到由于车速的不同，转弯的角速度计算精度不同：
                // 当车速快时，角速度精度不高，连续转弯会出现角度跟不上的情况，在这里做一些补偿。
                // 能够有效提高适应性
                int gap = index[1] - index[0];
                if (gap > 400)
                    index[1] = index[0] + 450;
                else if (gap > 350 && gap < 400)
                    index[1] = index[0] + 400;
                else if (gap < 300 && gap > 250)
                    index[1] = index[0] + 200;
                else if (gap < 250 && gap > 200)
                    index[1] = index[0] + 180;
                else if (gap < 200 && gap > 100)
                    index[1] = index[0] + 140;

                for (int k = index[0]; k < index[1]; k++)
                    real_turn.set(k, value);

            }
        }
    }

    /**
     * 提供自适应的turn宽度，从极值点开始，两个指针同时向两边移动，到达给定阈值后，设定为turn的起始点与终止点
     *
     * @param w  角速度
     * @param ex 极值点
     * @return 起始点与终止点
     */
    private int[] getTurnWidth(ArrayList<Double> w, int ex) {
        int[] result = {0, 0};
        int left = ex, right = ex;
        try {
            while (Math.abs(w.get(left)) > Setting.turn_width_thres) {
                left--;
                if (Math.abs(w.get(left)) < 20) {
                    double sum = 0;
                    for (int j = 0; j < 10; j++) {
                        sum += w.get(left - j);
                    }
                    if (abs(w.get(left)) <= abs(sum / 10)) break;
                }
            }
            result[0] = left;
            while (Math.abs(w.get(right)) > Setting.turn_width_thres) {
                right++;
                if (Math.abs(w.get(right)) < 20) {
                    double sum = 0;
                    for (int j = 0; j < 10; j++) {
                        sum += w.get(right + j);
                    }
                    if (abs(w.get(right)) <= abs(sum / 10)) break;
                }
            }
            result[1] = right;
        } catch (Exception e) {
            return result;
        }
        System.out.println("left = " + left + ",right =" + right);
        return result;
    }

    public int getTurn_leftPtr() {
        return turn_leftPtr;
    }

    public double getTurn_value() {
        return turn_value;
    }

    private double[] windowAvg(ArrayList<Double> data, int wSize) {
        int size = data.size() - wSize;
        double[] windowAvg = new double[size];
        double sum = 0;
        int half = (int) Math.floor(wSize / 2);
        for (int i = half; i < size - half; i++) {
            for (int j = i - half; j < wSize; j++) {
                sum += data.get(j);
            }
            windowAvg[i] = sum / wSize;
            sum = 0;
        }
        return windowAvg;
    }

    public boolean isLandMarking() {
        return landMarking;
    }

    public int getLastLandMarkEnd() {
        return lastLandMarkEnd;
    }

    public ArrayList<Integer> getExtremum() {
        return extremum;
    }

    public int getTurnWidth() {
        return turnWidth;
    }

    public ArrayList<Double> getPred_bump() {
        return pred_bump;
    }

    public ArrayList<Double> getPred_corner() {
        return pred_corner;
    }

    public ArrayList<Double> getPred_turn() {
        return pred_turn;
    }

    public ArrayList<Double> getReal_bump() {
        return real_bump;
    }

    public ArrayList<Double> getReal_corner() {
        return real_corner;
    }

    public ArrayList<Double> getReal_turn() {
        return real_turn;
    }
}
