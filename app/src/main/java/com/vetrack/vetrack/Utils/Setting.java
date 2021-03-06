package com.vetrack.vetrack.Utils;

/**
 * Vetrack
 * Create on 2019/6/23.
 */
public interface Setting {
    int landmark_frenq = 20;       //landmark measuremant in such nubmer of updates
    int resample_frenq = 100;      //resample once in such nubmer of updates
    int velocity_frenq = 5;        //velocity test once in such nubmer of updates
    int trace_freq = 10;
    //    int project_frenq;
    double T = 0.02;                    //delta count
    double ppm = 10;                  //pixels per meter
    int n_ps = 200;                //number of particles
    double death_thres = 0.01 / n_ps;   //weight lower than which is dead
    double wNoise = 0;
    double aNoise = 0.05;
    double xNoise = 0.1;
    double yNoise = 0.1;
    double tNoise = 0.01;
    double vNoise = 0.05;
    double sigma_bump = 3 * ppm;
    double sigma_corner = 7 * ppm;
    double sigma_turn = 1 * ppm;
    double acc_scale = 100;

    int bump_wSize = 80;
    double bump_thres = 0.43;
    int bump_width = 80;
    int corner_wSize = 100;
    double corner_thres = 30;
    int corner_width = 80;
    int turn_wSize = 100;
    double turn_thres = 30;
    int turn_width_thres = 35;//
    int turnBack_thres = 370;
    double turnThres_PF = Math.PI / 3;

    int oriWidth = 120;

    int COLLECT_INTERVAL = 10;
    int realTimeWidth = 500;
    int realTimeDetectFreq = 40;

    int[] changeFloorPoint = {86, 45};
    int[] newFloorPoint = {70, 45};

    int mapHeight = 960;
    int mapWidth = 960;

}
