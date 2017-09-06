package com.xiaoxin.update.util;

import android.util.Log;

/**
 * Created by liyuanbiao on 2016/9/17.
 */

public class UpdateLogUtil {

    private static final String TAG = "LogUtil";
    private static boolean logFlag;

    public static void setLogFlag(boolean logFlag) {
        UpdateLogUtil.logFlag = logFlag;
    }

    public static void w(Object msg) { // 警告信息
        log(TAG, String.valueOf(msg), 'w');
    }

    public static void w(String key, Object msg) {
        log(key, String.valueOf(msg), 'w');
    }

    public static void e(Object msg) { // 错误信息
        log(TAG, String.valueOf(msg), 'e');
    }

    public static void e(String key, Object msg) {
        log(key, String.valueOf(msg), 'e');
    }

    public static void d(Object msg) {// 调试信息
        log(TAG, String.valueOf(msg), 'd');
    }

    public static void d(String key, Object msg) {
        log(key, String.valueOf(msg), 'd');
    }

    public static void i(Object msg) {//
        log(TAG, String.valueOf(msg), 'i');
    }

    public static void i(String key, Object msg) {
        log(key, String.valueOf(msg), 'i');
    }

    public static void v(Object msg) {
        log(TAG, String.valueOf(msg), 'v');
    }

    public static void v(String key, Object msg) {
        log(key, String.valueOf(msg), 'v');
    }


    /**
     * 根据tag, msg和等级，输出日志
     *
     * @param tag
     * @param msg
     * @param level
     * @return void
     * @since v 1.0
     */
    private static void log(String tag, String msg, char level) {
        if (logFlag) {
            switch (level) {
                case 'e':
                    Log.e(tag, msg);
                    break;
                case 'w':
                    Log.w(tag, msg);
                    break;
                case 'd':
                    Log.d(tag, msg);
                    break;
                case 'i':
                    Log.i(tag, msg);
                    break;
                case 'v':
                    Log.v(tag, msg);
                    break;
            }
        }
    }
}
