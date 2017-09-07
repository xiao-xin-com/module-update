package com.xiaoxin.update.task.download;

import android.content.Context;

import com.xiaoxin.update.UpdateManager;
import com.xiaoxin.update.bean.VersionInfo;
import com.xiaoxin.update.listener.OnDownloadListener;

/**
 * Created by liyuanbiao on 2017/9/7.
 */

public class DownloadApkOrPatch {
    private Context context;
    private VersionInfo versionInfo;
    private OnDownloadListener downloadListener;

    public DownloadApkOrPatch(Context context, VersionInfo versionInfo) {
        this.context = context;
        this.versionInfo = versionInfo;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public OnDownloadListener getDownloadListener() {
        return downloadListener;
    }

    public void setDownloadListener(OnDownloadListener downloadListener) {
        this.downloadListener = downloadListener;
    }

    public VersionInfo getVersionInfo() {
        return versionInfo;
    }

    public void setVersionInfo(VersionInfo versionInfo) {
        this.versionInfo = versionInfo;
    }

    public void download() {
        if (UpdateManager.isIncrement()) {
            if (versionInfo.getPatchUrl() != null) {
                downloadPatch();
                return;
            }
        }
        downloadApk();
    }

    private void downloadApk() {
        ApkDownloadTask apkDownloadTask = new ApkDownloadTask(context, versionInfo);
        apkDownloadTask.setDownloadListener(downloadListener);
        apkDownloadTask.download();
    }

    private void downloadPatch() {
        PatchDownloadTask patchDownloadTask = new PatchDownloadTask(context, versionInfo);
        patchDownloadTask.setDownloadListener(downloadListener);
        patchDownloadTask.download();
    }
}
