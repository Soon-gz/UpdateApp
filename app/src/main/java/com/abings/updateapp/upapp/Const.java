package com.abings.updateapp.upapp;

import android.os.Environment;

import java.io.File;

/**
 * Created by Administrator on 2016/2/1.
 */
public class Const {

    public static int PHOTO_SPAN_COUNT = 3;

    public final static int Fragment_Home = 0;
    public final static int Fragment_Contacts = 1;
    public final static int Fragment_Setting = 2;
    public final static int Fragment_FeedBack = 3;

    public final static int Fragment_Meg = 4;
    public final static int Fragment_Search = 5;

//    public final static int Fragment_Lunch = 5;
//    public final static int Fragment_Operation = 6;
//    public final static int Fragment_Play = 7;
//    public final static int Fragment_Journal = 8;

    /**
     * 家长端下载地址
     */
    public final static String url_baby = "http://www.hellobaobei.com.cn/download/baby.xml";
    /**
     * 老师端下载地址
     */
    public final static String url_teacher = "http://www.hellobaobei.com.cn/download/teacher.xml";
    /**
     * 基础目录
     */
    public final static String basePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "hellobaby" + File.separator;
    /**
     * 下载app目录
     */
    public final static String apkDownPath = basePath + "down" + File.separator;
    /**
     * 视频目录
     */
    public final static String videoPath = basePath + "video" + File.separator;
}
