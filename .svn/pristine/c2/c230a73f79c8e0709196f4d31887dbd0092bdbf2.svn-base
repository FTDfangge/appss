package com.vetrack.vetrack.Model;


import com.vetrack.vetrack.Utils.Utils;

import Jama.Matrix;

public class TraceInfo {
    private double init_x;
    private double init_y;
    private double init_sn;
    private double init_theta;
    private double end_x;
    private double end_y;
    private double end_sn;
    private double end_theta;

    public TraceInfo() {
        init_x = init_y = init_sn = init_theta = 0;
    }

    public TraceInfo(String path, int parking, int pose, int trace) {
        Matrix data = Utils.readMatrix(path + "/parking" + parking + "pose" + pose + "traceinfo" + trace + ".txt");
        init_x = data.get(0, 0);
        init_y = data.get(0, 1);
        init_theta = data.get(0, 2);
        init_sn = data.get(0, 3);
        end_x = data.get(0, 4);
        end_y = data.get(0, 5);
        end_theta = data.get(0, 6);
        end_sn = data.get(0, 7);
    }

    public double getInit_x() {
        return init_x;
    }

    public void setInit_x(double init_x) {
        this.init_x = init_x;
    }

    public double getInit_y() {
        return init_y;
    }

    public void setInit_y(double init_y) {
        this.init_y = init_y;
    }

    public double getInit_sn() {
        return init_sn;
    }

    public void setInit_sn(double init_sn) {
        this.init_sn = init_sn;
    }

    public double getInit_theta() {
        return init_theta;
    }

    public void setInit_theta(double init_theta) {
        this.init_theta = init_theta;
    }

    public double getEnd_x() {
        return end_x;
    }

    public void setEnd_x(double end_x) {
        this.end_x = end_x;
    }

    public double getEnd_y() {
        return end_y;
    }

    public void setEnd_y(double end_y) {
        this.end_y = end_y;
    }

    public double getEnd_sn() {
        return end_sn;
    }

    public void setEnd_sn(double end_sn) {
        this.end_sn = end_sn;
    }

    public double getEnd_theta() {
        return end_theta;
    }

    public void setEnd_theta(double end_theta) {
        this.end_theta = end_theta;
    }
}
