package com.vetrack.vetrack;

import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.widget.Switch;
import android.widget.Toast;

import com.vetrack.vetrack.Function.Detector;
import com.vetrack.vetrack.Function.DrawPoint;
import com.vetrack.vetrack.Function.ParticleFilter;
import com.vetrack.vetrack.Model.CarData;
import com.vetrack.vetrack.Model.LandMarks;
import com.vetrack.vetrack.Model.MapInfo;
import com.vetrack.vetrack.Model.Particles;
import com.vetrack.vetrack.Model.TraceInfo;
import com.vetrack.vetrack.Service.Network;
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

import Jama.Matrix;

public class DatasetActivity extends AppCompatActivity {

    private static final String TAG = "DataSetActivity";
    private static final int INIT_FINISHED = 0x00010001;
    private static final int DRAW_POINT = 0x00010002;
    private String path = Environment.getExternalStorageDirectory().getPath() + "/vetrack";

    //private FrameLayout frameLayout;
    private ImageView imageView;
    private Button btn_start;
    private DrawPoint drawPoint;
    private Bitmap resultBm;
    private EditText et_parking;
    private EditText et_pose;
    private EditText et_trace;
    //private EditText et_aScale;
    private boolean run;
    private boolean initialized;

    private Particles particles;
    private MapInfo mapInfo;
    private CarData carData;
    private ArrayList<LandMarks> landMarks;
    private final int n_ps = Setting.n_ps;
    //private double acc_scale;
    private double hd_vehicle;

    //Fang Added
    private Switch privacyBtn;
    private int privacyState = -1, drivingState = 0; //0->drive 1->park
    private Bitmap bitmapFromServer;
    private final int REFRESH_MAP = 2;
    private Button pdBtn;
    private Boolean mapInit = false;
    private double[] current1;
    private double[] current2;
    private double[] current3;
    private ImageView imageView4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dataset);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        imageView = findViewById(R.id.imaV_map);
        btn_start = findViewById(R.id.btn_start);
        imageView4 = findViewById(R.id.imageView4);
        //et_aScale = findViewById(R.id.et_aScale);
        run = false;
        initialized = false;
        //frameLayout = findViewById(R.id.frameL_trace);
        setWindow();

        if ((!initialized)) {
            Toast.makeText(DatasetActivity.this,
                    "Initializing...",
                    Toast.LENGTH_SHORT).show();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    initialMap();
                    initial();
                    Log.d("information==========>",
                            "init has been finished!");
                    myHandler.sendEmptyMessage(INIT_FINISHED);
                }
            }).start();
        }

        privacyBtn = findViewById(R.id.privacySwitchBtn);
        privacyState = Network.getInstance().getPrivacyState();
        if (privacyState == 0) {
            privacyBtn.setText("????????????");
        } else {
            privacyBtn.setText("????????????");
            privacyBtn.setChecked(true);
        }
        privacyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (privacyState == 0) {
                    privacyState = 1;
                    Network.getInstance().setPrivacyState(1);
                    privacyBtn.setText("????????????");
                } else {
                    privacyState = 0;
                    Network.getInstance().setPrivacyState(0);
                    privacyBtn.setText("????????????");
                }
            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String map = Network.getInstance().download("getMap");
                    byte[] bitmapByte = Base64.decode(map, Base64.DEFAULT);
                    bitmapFromServer = BitmapFactory.decodeByteArray(bitmapByte, 0, bitmapByte.length);
                    Log.d("mapSize", String.valueOf(bitmapFromServer == null));
                    if (bitmapFromServer != null) {
//                        Message message = new Message();
//                        message.what = REFRESH_MAP;
//                        myHandler.sendMessage(message);
                    }
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }


            }
        }).start();



        pdBtn = findViewById(R.id.pdBtn);
        pdBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (drivingState == 0) { //press to park
                    pdBtn.setText("????????????");
                    drivingState = 1;
                    if (Network.getInstance().getPrivacyState() == 0) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    String dataString = null;

                                    String fileName = "vetrack"+Math.random();
                                    Network.getInstance().upload(fileName, dataString);
                                    Toast.makeText(DatasetActivity.this, "file Uploaded", Toast.LENGTH_LONG).show();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }).start();
                    }
                    // TODO: 4/1/2021 draw a car point here
                    imageView4.setVisibility(View.VISIBLE);

                } else {
                    pdBtn.setText("????????????");
                    drivingState = 0;
                    // TODO: 4/1/2021 remove car
                    imageView4.setVisibility(View.INVISIBLE);
                }

            }
        });
    }

    @SuppressLint("HandlerLeak")
    Handler myHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case INIT_FINISHED:
                    Toast.makeText(DatasetActivity.this,
                            "Initialization has been finished!",
                            Toast.LENGTH_SHORT).show();
                    initialized = true;
                    run = true;
                    btn_start.setText("Stop");
                    btn_start.setClickable(true);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {

                            ParticleFilter pf = new ParticleFilter(mapInfo, hd_vehicle);
                            double[][] robots;
                            drawPoint = new DrawPoint(DatasetActivity.this, n_ps);
                            int len = carData.getSize();
                            int count = 0;
                            for (int i = 0; i < len; i++) {
                                particles = pf.CalToNow(0,0,particles, landMarks.get(i), carData.getCarData().get(i));
                                count++;
                                if (count > 20) {
                                    robots = particles.getParticles();
                                    resultBm = drawPoint.setPoints(robots[0], robots[1], robots[6]);
                                    current1 = robots[0];
                                    current2 = robots[1];
                                    current3 = robots[6];
                                    try {
                                        Thread.sleep(200);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                    myHandler.sendEmptyMessage(DRAW_POINT);
                                    count = 0;
                                }
                                //Log.i(TAG,"process : "+i);
                                if (!run) {
                                    clear();
                                    break;
                                }
                            }
                            clear();
                        }
                    }).start();
                    break;
                case DRAW_POINT:
                    imageView.setImageBitmap(resultBm);
                    break;
                case REFRESH_MAP:
                    imageView.setImageBitmap(bitmapFromServer);
                    Log.d("MAP", "MAP refreshed");
            }
        }
    };


    private void clear() {
        particles = null;
        carData = null;
        landMarks = null;
    }

    @Override
    protected void onStop() {
        super.onStop();
        clear();
        mapInfo = null;
    }

    private void initialMap() {
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
            }
        } else {
            mapInfo.setProj_x(new MyFloatList(Utils.readList_float(mapPath + "/proj_x4.csv")));
            mapInfo.setProj_y(new MyFloatList(Utils.readList_float(mapPath + "/proj_y4.csv")));
            mapInfo.setMap(new MyFloatList(Utils.readList_float(mapPath + "/map4.csv")));
            mapInfo.setIswall_thick(new MyFloatList(Utils.readList_float(mapPath + "/iswall_thick4.csv")));
            mapInfo.setDis_bump(new MyFloatList(Utils.readList_float(mapPath + "/dis_bump4.csv")));
            mapInfo.setDis_corner(new MyFloatList(Utils.readList_float(mapPath + "/dis_corner4.csv")));
            mapInfo.setDirec_map(new MyFloatList(Utils.readList_float(mapPath + "/direc_map4.csv")));
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
            }
        }

    }

    private void initial() {
        String tracePath = path + "/trace";
        int parking=4, pose=2, trace=15;

        TraceInfo traceInfo = new TraceInfo(tracePath, parking, pose, trace);
        carData = new CarData(false);
        particles = new Particles(n_ps, traceInfo);
        hd_vehicle = traceInfo.getInit_theta() * Math.PI / 180;

        Matrix data = Utils.readMatrix(tracePath + "/" + parking + pose + trace + ".csv");
        for (int i = 0; i < data.getRowDimension(); i++) {
            double[] ttt = data.getArray()[i];
            carData.addData(ttt);
        }

        Detector detector = new Detector();
//        detector.detector_LR(carData.getCarData(), 1);
//        detector.detector_LR(carData.getCarData(), 2);
//        detector.detector_LR(carData.getCarData(), 3);
//        detector.detector_LR_predict(carData.getCarData(), 1);
//        detector.detector_LR_predict(carData.getCarData(), 2);
//        detector.detector_LR_predict(carData.getCarData(), 3);
        detector.detector_threshold(carData.getCarData());

        ArrayList<Double> bump = detector.getReal_bump();
        ArrayList<Double> turn = detector.getReal_turn();
        ArrayList<Double> corner = detector.getReal_corner();
        landMarks = new ArrayList<>();
        for (int i = 0; i < bump.size(); i++) {
            LandMarks temp = new LandMarks();
            temp.setBump(bump.get(i) == 1);
            temp.setTurn(turn.get(i));
            temp.setCorner(corner.get(i) == 1);
            landMarks.add(temp);
        }
    }

    private void setWindow() {
        //??????????????????
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
}