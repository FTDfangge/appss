package com.vetrack.vetrack.Model;


import java.util.Random;

public class Particles {
    private int id;
    private int size;
    private double[][] robots;

    public Particles(int id, int size) {
        robots = new double[7][size];//[x;y;hd_vehicle;velocity;hd_phone;p;storey number]
        this.id = id;
        this.size = size;
    }

    public Particles(int id, int size, double[][] robots) {
        this.id = id;
        this.size = size;
        this.robots = robots;
    }

    public Particles(int size, TraceInfo traceInfo) {
        this.size = size;
        Random rand = new Random();
        robots = new double[7][size];
        double init_x = traceInfo.getInit_x();
        double init_y = traceInfo.getInit_y();
        double init_theta = traceInfo.getInit_theta() * Math.PI / 180;
        double init_sn = traceInfo.getInit_sn();
        for (int i = 0; i < size; i++) {
            robots[0][i] = rand.nextGaussian() + init_x;
            robots[1][i] = rand.nextGaussian() + init_y;
            robots[2][i] = init_theta;
            robots[3][i] = rand.nextInt(3);
            robots[4][i] = init_theta;
            robots[5][i] = 1;
            robots[6][i] = init_sn;
        }
    }

    public void setParticles(double[][] source) {
        for (int i = 0; i < 7; i++) {
            System.arraycopy(source[i], 0, robots[i], 0, source[i].length);
        }
//        robots = source;
    }

    public double[][] getParticles() {
        return robots;
    }

    public int getId() {
        return id;
    }

    public Particles cloneParticles() {
        double[][] robots = new double[7][size];
        for (int i = 0; i < size; i++) {
            robots[0][i] = this.robots[0][i];
            robots[1][i] = this.robots[1][i];
            robots[2][i] = this.robots[2][i];
            robots[3][i] = this.robots[3][i];
            robots[4][i] = this.robots[4][i];
            robots[5][i] = this.robots[5][i];
            robots[6][i] = this.robots[6][i];
        }
        return new Particles(id, size, robots);
    }
}
