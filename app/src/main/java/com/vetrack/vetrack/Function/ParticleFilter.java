package com.vetrack.vetrack.Function;


import com.vetrack.vetrack.Model.LandMarks;
import com.vetrack.vetrack.Model.MapInfo;
import com.vetrack.vetrack.Model.Particles;
import com.vetrack.vetrack.Utils.Setting;
import com.vetrack.vetrack.Utils.Utils;

import java.util.ArrayList;
import java.util.Random;

/**
 * Vetrack
 * Create on 2019/5/30.
 */
public class ParticleFilter {

    private int landmark_frenq;       //landmark measuremant in such nubmer of updates
    private int resample_frenq;      //resample once in such nubmer of updates
    private int velocity_frenq;        //velocity test once in such nubmer of updates
    //    private int project_frenq;
    private double T;                    //delta count
    private double ppm;                  //pixels per meter
    private int n_ps;                //number of particles
    private double death_thres;   //weight lower than which is dead
    private double wNoise;
    private double aNoise;
    private double xNoise;
    private double yNoise;
    private double tNoise;
    private double vNoise;
    private double sigma_bump;
    private double sigma_corner;
    private double sigma_turn;
    private double acc_scale;
    private double turnDirect;
    private double hd_Last;
    private boolean isChangingFloor;
    private double floor_now;

    private Random rand;
    private Random randD;
    private Random randI;

    private ArrayList<Double[][]> trace;
//    private ArrayList<Double> oriList;
//    private double oriLast;
//    private int oriWidth;

    //    private double[] data;//加速度数据
//    private double[][] robots;//粒子数组，保存粒子们的当前状态
//    private LandMarks landMarks;
    private MapInfo mapInfo;
    private int count = 0;
    private ArrayList<Integer> resampleList;

    public ParticleFilter(MapInfo mapInfo, double hd_vehicle) {
//        this.data = data;
//        robots = particles.getParticles();
//        this.landMarks = landMarks;
        this.mapInfo = mapInfo;
        landmark_frenq = Setting.landmark_frenq;       //landmark measuremant in such nubmer of updates
        resample_frenq = Setting.resample_frenq;      //resample once in such nubmer of updates
        velocity_frenq = Setting.velocity_frenq;        //velocity test once in such nubmer of updates
        //       project_frenq = 1;
        T = Setting.T;                    //delta t
        ppm = Setting.ppm;                  //pixels per meter
        n_ps = Setting.n_ps;                //number of particles
        death_thres = Setting.death_thres;   //weight lower than which is dead
        wNoise = Setting.wNoise;
        aNoise = Setting.aNoise;
        xNoise = Setting.xNoise;
        yNoise = Setting.yNoise;
        tNoise = Setting.tNoise;
        vNoise = Setting.vNoise;
        sigma_bump = Setting.sigma_bump;
        sigma_corner = Setting.sigma_corner;
        sigma_turn = Setting.sigma_turn;
        acc_scale = Setting.acc_scale;
        rand = new Random();
        randD = new Random();
        randI = new Random();
        count = 0;
        hd_Last = hd_vehicle;
        trace = new ArrayList<>();
        resampleList = new ArrayList<>();
        isChangingFloor = false;
        floor_now = 0;


//        oriList = new ArrayList<>();
//        oriLast = 0;
//        oriWidth = Setting.oriWidth;
    }

    public Particles CalToNow(float deltaV, float deltaTheta, Particles particles, LandMarks landMarks, double[] data) {

        count++;
        double[][] robots = particles.getParticles();
        double[][] temp = new double[7][n_ps];//重采样时的临时数组，与粒子数组同样大小

        /*
          movement update
         */
        double ax = data[0] * acc_scale + aNoise * rand.nextGaussian();
        double ay = data[1] * acc_scale + aNoise * rand.nextGaussian();
        double wz = data[3] + wNoise * rand.nextGaussian();
        //double ori = data[4];

   /*   //
        oriList.add(ori);
        if(count>oriWidth){
            double sum = 0;
            for(int i = 1;i<=oriWidth;i++){
                sum+=oriList.get(count-i);
            }
            if(Math.abs(oriList.get(count-1)-sum/oriWidth)<Math.PI/18)
                oriLast = sum/oriWidth;
        }*/

        for (int i = 0; i < n_ps; i++) {
            robots[0][i] = robots[0][i] + Math.cos(robots[2][i]) * robots[3][i] * T + xNoise * rand.nextGaussian();
            robots[1][i] = robots[1][i] + Math.sin(robots[2][i]) * robots[3][i] * T + yNoise * rand.nextGaussian();
        }

        double[] X = new double[n_ps];
        double[] Y = new double[n_ps];
//        for (int i = 0; i < n_ps; i++) {
//            X[i] = robots[0][i];
//            Y[i] = robots[1][i];
//        }
        for (int i = 0; i < n_ps; i++) {
            X[i] = Math.round(robots[0][i] * ppm);
            Y[i] = Math.round(robots[1][i] * ppm);
        }
        double[] K = new double[n_ps];
        System.arraycopy(robots[6], 0, K, 0, n_ps);
        int mulX = 800;
        int mulK = 960000;

        for (int i = 0; i < n_ps; i++) {
            robots[0][i] = robots[0][i] + mapInfo.getProj_x().get((int) Y[i] + ((int) K[i] - 1) * mulK + ((int) X[i]) * mulX) / ppm;
            robots[1][i] = robots[1][i] + mapInfo.getProj_y().get((int) Y[i] + ((int) K[i] - 1) * mulK + ((int) X[i]) * mulX) / ppm;
        }

        /*
          only update robots(3,:) if needed, update it later
         */
        double[] theta = new double[n_ps];
        for (int i = 0; i < n_ps; i++) {
            theta[i] = robots[4][i] - robots[2][i];
        }
        double[] a = new double[n_ps];
        for (int i = 0; i < n_ps; i++) {
            a[i] = ay * Math.cos(theta[i]) - ax * Math.sin(theta[i]);
//            a[i] = Math.sqrt(ay * ay + ax * ax);
        }

        for (int i = 0; i < n_ps; i++) {
            robots[3][i] = robots[3][i] + deltaV + vNoise * rand.nextGaussian();
            robots[4][i] = Utils.doubleMod(robots[4][i] + deltaTheta, 2 * Math.PI);

            robots[3][i] = robots[3][i] + a[i] * T + vNoise * rand.nextGaussian();

            robots[4][i] = Utils.doubleMod(robots[4][i] + wz * T, 2 * Math.PI);
        }
        /*
        update robots(7,:) ... storey number
         */
        double[] vb = new double[n_ps];
        for (int i = 0; i < n_ps; i++) {
            vb[i] = mapInfo.getMap().get((int) Y[i] + ((int) K[i] - 1) * mulK + ((int) X[i]) * mulX);
        }
        for (int i = 0; i < n_ps; i++) {
            if (vb[i] > 100) {
                robots[6][i] = Utils.doubleMod(vb[i], 100.0);
            }
        }
        double xx = 0, yy = 0, ss = 0;
        for (int i = 0; i < n_ps; i++) {
            xx += robots[0][i];
            yy += robots[1][i];
            ss += robots[6][i];
        }
        double center_x = xx / n_ps;
        double center_y = yy / n_ps;
        double floor = ss / n_ps;
        if (center_x > Setting.changeFloorPoint[0] - 1
                && center_x < Setting.changeFloorPoint[0] + 1
                && center_y < Setting.changeFloorPoint[1] - 3
                && center_y > Setting.changeFloorPoint[1] - 4) {
            isChangingFloor = true;
            System.out.println("Start changing floor, x = " + center_x + "  y = " + center_y);
        }
        if (isChangingFloor && center_x > Setting.newFloorPoint[0] - 1
                && center_x < Setting.newFloorPoint[0] + 1
                && center_y < Setting.newFloorPoint[1] + 1
                && center_y > Setting.newFloorPoint[1] - 1) {
            isChangingFloor = false;
            floor_now = Math.round(floor);
            System.out.println("Finish changing floor, and now is at " + floor_now + " floor");
        }
        if (floor_now == 0) {
            floor_now = floor;
        }

        /*
        weight update
         */
        double[] p = new double[n_ps];
        for (int i = 0; i < n_ps; i++) {
            p[i] = 1;
        }
        X = new double[n_ps];
        Y = new double[n_ps];
        for (int i = 0; i < n_ps; i++) {
            X[i] = Math.round(robots[0][i] * ppm);
            Y[i] = Math.round(robots[1][i] * ppm);
        }
        K = new double[n_ps];
        System.arraycopy(robots[6], 0, K, 0, n_ps);

        double[] isInwall = new double[n_ps];
        for (int i = 0; i < n_ps; i++) {
            isInwall[i] = 1.0 - mapInfo.getIswall_thick().get((int) Y[i] + ((int) K[i] - 1) * mulK + ((int) X[i]) * mulX);
        }
        for (int i = 0; i < n_ps; i++) {
            p[i] = isInwall[i] * p[i];
        }

        //floor weight update
        for (int i = 0; i < n_ps; i++) {
            if (robots[6][i] - floor_now > 1) {
                p[i] *= 0.1;
            }
        }

        //landmarks
        if (count % landmark_frenq == 0) {
            if (landMarks.isBump()) {
                System.out.println("bump detected at " + count);

                double[] dis = new double[n_ps];
                for (int i = 0; i < n_ps; i++) {
                    dis[i] = mapInfo.getDis_bump().get((int) Y[i] + ((int) K[i] - 1) * mulK + ((int) X[i]) * mulX);
                }
                for (int i = 0; i < n_ps; i++) {
                    p[i] = Utils.normpdf(dis[i], 0, sigma_bump) * p[i];
                }
                Double[][] location = new Double[n_ps][2];
                for (int i = 0; i < n_ps; i++) {
                    location[i][0] = robots[0][i];
                    location[i][1] = robots[1][i];
                }
                trace.add(location);
            } else {
                double[] dis = new double[n_ps];
                for (int i = 0; i < n_ps; i++) {
                    dis[i] = mapInfo.getDis_bump().get((int) Y[i] + ((int) K[i] - 1) * mulK + ((int) X[i]) * mulX);
                }
                for (int i = 0; i < n_ps; i++) {
                    if (dis[i] < 1) {
                        p[i] *= 0.99;
                    }
                }
            }
            if (landMarks.isCorner()) {
                System.out.println("corner detected at " + count);
                double[] dis = new double[n_ps];
                for (int i = 0; i < n_ps; i++) {
                    dis[i] = mapInfo.getDis_corner().get((int) Y[i] + ((int) K[i] - 1) * mulK + ((int) X[i]) * mulX);
                }
                for (int i = 0; i < n_ps; i++) {
                    p[i] = Utils.normpdf(dis[i], 0, sigma_corner) * p[i];
                }
                Double[][] location = new Double[n_ps][2];
                for (int i = 0; i < n_ps; i++) {
                    location[i][0] = robots[0][i];
                    location[i][1] = robots[1][i];
                }
                trace.add(location);
            }
            //trace alignment
            for (int i = 0; i < n_ps; i++) {
                Double[] location = {robots[0][i], robots[1][i]};
//                if (trace.size() >= 1 && Utils.distance(location, trace.get(trace.size() - 1)[i]) < 1)
//                    p[i] = p[i] * 0.5;
                if (trace.size() >= 4 && Utils.distance(location, trace.get(trace.size() - 4)[i]) < 2)
                    p[i] = p[i] * 0.5;
            }
        }
        //update robots(3,:)
        if (landMarks.getTurn() != 0) {
            turnDirect += landMarks.getTurn();
            // if the vehicle is near a corner, we consider the change of heading
            // direction of the phone is caused by the turning of the vehicle
            //double sum_angle = 0;
            for (int i = 0; i < n_ps; i++) {
                robots[2][i] = Utils.doubleMod(robots[2][i] + wz*T + tNoise * rand.nextGaussian(), 2 * Math.PI);
                //sum_angle += robots[2][i];
            }
            //System.out.println("turn detected at " + count + ":" + sum_angle / n_ps);
        } else {
            if (turnDirect != 0) {
                double sum = 0;
                int count_t = 0;
                double angle;
                //if((Math.PI*5/6)<Math.abs(ori-oriLast)&&Math.abs(ori-oriLast)<(Math.PI*7/6))
                if (Math.abs(turnDirect) > Setting.turnBack_thres) {
                    angle = Math.PI;
                } else
                    angle = Math.PI / 2;
                if (turnDirect < 0) {
                    hd_Last = (hd_Last == 0 ? 2 * Math.PI : hd_Last);
                    for (int i = 0; i < n_ps; i++) {
                        if (Math.abs(hd_Last - robots[2][i]) > Setting.turnThres_PF) {
                            //p[i] = Utils.normpdf(Math.abs(hd_Last - robots[2][i]), angle, sigma_turn) * p[i];
                            robots[2][i] = hd_Last - angle;
                            sum += robots[2][i];
                            count_t++;
                        } else {
                            p[i] *= 0.5;
                        }
                    }
                    if (sum != 0 && count_t > n_ps * 0.7)
                        hd_Last = Math.round((sum / count_t) / (Math.PI / 2)) * (Math.PI / 2);
                    else {
                        hd_Last = hd_Last - angle;
                        for (int i = 0; i < n_ps; i++)
                            robots[2][i] = hd_Last;
                    }
                } else {
                    for (int i = 0; i < n_ps; i++) {
                        if (Math.abs(robots[2][i] - hd_Last) > Setting.turnThres_PF) {
                            //p[i] = Utils.normpdf(Math.abs(hd_Last - robots[2][i]), angle, sigma_turn) * p[i];
                            robots[2][i] = hd_Last + angle;
                            sum += robots[2][i];
                            count_t++;
                        } else {
                            p[i] *= 0.5;
                        }
                    }
                    if (sum != 0 && count_t > n_ps * 0.7)
                        hd_Last = Math.round((sum / count_t) / (Math.PI / 2)) * (Math.PI / 2);
                    else {
                        hd_Last = hd_Last + angle;
                        for (int i = 0; i < n_ps; i++)
                            robots[2][i] = hd_Last;
                    }
                    if (hd_Last > 2 * Math.PI) hd_Last -= 2 * Math.PI;
                }
                turnDirect = 0;
                System.out.println("hd_vehicle(robot[2]) is now at " + count + ",hd_now=" + hd_Last + ",robot[2][0]=" + robots[2][0]);
                if (isChangingFloor) {
                    for (int i = 0; i < n_ps; i++) {
                        robots[0][i] = Setting.newFloorPoint[0] + xNoise * rand.nextGaussian();
                        robots[1][i] = Setting.newFloorPoint[1] + yNoise * rand.nextGaussian();
                    }
                }
            }
//                for (int i = 0; i < n_ps; i++) {
//                    double tmp = Math.cos(robots[2][i] - mapInfo.getDirec_map().get((int) Y[i] + ((int) K[i] - 1) * mulK + ((int) X[i]) * mulX));
//                    tmp = tmp * tmp;
//                    p[i] = p[i] * Math.min(1.5 * tmp, 1.0);
//                    if (Math.cos(robots[2][i] - mapInfo.getDirec_map().get((int) Y[i] + ((int) K[i] - 1) * mulK + ((int) X[i]) * mulX)) > 0) {
//                        robots[2][i] = mapInfo.getDirec_map().get((int) Y[i] + ((int) K[i] - 1) * mulK + ((int) X[i]) * mulX);
//                    } else {
//                        robots[2][i] = mapInfo.getDirec_map().get((int) Y[i] + ((int) K[i] - 1) * mulK + ((int) X[i]) * mulX) + Math.PI;
//                    }
//                }
            if (!isChangingFloor) {
                if (hd_Last % Math.PI == 0) {
                    for (int i = 0; i < n_ps; i++) {
                        if (mapInfo.getMap().get((int) Y[i] + ((int) K[i] - 1) * mulK + ((int) X[i] + 1) * mulX) == 0 &&
                                mapInfo.getMap().get((int) Y[i] + ((int) K[i] - 1) * mulK + ((int) X[i] - 1) * mulX) == 0)
                            p[i] *= 0.7;
                    }
                } else {
                    for (int i = 0; i < n_ps; i++) {
                        if (mapInfo.getMap().get((int) Y[i] + 1 + ((int) K[i] - 1) * mulK + ((int) X[i]) * mulX) == 0 &&
                                mapInfo.getMap().get((int) Y[i] - 1 + ((int) K[i] - 1) * mulK + ((int) X[i]) * mulX) == 0)
                            p[i] *= 0.7;
                    }
                }
            }
        }
        //velocity model()
        if (count % velocity_frenq == 0) {
            for (int i = 0; i < n_ps; i++) {
                if (robots[3][i] < 0.8) p[i] = p[i] * 0.5;
                if (robots[3][i] < 0.4) p[i] = p[i] * 0.5;
                if (robots[3][i] < 0.2) p[i] = p[i] * 0.5;
                if (robots[3][i] < 0.1) p[i] = p[i] * 0.5;
                if (robots[3][i] < 0.0) p[i] = 0;
            }
            for (int i = 0; i < n_ps; i++) {
                if (robots[3][i] < 0) {
                    robots[3][i] = 0;
                }
            }
        }
        //TO BE CONTINUED
        for (int i = 0; i < n_ps; i++) {
            robots[5][i] *= p[i];
        }
        double sum = 0;
        for (int i = 0; i < n_ps; i++) {
            sum += robots[5][i];
        }
        for (int i = 0; i < n_ps; i++) {
            robots[5][i] /= sum;
        }
        int death = 0;
        for (int i = 0; i < n_ps; i++) {
            if (robots[5][i] < death_thres) death++;
        }
        /*
        Resampling
         */
        if (count % resample_frenq == 0 || death > 0.05 * n_ps) {
            System.out.println("resampling at " + count);
            resampleList.add(count);
            double maxW = Double.NEGATIVE_INFINITY;
            for (int i = 0; i < n_ps; i++) {
                maxW = Math.max(maxW, robots[5][i]);
            }
            double beta = 0;
            int index = randI.nextInt(n_ps);
            for (int i = 1; i <= n_ps; i++) {
                beta = beta + randD.nextDouble() * 2 * maxW;
                while (beta > robots[5][index]) {
                    beta = beta - robots[5][index];
                    index++;
                    if (index >= n_ps)
                        index = 0;
                }
                for (int j = 0; j < 7; j++) {
                    temp[j][i - 1] = robots[j][index];
                }
            }
            for (int i = 0; i < 7; i++) {
                System.arraycopy(temp[i], 0, robots[i], 0, temp[i].length);
            }
        }
        particles.setParticles(robots);
        return particles;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public ArrayList<Integer> getResampleList() {
        return resampleList;
    }

    public void clearResampleList() {
        resampleList.clear();
    }
}
