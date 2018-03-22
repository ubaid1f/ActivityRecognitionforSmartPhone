package com.example.euguzns.arforphone;

import android.content.Context;
import android.os.Environment;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import dataobject.SensorObject;

/**
 * Created by euguzns on 2018-01-12.
 */

public class FileWrite {

    // Get context from Main Activity
    Context contexta;

    // Variables for saving data
    public static String mSdCard;
    public static File mFilePath;
    public static File mFile;

    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

    String headerforSingle = "@relation ARfeatures\n"
            + "@attribute accX_average REAL\n" + "@attribute accY_average REAL\n" + "@attribute accZ_average REAL\n"
            + "@attribute accX_max REAL\n" + "@attribute accY_max REAL\n" + "@attribute accZ_max REAL\n"
            + "@attribute accX_min REAL\n" + "@attribute accY_min REAL\n" + "@attribute accZ_min REAL\n"
            + "@attribute accX_deviation REAL\n" + "@attribute accY_deviation REAL\n" + "@attribute accZ_deviation REAL\n"
            + "@attribute accX_crossing REAL\n" + "@attribute accY_crossing REAL\n" + "@attribute accZ_crossing REAL\n"
            + "@attribute accX_quartile REAL\n" + "@attribute accY_quartile REAL\n" + "@attribute accZ_quartile REAL\n"
            + "@attribute accX_meanabs REAL\n" + "@attribute accY_meanabs REAL\n" + "@attribute accZ_meanabs REAL\n"
            + "@attribute accX_variance REAL\n" + "@attribute accY_variance REAL\n" + "@attribute accZ_variance REAL\n"
            + "@attribute action {Stand,Sit,Lie, Walk}\n"
            + "@data\n";

    public FileWrite() {
        mkFolder();     // Make Folder
    }

    public String getTime() {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        String result = "";

        SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");//yyyy_MM_dd HH:mm:ss"yyyy:MM:dd_HH:mm:ss.SSS"
        result = format.format(new Date());

        return result;
    }

    public void fileSaveACC(SensorObject obj, String device) {

        timeStamp = getTime();

        if (device == "Phone")
            mFile = new File(mFilePath, timeStamp + "_Accelerometer_Phone.txt");
        else if (device == "Watch")
            mFile = new File(mFilePath, timeStamp + "_Accelerometer_Watch.txt");

        if (!mFile.isFile()) {
            FileWriter fw;
            try {
                fw = new FileWriter(mFile, true);
                for (int i = 0; i < 50; i++) {
                    fw.write(obj.getAccX()[i] + "\t" + obj.getAccY()[i] + "\t" + obj.getAccZ()[i] + "\n");
                }
                System.out.print("\n");
                fw.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void fileSaveFeatures(double[] feature, String selectedAct, String device) {

        timeStamp = getTime();

        if (device == "Phone")
            mFile = new File(mFilePath, timeStamp + "_Features_Phone.txt");
        else if (device == "Watch")
            mFile = new File(mFilePath, timeStamp + "_Features_Watch.txt");

        if (!mFile.isFile()) {
            FileWriter fw = null;
            try {
                fw = new FileWriter(mFile, true);
                fw.write(headerforSingle);
                for (int i = 0; i < feature.length; i++) {
                    fw.write(feature[i] + ",");
                }
                fw.write(selectedAct);
                fw.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void mkFolder() {
        mSdCard = Environment.getExternalStorageDirectory().getPath();
        mFilePath = new File(mSdCard + "/AR/");
        if (!mFilePath.exists()) {
            mFilePath.mkdirs();
        }
    }
}
