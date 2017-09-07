package com.xiaoxin.update.task.download;

import android.content.Context;
import android.text.TextUtils;

import com.xiaoxin.update.UpdateManager;
import com.xiaoxin.update.bean.PatchInfo;
import com.xiaoxin.update.bean.PatchUrl;
import com.xiaoxin.update.bean.VersionInfo;
import com.xiaoxin.update.helper.DispatchDownloadEvent;
import com.xiaoxin.update.listener.DispatchFileDownloadListener;
import com.xiaoxin.update.listener.OnDownloadListener;
import com.xiaoxin.update.listener.simple.SimplePatchListener;
import com.xiaoxin.update.task.install.InstallApkThread;
import com.xiaoxin.update.task.patch.PatchTask;
import com.xiaoxin.update.util.ApkUtils;
import com.xiaoxin.update.util.SignUtils;
import com.xiaoxin.update.util.UpdateLog;

import java.io.File;

/**
 * Created by liyuanbiao on 2017/9/7.
 */

class PatchDownloadTask {
    private DispatchDownloadEvent downloadEvent;
    private Context context;
    private VersionInfo versionInfo;
    private PatchUrl patchUrl;
    private OnDownloadListener downloadListener;

    public PatchDownloadTask(Context context) {
        this(context, null);
    }

    public PatchDownloadTask(Context context, VersionInfo versionInfo) {
        this.context = context;
        this.versionInfo = versionInfo;
        downloadEvent = new DispatchDownloadEvent(context, DispatchDownloadEvent.TYPE_PATCH);
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
        UpdateLog.d("PatchDownloadTask download() called");
        patchUrl = versionInfo.getPatchUrl();

        if (patchUrl == null || TextUtils.isEmpty(patchUrl.getUrl())) {
            UpdateLog.d("PatchDownloadTask download: 差分包不存在");
            downloadApk();
            return;
        }

        if (!isNeedDownload()) {
            UpdateLog.d("PatchDownloadTask download: isNeedDownload 不需要下载，直接组合差分包");
            patchApk();
            return;
        }

        BaseDownloadTask baseDownloadTask = new BaseDownloadTask(patchUrl.getUrl(),
                UpdateManager.getPatchTargetFile(), new MyDispatchFileDownloadListener(downloadEvent));
        try {
            baseDownloadTask.startDownload();
        } catch (Exception e) {
            UpdateLog.e("PatchDownloadTask download: ", e);
        }
    }

    private boolean isNeedDownload() {
        UpdateLog.d("PatchDownloadTask isNeedDownload() called");
        String patchTargetFile = UpdateManager.getPatchTargetFile();
        if (!new File(patchTargetFile).exists()) {
            UpdateLog.d("PatchDownloadTask isNeedDownload: patch文件不存在，需要下载");
            return true;
        }

        if (!SignUtils.checkMd5(patchTargetFile, patchUrl.getMd5())) {
            UpdateLog.d("PatchDownloadTask isNeedDownload: md5不匹配，需要下载");
            if (new File(patchTargetFile).delete()) {
                UpdateLog.d("PatchDownloadTask isNeedDownload: 删除之前的patch文件成功");
            }
            return true;
        }

        return false;
    }

    private class MyDispatchFileDownloadListener extends DispatchFileDownloadListener {
        public MyDispatchFileDownloadListener(DispatchDownloadEvent downloadEvent) {
            super(downloadEvent);
        }

        @Override
        public void completed(final com.liulishuo.filedownloader.BaseDownloadTask task) {
            super.completed(task);
            UpdateLog.d("PatchDownloadTask patchApk() 下载完成");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    patchApk();
                }
            }).start();
        }
    }

    private void patchApk() {
        UpdateLog.d("PatchDownloadTask patchApk() called");
        final String patchPath = UpdateManager.getPatchTargetFile();
        if (!SignUtils.checkMd5(patchPath, patchUrl.getMd5())) {
            UpdateLog.d("PatchDownloadTask patchApk: 本地patch与服务器patch的md5不匹配，下载整包");
            downloadApk();
            return;
        }
        String oldApkPath = ApkUtils.getSourceApkPath(context, context.getPackageName());
        String newApkPath = UpdateManager.getTargetFile();
        PatchTask patchTask = new PatchTask(oldApkPath, newApkPath, patchPath);
        patchTask.setOnPatchListener(new SimplePatchListener() {
            @Override
            public void onComplete(PatchInfo patchInfo) {
                super.onComplete(patchInfo);
                if (SignUtils.checkMd5(patchInfo.getNewFile(), versionInfo.getMd5checksum())) {
                    new InstallApkThread(context, versionInfo).run();
                } else {
                    UpdateLog.d("PatchDownloadTask patchApk onComplete : md5不匹配");
                    if (new File(patchInfo.getNewFile()).delete()) {
                        UpdateLog.d("PatchDownloadTask isNeedDownload: 删除组合失败的apk，" +
                                "如何不删除可能FileDownloader断点续传会错误");
                    }
                    downloadApk();
                }
            }

            @Override
            public void onError(Exception e) {
                super.onError(e);
                UpdateLog.e("PatchDownloadTask patchApk onError : ", e);
                downloadApk();
            }
        });
        patchTask.patch();
    }

    private void downloadApk() {
        UpdateLog.d("PatchDownloadTask downloadApk() called");
        ApkDownloadTask apkDownloadTask = new ApkDownloadTask(context, versionInfo);
        apkDownloadTask.setDownloadListener(downloadListener);
        apkDownloadTask.download();
    }

}
