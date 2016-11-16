package com.xiaoxin.update.listener;

/**
 * Created by liyuanbiao on 2016/11/16.
 */

public class XXListenerHelper {
    private static XXDownloadObserver downloadObserver = new XXDownloadObserver();
    private static XXUpdateStatusChangeObserver statusChangeObserver = new XXUpdateStatusChangeObserver();

    public static XXDownloadObserver getDownloadObserver() {
        return downloadObserver;
    }

    public static XXUpdateStatusChangeObserver getStatusChangeObserver() {
        return statusChangeObserver;
    }

    public static void registerDownloadListener(XXDownloadListener observer) {
        downloadObserver.registerDownloadListener(observer);
    }

    public static void unregisterDownloadListener(XXDownloadListener observer) {
        downloadObserver.unregisterDownloadListener(observer);
    }

    public static void unregisterAllDownloadListener() {
        downloadObserver.unregisterAllDownloadListener();
    }

    public static void registerUpdateStatusChangeListener(XXOnUpdateStatusChangeListener observer) {
        statusChangeObserver.registerUpdateStatusChangeListener(observer);
    }

    public static void unregisterUpdateStatusChangeListener(XXOnUpdateStatusChangeListener observer) {
        statusChangeObserver.unregisterUpdateStatusChangeListener(observer);
    }

    public static void unregisterAllUpdateStatusChangeListener() {
        statusChangeObserver.unregisterAllUpdateStatusChangeListener();
    }
}
