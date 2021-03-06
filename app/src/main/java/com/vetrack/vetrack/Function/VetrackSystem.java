package com.vetrack.vetrack.Function;

import android.util.Log;

import com.vetrack.vetrack.Model.CarData;
import com.vetrack.vetrack.Model.LandMarks;
import com.vetrack.vetrack.Model.MapInfo;
import com.vetrack.vetrack.Model.Particles;
import com.vetrack.vetrack.Model.TraceInfo;
import com.vetrack.vetrack.Utils.DataType.MyFloatList;
import com.vetrack.vetrack.Utils.Setting;
import com.vetrack.vetrack.Utils.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


/**
 * Vetrack
 * Create on 2019/7/14.
 */
public class VetrackSystem {

    private ArrayList<double[][]> particleList;
    private ParticleFilter pf;
    private int countData;
    private Particles particles;
    private MapInfo mapInfo;
    private CarData carDataRT;
    private ArrayList<double[]> carDataAll;
    private Detector detector;
    private ArrayList<LandMarks> landMarksList;
//    private ArrayList<double[]> trajectory;

    //    private int lastReCalculate;
    private boolean needReCal;
    private int lastCredible;
    private ReadWriteLock pfLock;
    private ReadWriteLock carDataLock;
    private boolean runBackEnd;
    private float delV, delTheta;

    public void initialMap(String path) throws IOException, ClassNotFoundException {
        if (mapInfo != null)
            return;
        mapInfo = new MapInfo();
        String mapPath = path + "/map";
        String mapInfoObjPath = mapPath + "/MapInfo4.obj";
        File file = new File(mapInfoObjPath);
        if (file.exists()) {
            try {
                ObjectInputStream in = new ObjectInputStream(new FileInputStream(mapInfoObjPath));
                mapInfo = (MapInfo) in.readObject();
                in.close();
                Log.i("init_map", "true");
            } catch (IOException | ClassNotFoundException e) {
                Log.i("init_map", "false");
                e.printStackTrace();
                throw e;
            }
        } else {
            mapInfo.setProj_x(new MyFloatList(Utils.readList_float(mapPath + "/proj_x.csv")));
            mapInfo.setProj_y(new MyFloatList(Utils.readList_float(mapPath + "/proj_y.csv")));
            mapInfo.setMap(new MyFloatList(Utils.readList_float(mapPath + "/map.csv")));
            mapInfo.setIswall_thick(new MyFloatList(Utils.readList_float(mapPath + "/iswall_thick.csv")));
            mapInfo.setDis_bump(new MyFloatList(Utils.readList_float(mapPath + "/dis_bump.csv")));
            mapInfo.setDis_corner(new MyFloatList(Utils.readList_float(mapPath + "/dis_corner.csv")));
            mapInfo.setDirec_map(new MyFloatList(Utils.readList_float(mapPath + "/direc_map.csv")));
            Log.i("init_map", "true");
            try {
                if (file.createNewFile()) {
                    FileOutputStream outputStream = new FileOutputStream(mapInfoObjPath);//?????????????????????????????????
                    ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
                    objectOutputStream.writeObject(mapInfo);
                    //???????????????????????????objectOutputStream.close()???????????????outputStream?????????????????????????????????????????????objectOutputStream??????
                    objectOutputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
                throw e;
            }
        }
    }

    public void initialTrack(TraceInfo traceInfo) {
        carDataRT = new CarData(true);
        carDataAll = new ArrayList<>();
        detector = new Detector();
        particles = new Particles(Setting.n_ps, traceInfo);
        double hd_vehicle = traceInfo.getInit_theta() * Math.PI / 180;
        particleList = new ArrayList<>();
        particleList.add(particles.cloneParticles().getParticles());
        landMarksList = new ArrayList<>();
        pf = new ParticleFilter(mapInfo, hd_vehicle);
        countData = 0;
        pfLock = new ReentrantReadWriteLock(true);
        carDataLock = new ReentrantReadWriteLock();

//        lastReCalculate = 0;
        needReCal = false;
        runBackEnd = true;

//        Thread backEnd = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                backEnd();
//            }
//        });
//        backEnd.start();
    }

    public void stop() {
        runBackEnd = false;
    }

    public double[][] processData(double[] rawData, float deltaV, float deltaTheta) {
        carDataLock.writeLock().lock();
        try {
            countData++;
            carDataRT.addData(rawData);
            carDataAll.add(carDataRT.getLastCarData());
        } finally {
            carDataLock.writeLock().unlock();
        }

        LandMarks landMarks = null;
        landMarks = detector.detector_predict(carDataRT.getCarData(), countData);
//        LandMarks landMarks = new LandMarks();
//        landMarksList.add(landMarks);
        pfLock.writeLock().lock();
        try {
            particles.setParticles(particleList.get(particleList.size() - 1));
            particles = pf.CalToNow(deltaV, deltaTheta, particles, landMarks, carDataRT.getCarData().get(carDataRT.getSize() - 1));
            delV = deltaV;
            delTheta = deltaTheta;
            particleList.add(particles.cloneParticles().getParticles());
        } finally {
            pfLock.writeLock().unlock();
        }
        return particles.getParticles();
    }

    /**
     * Back End
     */
    private void backEnd() {
        while (runBackEnd) {

            //*********************************************//
            //
//            if (countData != 0 && countData % 2000 == 0) {
//                while (detector.isLandMarking()) {
//                    try {
//                        Thread.sleep(Setting.COLLECT_INTERVAL / 2);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//                rollBack(lastReCalculate);
//                System.out.println("Regular inspection at " + countData);
//                lastReCalculate = countData;
//            }

            //*********************************************//
            //
            if (countData - detector.getLastLandMarkEnd() < 5) {
                rollBack(lastCredible);
            }

            //*********************************************//
            //
//            ArrayList<Integer> resampleList = pf.getResampleList();
//            if (resampleList.size() > 10) {
//                if ((int) getLastElement(resampleList) - resampleList.get(resampleList.size() - 10) < 100) {
//                    System.out.println("resample roll back");
//                    rollBack((int) getLastElement(resampleList) - 500);
//                    resampleList.clear();
//                }
//            }

            //*********************************************//
            //
            if (needReCal) {
                rollBack(0);
                System.out.println("Recalculate at " + countData);
            }

            //*********************************************//
            try {
                Thread.sleep(Setting.COLLECT_INTERVAL / 2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void rollBack(int start) {
        if (countData - lastCredible < 50)
            return;
        System.out.println("roll back start at " + start);
        ArrayList<double[]> carDataRB = new ArrayList<>();
        int countData;
        carDataLock.readLock().lock();
        try {
            countData = this.countData;
            for (int i = start; i < countData; i++) {
                carDataRB.add(carDataAll.get(i));
            }
        } finally {
            carDataLock.readLock().unlock();
        }
        Detector detectorRB = new Detector();
        detectorRB.detector_threshold(carDataRB);
        ArrayList<Double> bump = detectorRB.getReal_bump();
        ArrayList<Double> turn = detectorRB.getReal_turn();
        ArrayList<Double> corner = detectorRB.getReal_corner();
        while (landMarksList.size() < this.countData) {
            LandMarks temp = new LandMarks();
            landMarksList.add(temp);
        }
        for (int i = 0; i < bump.size(); i++) {
            LandMarks temp = new LandMarks();
            temp.setBump(bump.get(i) == 1);
            temp.setTurn(turn.get(i));
            temp.setCorner(corner.get(i) == 1);
            landMarksList.set(start + i, temp);
        }

        Particles pt = new Particles(start, Setting.n_ps);
        double[][] particleLast, particleStart;
        pfLock.readLock().lock();
        try {
            particleStart = particleList.get(start);
            particleLast = particleList.get(particleList.size() - 1);
        } finally {
            pfLock.readLock().unlock();
        }
        pt.setParticles(particleStart);
        ArrayList<double[][]> particleListRB = new ArrayList<>();
        ParticleFilter pft = new ParticleFilter(mapInfo, pt.getParticles()[2][0]);
        for (int i = start; i < countData; i++) {
            pt = pft.CalToNow(delV, delTheta, pt, landMarksList.get(i), carDataAll.get(i));
            particleListRB.add(pt.cloneParticles().getParticles());
        }
        double centerXRB = Utils.mean(pt.getParticles()[0]);
        double centerYRB = Utils.mean(pt.getParticles()[1]);
        double centerX = Utils.mean(particleLast[0]);
        double centerY = Utils.mean(particleLast[1]);
        double distance = Utils.distance(new Double[]{centerXRB, centerYRB}, new Double[]{centerX, centerY});
        if (distance > 7) {
            if (distance > 20 && !needReCal) {
                needReCal = true;
                return;
            } else {
                needReCal = false;
            }
            pfLock.writeLock().lock();
            try {
                for (int i = particleList.size() - 1, j = particleListRB.size() - 1; j > 0; i--, j--) {
                    particleList.set(i, particleListRB.get(j));
                }
                System.out.println("roll back end at " + (particleList.size() - 1) + " and count now is " + this.countData);
            } finally {
                pfLock.writeLock().unlock();
            }
        }
        lastCredible = this.countData;
        System.out.println("last credible point is " + lastCredible);
    }

}
