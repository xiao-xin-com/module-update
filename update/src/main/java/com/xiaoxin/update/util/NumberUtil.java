package com.xiaoxin.update.util;

/**
 * Created by liyuanbiao on 2016/9/24.
 */
public class NumberUtil {

    public static int parseInt(String intStr, int defaultValue) {
        try {
            return Integer.parseInt(intStr);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static float parseFloat(String floatStr, int defaultValue) {
        try {
            return Float.parseFloat(floatStr);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static double parseDouble(String doubleStr, double defaultValue) {
        try {
            return Double.parseDouble(doubleStr);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
