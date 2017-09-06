package com.xiaoxin.update.helper;

import com.xiaoxin.update.listener.OnDownloadListener;
import com.xiaoxin.update.listener.OnUpdateStatusChangeListener;

/**
 * Created by liyuanbiao on 2016/11/16.
 */

public class ListenerHelper {
    private static final DownloadObserver downloadObserver = new DownloadObserver();
    private static final UpdateStatusChangeObserver statusChangeObserver = new UpdateStatusChangeObserver();

    public static DownloadObserver getDownloadObserver() {
        return downloadObserver;
    }

    public static UpdateStatusChangeObserver getStatusChangeObserver() {
        return statusChangeObserver;
    }

    public static void registerDownloadListener(OnDownloadListener observer) {
        downloadObserver.registerDownloadListener(observer);
    }

    public static void unregisterDownloadListener(OnDownloadListener observer) {
        downloadObserver.unregisterDownloadListener(observer);
    }

    public static void unregisterAllDownloadListener() {
        downloadObserver.unregisterAllDownloadListener();
    }

    public static void registerUpdateStatusChangeListener(OnUpdateStatusChangeListener observer) {
        statusChangeObserver.registerUpdateStatusChangeListener(observer);
    }

    public static void unregisterUpdateStatusChangeListener(OnUpdateStatusChangeListener observer) {
        statusChangeObserver.unregisterUpdateStatusChangeListener(observer);
    }

    public static void unregisterAllUpdateStatusChangeListener() {
        statusChangeObserver.unregisterAllUpdateStatusChangeListener();
    }
}
