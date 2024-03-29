package com.xiaoxin.update.task.download;

import android.content.Context;

import com.xiaoxin.update.UpdateManager;
import com.xiaoxin.update.bean.VersionInfo;
import com.xiaoxin.update.helper.CurrentStatus;
import com.xiaoxin.update.listener.OnDownloadListener;
import com.xiaoxin.update.listener.UpdateStatus;
import com.xiaoxin.update.task.install.InstallApkThread;
import com.xiaoxin.update.util.SignUtils;
import com.xiaoxin.update.util.ThreadTask;
import com.xiaoxin.update.util.UpdateLog;

import java.io.File;

/**
 * Created by liyuanbiao on 2017/9/7.
 */

public class DownloadApkOrPatch implements Runnable {
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
        UpdateLog.d("DownloadApkOrPatch download() called");

        String targetFile = UpdateManager.getTargetFile();

        File file = new File(targetFile);
        if (file.exists()) {
            if (SignUtils.checkMd5(file, versionInfo.getMd5checksum())) {
                UpdateLog.d("DownloadApkOrPatch download() " +
                        "本地文件已存在，而且和服务器的md5匹配，直接安装");
                InstallApkThread installApkThread =
                        new InstallApkThread(context, versionInfo);
                ThreadTask.submit(installApkThread);
                return;
            } else if (file.delete()) {
                UpdateLog.d("DownloadApkOrPatch download() " +
                        "本地文件已存在,但是md5不对，把它删除");
            }
        }

        int status = CurrentStatus.getStatus();
        UpdateLog.d("DownloadApkOrPatch download() CurrentStatus -> " + status);
        if (status == UpdateStatus.STATUS_DOWNLOAD_PATCH_START ||
                status == UpdateStatus.STATUS_DOWNLOADING_PATCH ||
                status == UpdateStatus.STATUS_DOWNLOAD_START ||
                status == UpdateStatus.STATUS_DOWNLOADING ||
                status == UpdateStatus.STATUS_INSTALL_START) {
            UpdateLog.d("DownloadApkOrPatch download() 当前有下载或安装任务正在进行");
            return;
        }
        if (UpdateManager.isIncrement()) {
            if (versionInfo.getPatchUrl() != null) {
                downloadPatch();
                return;
            }
            UpdateLog.d("DownloadApkOrPatch download() 虽然设置了增量升级，但是差分包却不存在");
        }
        downloadApk();
    }

    private void downloadApk() {
        UpdateLog.d("DownloadApkOrPatch downloadApk() called");
        ApkDownloadTask apkDownloadTask = new ApkDownloadTask(context, versionInfo);
        apkDownloadTask.setDownloadListener(downloadListener);
        apkDownloadTask.download();
    }

    private void downloadPatch() {
        UpdateLog.d("DownloadApkOrPatch downloadPatch() called");
        PatchDownloadTask patchDownloadTask = new PatchDownloadTask(context, versionInfo);
        patchDownloadTask.setDownloadListener(downloadListener);
        patchDownloadTask.download();
    }

    @Override
    public void run() {
        download();
    }
}
