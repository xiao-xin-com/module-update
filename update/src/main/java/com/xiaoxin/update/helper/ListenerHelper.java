package com.xiaoxin.update.helper;

import com.xiaoxin.update.listener.OnDownloadListener;
import com.xiaoxin.update.listener.OnPatchListener;
import com.xiaoxin.update.listener.OnUpdateStatusChangeListener;

/**
 * Created by liyuanbiao on 2016/11/16.
 */

public class ListenerHelper {
    private static final DownloadObserver downloadObserver = new DownloadObserver();
    private static final PatchObserver patchObserver = new PatchObserver();
    private static final UpdateStatusChangeObserver statusChangeObserver = new UpdateStatusChangeObserver();

    public static DownloadObserver getDownloadObserver() {
        return downloadObserver;
    }

    public static UpdateStatusChangeObserver getStatusChangeObserver() {
        return statusChangeObserver;
    }

    public static PatchObserver getPatchObserver() {
        return patchObserver;
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

    public static void registerPatchListener(OnPatchListener observer) {
        patchObserver.registerPatchListener(observer);
    }

    public static void unregisterPatchListener(OnPatchListener observer) {
        patchObserver.unregisterPatchListener(observer);
    }

    public static void unregisterAllPatchListener() {
        patchObserver.unregisterAllPatchListener();
    }

}
