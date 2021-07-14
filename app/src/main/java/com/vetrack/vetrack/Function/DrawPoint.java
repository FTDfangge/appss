package com.vetrack.vetrack.Function;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.Log;
import android.view.View;

import com.vetrack.vetrack.R;
import com.vetrack.vetrack.Utils.Setting;


public class DrawPoint extends View {

    Paint paint_all = new Paint();
    Paint paint_center = new Paint();

    private boolean showAllPoints = true;
    private int n_ps;
    private int sn;
//    private double[] x;
//    private double[] y;

    private Bitmap bitmap;

    public DrawPoint(Context context, int n_ps) {
        super(context);

        this.n_ps = n_ps;
        paint_center.setStrokeJoin(Paint.Join.ROUND);
        paint_center.setStrokeCap(Paint.Cap.ROUND);
        paint_center.setStrokeWidth(40);
        paint_center.setColor(Color.RED);// 设置红色
        paint_center.setAntiAlias(true);// 设置画笔的锯齿效果。

        paint_all.setStrokeJoin(Paint.Join.ROUND);
        paint_all.setStrokeCap(Paint.Cap.ROUND);
        paint_all.setStrokeWidth(5);
        paint_all.setColor(Color.BLUE);
        paint_all.setAntiAlias(true);
    }

    private void refreshMap() {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.map1);
        switch (sn) {
            case 2:
                bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.map2);
                break;
            case 3:
                bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.map3);
                break;
        }
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        int newWidth = 960;
        int newHeight = 960;

        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;

        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        this.bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
    }

    public Bitmap setPoints(double[] x, double[] y, double[] sn) {
//        this.x = x;
//        this.y = y;
        double xx = 0, yy = 0, ss = 0;
        for (int i = 0; i < x.length; i++) {
            xx += x[i];
            yy += y[i];
            ss += sn[i];
        }
        double center_x = xx / n_ps;
        double center_y = yy / n_ps;
        //Log.i("x and y","x = "+center_x+";y = "+center_y);
        this.sn = (int) Math.round(ss / n_ps);
        refreshMap();
        Canvas canvas = new Canvas(bitmap);
//        canvas.drawPoint((float) Math.max(Math.min(center_x * 10, 960 - 1), 0),
//                        Math.max(Math.min((float) 960.0 - (float) center_y * 10, 960 - 1), 0),
//                        paint_center);
        canvas.drawPoint((float)Math.max(Math.min(center_x, 880),80), (float)Math.max(Math.min(center_y,880),80), paint_center);
        Log.d("Draw", (float)Math.max(Math.min(center_x, 880),80)+"-"+(float)Math.max(Math.min(center_y,880),80));
//        if (showAllPoints) {
//            for (int i = 0; i < n_ps; i++)
//                canvas.drawPoint((float) Math.max(Math.min(x[i] * 10, 1200 - 1), 0), Math.max(Math.min((float) 800.0 - (float) y[i] * 10, 800 - 1), 0), paint_all);
//        }



        return bitmap;
    }

    public void setShowAllPoints(boolean showAllPoints) {
        this.showAllPoints = showAllPoints;
    }

    public boolean isShowAllPoints() {
        return showAllPoints;
    }
}

