package com.xiaoxin.update.task.download;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.text.TextUtils;

import com.xiaoxin.update.UpdateManager;
import com.xiaoxin.update.bean.VersionInfo;
import com.xiaoxin.update.helper.DispatchDownloadEvent;
import com.xiaoxin.update.listener.DispatchFileDownloadListener;
import com.xiaoxin.update.listener.OnDownloadListener;
import com.xiaoxin.update.task.install.InstallApkThread;
import com.xiaoxin.update.util.GetAppInfo;
import com.xiaoxin.update.util.SignUtils;
import com.xiaoxin.update.util.UpdateLog;
import com.xiaoxin.update.util.UpdateUtil;

import java.io.File;

/**
 * Created by liyuanbiao on 2017/9/6.
 */

class ApkDownloadTask {
    private final DispatchDownloadEvent downloadEvent;
    private Context context;
    private OnDownloadListener downloadListener;
    private VersionInfo versionInfo;

    public ApkDownloadTask(Context context) {
        this(context, null);
    }

    public ApkDownloadTask(Context context, VersionInfo versionInfo) {
        this.context = context;
        this.versionInfo = versionInfo;
        downloadEvent = new DispatchDownloadEvent(context, DispatchDownloadEvent.TYPE_APK);
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

    public VersionInfo getVersionInfo() {
        return versionInfo;
    }

    public void setVersionInfo(VersionInfo versionInfo) {
        this.versionInfo = versionInfo;
    }

    public void setDownloadListener(OnDownloadListener downloadListener) {
        this.downloadListener = downloadListener;
    }

    public void download() {
        String updateUrl = versionInfo.getUpdateUrl();
        if (updateUrl == null) return;

        if (!isNeedDownload()) {
            installApk();
            return;
        }

        BaseDownloadTask baseDownloadTask = new BaseDownloadTask(updateUrl,
                UpdateManager.getTargetFile(), new MyDispatchFileDownloadListener(downloadEvent));
        try {
            baseDownloadTask.startDownload();
        } catch (Exception e) {
            UpdateLog.e("download: ", e);
        }
    }

    private class MyDispatchFileDownloadListener extends DispatchFileDownloadListener {

        MyDispatchFileDownloadListener(DispatchDownloadEvent downloadEvent) {
            super(downloadEvent);
        }

        @Override
        public void completed(final com.liulishuo.filedownloader.BaseDownloadTask task) {
            super.completed(task);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    installApk();
                }
            }).start();
        }
    }

    ;

    private void installApk() {
        if (SignUtils.checkMd5(UpdateManager.getTargetFile(),
                versionInfo.getMd5checksum())) {
            new InstallApkThread(context, versionInfo).run();
        }
    }

    //是否需要从无服务器下载新的apk
    private boolean isNeedDownload() {
        //文件不存在
        final String targetFile = UpdateManager.getTargetFile();
        if (TextUtils.isEmpty(targetFile) || !new File(targetFile).exists()) {
            return true;
        }

        //下载的应用于本应用包名不匹配
        if (!TextUtils.equals(GetAppInfo.getAPKPackageName(context, targetFile),
                GetAppInfo.getAppPackageName(context))) {
            return true;
        }

        //本地apk的versionCode小于应用的versionCode
        PackageInfo packageInfo = UpdateUtil.getPackageInfo(context, targetFile);
        if (packageInfo == null || packageInfo.versionCode < GetAppInfo.getAppVersionCode(context)) {
            if (new File(targetFile).delete()) {
                //此处不做处理
                return true;
            }
            return true;
        }

        //本地下载的apkMD5与服务器上不匹配
        if (!SignUtils.checkMd5(targetFile, versionInfo.getMd5checksum())) {
            if (new File(targetFile).delete()) {
                //此处不做处理
                return true;
            }
            return true;
        }
        return false;
    }
}
