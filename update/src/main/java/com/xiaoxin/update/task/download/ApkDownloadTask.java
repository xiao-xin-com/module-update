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
import com.xiaoxin.update.util.ThreadTask;
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
        UpdateLog.d("ApkDownloadTask download() called");
        String updateUrl = versionInfo.getUpdateUrl();
        if (updateUrl == null) return;

        if (!isNeedDownload()) {
            UpdateLog.d("ApkDownloadTask download: isNeedDownload -> 不需要下载，直接安装apk");
            installApk();
            return;
        }

        BaseDownloadTask baseDownloadTask = new BaseDownloadTask(updateUrl,
                UpdateManager.getTargetFile(), new MyDispatchFileDownloadListener(downloadEvent));
        try {
            baseDownloadTask.startDownload();
        } catch (Exception e) {
            UpdateLog.e("ApkDownloadTask download: ", e);
        }
    }

    private class MyDispatchFileDownloadListener extends DispatchFileDownloadListener {

        MyDispatchFileDownloadListener(DispatchDownloadEvent downloadEvent) {
            super(downloadEvent);
        }

        @Override
        public void completed(final com.liulishuo.filedownloader.BaseDownloadTask task) {
            super.completed(task);
            ThreadTask.execute(new Runnable() {
                @Override
                public void run() {
                    installApk();
                }
            });
        }
    }

    private void installApk() {
        UpdateLog.d("ApkDownloadTask installApk() called");
        if (SignUtils.checkMd5(UpdateManager.getTargetFile(),
                versionInfo.getMd5checksum())) {
            new InstallApkThread(context, versionInfo).run();
            return;
        }
        UpdateLog.d("ApkDownloadTask installApk() md5不匹配");
    }

    //是否需要从无服务器下载新的apk
    private boolean isNeedDownload() {
        UpdateLog.d("ApkDownloadTask isNeedDownload() called");
        //文件不存在
        final String targetFile = UpdateManager.getTargetFile();
        if (TextUtils.isEmpty(targetFile) || !new File(targetFile).exists()) {
            UpdateLog.d("ApkDownloadTask isNeedDownload: 文件不存在");
            return true;
        }

        //本地文件与本应用包名不匹配
        if (!TextUtils.equals(GetAppInfo.getAPKPackageName(context, targetFile),
                GetAppInfo.getAppPackageName(context))) {
            UpdateLog.d("ApkDownloadTask isNeedDownload: 本地文件与本应用包名不匹配");
            if (new File(targetFile).delete()) {
                //此处不做处理
                UpdateLog.d("ApkDownloadTask isNeedDownload: 删除之前的文件成功)");
                return true;
            }
            return true;
        }

        //本地apk的versionCode小于应用的versionCode
        PackageInfo packageInfo = UpdateUtil.getPackageInfo(context, targetFile);
        if (packageInfo == null || packageInfo.versionCode < GetAppInfo.getAppVersionCode(context)) {
            UpdateLog.d("ApkDownloadTask isNeedDownload: 本地apk的versionCode小于应用的versionCode");
            if (new File(targetFile).delete()) {
                //此处不做处理
                UpdateLog.d("ApkDownloadTask isNeedDownload: 删除之前的文件成功)");
                return true;
            }
            return true;
        }

        //本地下载的apk的MD5与服务器上不匹配
        if (!SignUtils.checkMd5(targetFile, versionInfo.getMd5checksum())) {
            UpdateLog.d("ApkDownloadTask isNeedDownload: 本地下载的apk的MD5与服务器上不匹配");
            if (new File(targetFile).delete()) {
                //此处不做处理
                UpdateLog.d("ApkDownloadTask isNeedDownload: 删除之前的文件成功)");
                return true;
            }
            return true;
        }
        return false;
    }
}
