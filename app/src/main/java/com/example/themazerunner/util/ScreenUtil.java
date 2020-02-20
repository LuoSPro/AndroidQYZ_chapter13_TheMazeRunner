package com.example.themazerunner.util;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

public class ScreenUtil {

    /**
     * 获取屏幕相关参数
     *
     * @param context
     * @return DisplayMetrics 屏幕宽高
     */
    public static DisplayMetrics getScreenSize(Context context){
        //DisplayMetrics 描述显示的一般信息的结构，如大小、密度和字体比例。
        DisplayMetrics metrics = new DisplayMetrics();
        //活动与窗口管理器交互的界面
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        //提供有关逻辑显示的大小和密度的信息
        Display display = wm.getDefaultDisplay();
        display.getMetrics(metrics);
        return metrics;
    }


    /**
     * 获取屏幕 density
     *
     * @param context
     * @return 屏幕 density
     */
    public static float getDeviceDensity(Context context){
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(metrics);
        return metrics.density;
    }
}
