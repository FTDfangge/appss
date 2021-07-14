package com.vetrack.vetrack.Utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.View;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;

import Jama.Matrix;

/**
 * Vetrack
 * Create on 2019/5/28.
 */
public class Utils {
    //Baidu Map utils
    public final static String CoorType_GCJ02 = "gcj02";
    public final static String CoorType_BD09LL = "bd09ll";
    public final static String CoorType_BD09MC = "bd09";

    /***
     *61 ： GPS定位结果，GPS定位成功。
     *62 ： 无法获取有效定位依据，定位失败，请检查运营商网络或者wifi网络是否正常开启，尝试重新请求定位。
     *63 ： 网络异常，没有成功向服务器发起请求，请确认当前测试手机网络是否通畅，尝试重新请求定位。
     *65 ： 定位缓存的结果。
     *66 ： 离线定位结果。通过requestOfflineLocaiton调用时对应的返回结果。
     *67 ： 离线定位失败。通过requestOfflineLocaiton调用时对应的返回结果。
     *68 ： 网络连接失败时，查找本地离线定位时对应的返回结果。
     *161： 网络定位结果，网络定位定位成功。
     *162： 请求串密文解析失败。
     *167： 服务端定位失败，请您检查是否禁用获取位置信息权限，尝试重新请求定位。
     *502： key参数错误，请按照说明文档重新申请KEY。
     *505： key不存在或者非法，请按照说明文档重新申请KEY。
     *601： key服务被开发者自己禁用，请按照说明文档重新申请KEY。
     *602： key mcode不匹配，您的ak配置过程中安全码设置有问题，请确保：sha1正确，“;”分号是英文状态；且包名是您当前运行应用的包名，请按照说明文档重新申请KEY。
     *501～700：key验证失败，请按照说明文档重新申请KEY。
     */

    public static float[] EARTH_WEIGHT = {0.1f, 0.2f, 0.4f, 0.6f, 0.8f}; // 推算计算权重_地球
    //public static float[] MOON_WEIGHT = {0.0167f,0.033f,0.067f,0.1f,0.133f};
    //public static float[] MARS_WEIGHT = {0.034f,0.068f,0.152f,0.228f,0.304f};
    public final static int RECEIVE_TAG = 1;
    public final static int DIAGNOSTIC_TAG = 2;


    public static Object getLastElement(ArrayList arrayList) {
        ArrayList obj = (ArrayList) arrayList.clone();
        return obj.get(obj.size() - 1);
    }

    public static ArrayList<Double> removeZero(ArrayList<Double> arrayList) {
        Iterator it = arrayList.iterator();
        while (it.hasNext()) {
            if ((double) it.next() == 0) {
                it.remove();
            }
        }
        return arrayList;
    }

    public static double[][] transpose(double[][] A) {
        int r = A.length, c = A[0].length;
        double[][] ans = new double[c][r];
        for (int i = 0; i < r; i++)
            for (int j = 0; j < c; j++)
                ans[j][i] = A[i][j];
        return ans;
    }

    public static double mean(double[] A) {
        double sum = 0;
        for (int i = 0; i < A.length; i++) {
            sum += A[i];
        }
        return sum / A.length;
    }

    public static double distance(Double[] p1, Double[] p2) {
        return Math.sqrt((p1[0] - p2[0]) * (p1[0] - p2[0]) + (p1[1] - p2[1]) * (p1[1] - p2[1]));
    }

    public static double normpdf(double x, double mu, double sigma) {
        //return 1.0 / Math.sqrt(2 * Math.PI) / sigma * Math.exp(-1.0 * Math.pow(x - mu, 2) / 2 / Math.pow(sigma, 2));
        return Math.exp(-0.5 * Math.pow((x - mu) / sigma, 2)) / (Math.sqrt(2 * Math.PI) * sigma);
    }

    public static ArrayList<Double> ma2list(double[][] m) {
        ArrayList<Double> l = new ArrayList<Double>();
        for (int i = 0; i < m[0].length; i++) {
            for (int j = 0; j < m.length; j++) {
                l.add(m[j][i]);
            }
        }
        return l;
    }

    public static double doubleMod(double a, double b) {
        double res = Math.floor(a / b);
        return a - res * b;
    }

    private static int getColumnD(String s) {
        return s.split(",").length;
    }

    public static double findMaxElement(Matrix m) {
        double res = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < m.getRowDimension(); i++) {
            for (int j = 0; j < m.getColumnDimension(); j++) {
                res = Math.max(res, m.getArray()[i][j]);
            }
        }
        return res;
    }

    public static byte[] readList_byte(String fileName) {
        File file = new File("/storage/emulated/0/TraceData/" + fileName);
        BufferedReader reader = null;
        Matrix matrix = null;
        byte[] al = new byte[1200 * 800 * 3];
        try {
            System.out.println("读数组");
            reader = new BufferedReader(new FileReader(file));
            String tempString = null;
            int ptr = 0;
            // 一次读入一行，直到读入null
            while ((tempString = reader.readLine()) != null) {
                String[] sourceStrArray = tempString.split(",");
                for (int i = 0; i < sourceStrArray.length; i++) {
                    al[ptr++] = (Byte.valueOf(sourceStrArray[i]));
                }

            }

            //matrix.print(0, 10);
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                }
            }
        }
        return al;
    }

    public static float[] readList_float(String fileName) {
        File file = new File(fileName);
        BufferedReader reader = null;
        Matrix matrix = null;
        float[] al = new float[1200 * 800 * 3];
        try {
            System.out.println("读数组");
            reader = new BufferedReader(new FileReader(file));
            String tempString = null;
            int ptr = 0;
            // 一次读入一行，直到读入null
            while ((tempString = reader.readLine()) != null) {
                String[] sourceStrArray = tempString.split(",");
                for (int i = 0; i < sourceStrArray.length; i++) {
                    al[ptr++] = Float.valueOf(sourceStrArray[i]);
                }

            }

            //matrix.print(0, 10);
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                }
            }
        }
        return al;
    }

    public static String writeMatrix(File path, String fileName, Matrix info) {
        try {
            File dir = new File(path.getAbsoluteFile(), "TraceData");
            dir.mkdir();
            File file = new File(dir.getAbsoluteFile(), fileName);
            BufferedWriter bw = new BufferedWriter(new FileWriter(file, true));
            Log.d("ROW", info.getRowDimension() + "");
            for (int i = 0; i < info.getRowDimension(); i++) {
                String temp = "";
                for (int j = 0; j < info.getColumnDimension(); j++) {
                    if (j == 0)
                        temp += info.get(i, j);
                    else
                        temp = temp + "," + info.get(i, j);
                }
                bw.write(temp);
                bw.newLine();
            }

            bw.flush();
            bw.close();
            return dir.getAbsoluteFile().toString() + fileName;
        } catch (Exception e) {
            Log.e("WRITE_FILE_ERROR", e.getMessage());
            e.printStackTrace();
            return "error";
        }
    }

    public static String writeList(File path, String fileName, ArrayList<double[]> info) {
        try {
            File dir = new File(path.getAbsoluteFile(), "TraceData");
            dir.mkdir();
            File file = new File(dir.getAbsoluteFile(), fileName);
            BufferedWriter bw = new BufferedWriter(new FileWriter(file, true));
            for (int i = 0; i < info.size(); i++) {
                String temp = "";
                for (int j = 0; j < info.get(i).length; j++) {
                    if (j == 0)
                        temp += info.get(i)[j];
                    else
                        temp = temp + "," + info.get(i)[j];
                }
                bw.write(temp);
                bw.newLine();
            }

            bw.flush();
            bw.close();
            return dir.getAbsoluteFile().toString() + fileName;
        } catch (Exception e) {
            Log.e("WRITE_FILE_ERROR", e.getMessage());
            e.printStackTrace();
            return "error";
        }
    }

    public static ArrayList<Double> readList(String fileName) {
        File file = new File("/storage/emulated/0/TraceData/" + fileName);
        BufferedReader reader = null;
        Matrix matrix = null;
        ArrayList<Double> al = new ArrayList<Double>(1200 * 800 * 3);
        try {
            System.out.println("读数组");
            reader = new BufferedReader(new FileReader(file));
            String tempString = null;

            // 一次读入一行，直到读入null
            while ((tempString = reader.readLine()) != null) {
                String[] sourceStrArray = tempString.split(",");
                for (int i = 0; i < sourceStrArray.length; i++) {
                    al.add(Double.valueOf(sourceStrArray[i]));
                }

            }

            //matrix.print(0, 10);
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                }
            }
        }
        return al;
    }

    public static short[] readList_short(String fileName) {
        File file = new File("/storage/emulated/0/TraceData/" + fileName);
        BufferedReader reader = null;
        Matrix matrix = null;
        short[] al = new short[1200 * 800 * 3];
        try {
            System.out.println("读数组");
            reader = new BufferedReader(new FileReader(file));
            String tempString = null;
            int ptr = 0;
            // 一次读入一行，直到读入null
            while ((tempString = reader.readLine()) != null) {
                String[] sourceStrArray = tempString.split(",");
                for (int i = 0; i < sourceStrArray.length; i++) {
                    al[ptr++] = (Short.valueOf(sourceStrArray[i]));
                }

            }

            //matrix.print(0, 10);
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                }
            }
        }
        return al;
    }

    public static Matrix readMatrix(String fileName) {
        File file = new File(fileName);
        BufferedReader reader = null;
        Matrix matrix = null;
        try {
            System.out.println("读矩阵");
            reader = new BufferedReader(new FileReader(file));
            String tempString = null;
            int line = 1;
            int columns = 0;
            ArrayList<double[]> al = new ArrayList<double[]>();
            // 一次读入一行，直到读入null
            while ((tempString = reader.readLine()) != null) {
                if (line == 1) {
                    columns = getColumnD(tempString);
                }
                double[] tempRow = new double[columns];
                String[] sourceStrArray = tempString.split(",");
                for (int i = 0; i < columns; i++) {
                    tempRow[i] = Double.valueOf(sourceStrArray[i]);
                }
                al.add(tempRow);
                line++;
            }
            double[][] rawData = new double[line - 1][columns];
            for (int i = 0; i < line - 1; i++) {
                for (int j = 0; j < columns; j++) {
                    rawData[i][j] = al.get(i)[j];
                }
            }
            matrix = new Matrix(rawData);
            //matrix.print(0, 10);
            reader.close();
            Log.i("load_matrix", "success");
        } catch (IOException e) {
            Log.i("load_matrix", "failed");
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                }
            }
        }
        return matrix;
    }

    public static Matrix FuncE(Matrix input, double windowsize) {
        int n = input.getRowDimension();
        Matrix res = new Matrix(n, 1);
        int half = (int) Math.floor(windowsize / 2);
        for (int i = half + 1; i <= n - half; i++) {
            //System.out.println(input.getRowDimension());
            Matrix window = input.getMatrix(i - half - 1, i + half - 1, 0, 0);
            double sum = 0;
            double div = 0;
            for (int j = 0; j < window.getRowDimension(); j++) {
                sum += window.getArray()[j][0];
                div += 1;
            }
            double avg = sum / div;
            for (int j = 0; j < window.getRowDimension(); j++) {
                window.getArray()[j][0] -= avg;
            }

            Matrix tempMatrix = window.arrayTimes(window);
            sum = 0;
            div = 0;
            for (int j = 0; j < tempMatrix.getRowDimension(); j++) {
                sum += tempMatrix.getArray()[j][0];
                div += 1;
            }
            avg = sum / div;
            res.getArray()[i - 1][0] = avg;
        }
        return res;
    }

    public static Matrix FuncC(Matrix input, double windowsize) {
        int n = input.getRowDimension();
        Matrix res = new Matrix(n, 1);
        int half = (int) Math.floor(windowsize / 2);
        for (int i = half + 1; i <= n - half; i++) {
            Matrix window = input.getMatrix(i - half - 1, i + half - 1, 0, 0);
            double sum = 0;
            for (int j = 0; j < window.getRowDimension(); j++) {
                sum += window.getArray()[j][0];
            }

            res.getArray()[i - 1][0] = Math.abs(sum);
        }
        return res;
    }

    public static Matrix crossProduct(Matrix A, Matrix B) {
        Matrix C = (Matrix) A.clone();
        for (int i = 0; i < C.getRowDimension(); i++) {
            C.getArray()[i][0] = A.getArray()[i][1] * B.getArray()[i][2] - A.getArray()[i][2] * B.getArray()[i][1];
        }
        for (int i = 0; i < C.getRowDimension(); i++) {
            C.getArray()[i][1] = A.getArray()[i][2] * B.getArray()[i][0] - A.getArray()[i][0] * B.getArray()[i][2];
        }
        for (int i = 0; i < C.getRowDimension(); i++) {
            C.getArray()[i][2] = A.getArray()[i][0] * B.getArray()[i][1] - A.getArray()[i][1] * B.getArray()[i][0];
        }
        return C;
    }

    public static double std(ArrayList<Double> l) {
        double sum = 0;
        double cnt = 0;
        for (double e : l) {
            sum += e;
            cnt++;
        }
        double avg = sum / cnt;
        sum = 0;
        for (double e : l) {
            sum += Math.pow(e - avg, 2);
        }
        return Math.sqrt(sum / (cnt - 1));
    }

    public static double rms(ArrayList<Double> l) {
        double sum = 0;
        double cnt = 0;
        for (double e : l) {
            sum += e * e;
            cnt++;
        }
        return Math.sqrt(sum / cnt);
    }

    public void inputstreamtofile(InputStream ins, File file) {
        OutputStream os = null;
        try {
            os = new FileOutputStream(file);
            int bytesRead = 0;
            byte[] buffer = new byte[8192];
            while ((bytesRead = ins.read(buffer, 0, 8192)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            os.close();
            ins.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Bitmap convertViewToBitmap(View view) {
        view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        view.buildDrawingCache();
        Bitmap bitmap = view.getDrawingCache();

        return bitmap;
    }

    public static byte[] bitmap2Bytes(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }

    public static Bitmap bytes2Bimap(byte[] b) {
        if (b.length != 0) {
            return BitmapFactory.decodeByteArray(b, 0, b.length);
        } else {
            return null;
        }
    }
}
