package com.xiaoxin.update.helper;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.liulishuo.filedownloader.BaseDownloadTask;
import com.xiaoxin.update.UpdateManager;
import com.xiaoxin.update.bean.AppInfo;
import com.xiaoxin.update.listener.OnDownloadListener;
import com.xiaoxin.update.listener.UpdateStatus;
import com.xiaoxin.update.util.ApkUtils;
import com.xiaoxin.update.util.NotifyUtil;
import com.xiaoxin.update.util.UpdateLog;

/**
 * Created by liyuanbiao on 2017/9/6.
 */

public class DispatchDownloadEvent {

    private DownloadObserver downloadObserver = ListenerHelper.getDownloadObserver();
    private Context context;
    private AppInfo appInfo;
    private int applicationIcon;
    private String applicationLabel;
    private int type = TYPE_APK;
    public static final int TYPE_APK = 1;
    public static final int TYPE_PATCH = 2;

    public DispatchDownloadEvent(Context context) {
        this.context = context;
        this.appInfo = new AppInfo(context);
        this.applicationIcon = appInfo.getApplicationIcon();
        this.applicationLabel = appInfo.getApplicationLabel();
    }

    public DispatchDownloadEvent(Context context, int type) {
        this(context);
        this.type = type;
    }

    private boolean isApk() {
        return type == TYPE_APK;
    }

    public void started(BaseDownloadTask task) {
        UpdateLog.d("started() called with: task = [" + task + "]");
        if (UpdateManager.isShowUI() && !UpdateManager.isSilence()) {
            NotifyUtil.create(context, task.getId()).notify_progress(null,
                    applicationIcon, "开始升级", applicationLabel, "开始下载", false, false, false, 0, 100, false);
        }
        final OnDownloadListener downloadListener = UpdateManager.getDownloadListener();
        if (downloadListener != null) {
            downloadListener.onStart(task.getTargetFilePath());
        }
        downloadObserver.onStart(task.getTargetFilePath());
        statusChange(isApk() ? UpdateStatus.STATUS_DOWNLOAD_START : UpdateStatus.STATUS_DOWNLOAD_PATCH_START);
    }

    public void completed(BaseDownloadTask task) {
        UpdateLog.d("completed() called with: task = [" + task + "]");
        if (UpdateManager.isShowUI() && !UpdateManager.isSilence()) {
            Intent intent = ApkUtils.getInstallIntent(task.getTargetFilePath());
            PendingIntent pendingIntent = PendingIntent.getActivity(context, task.getId(), intent, 0);
            NotifyUtil.create(context, task.getId()).notify_progress(type == TYPE_APK ? pendingIntent : null,
                    applicationIcon, "开始升级", applicationLabel, "下载完成", false, false, false, 100, 100, false);
        }

        final OnDownloadListener downloadListener = UpdateManager.getDownloadListener();
        if (downloadListener != null) {
            downloadListener.onComplete(task.getTargetFilePath());
        }

        downloadObserver.onComplete(task.getTargetFilePath());
        statusChange(isApk() ? UpdateStatus.STATUS_DOWNLOAD_COMPLETE :
                UpdateStatus.STATUS_DOWNLOAD_PATCH_COMPLETE);
    }

    public void warn(BaseDownloadTask task) {
        UpdateLog.d("warn() called with: task = [" + task + "]");
        if (UpdateManager.isShowUI() && !UpdateManager.isSilence()) {
            NotifyUtil.create(context, task.getId()).notify_progress(null,
                    applicationIcon, "开始升级", applicationLabel, "下载错误", false, false, false, 0, 0, false);
        }
    }

    public void error(BaseDownloadTask task, Throwable e) {
        UpdateLog.d("error() called with: task = [" + task + "], e = [" + e + "]");
        if (UpdateManager.isShowUI() && !UpdateManager.isSilence()) {
            NotifyUtil.create(context, task.getId()).notify_progress(null,
                    applicationIcon, "开始升级", applicationLabel, "下载错误", false, false, false, 0, 0, false);
        }

        final OnDownloadListener downloadListener = UpdateManager.getDownloadListener();
        if (downloadListener != null) {
            downloadListener.onError(task.getTargetFilePath(), e);
        }
        downloadObserver.onError(task.getTargetFilePath(), e);
        statusChange(isApk() ? UpdateStatus.STATUS_DOWNLOAD_ERROR : UpdateStatus.STATUS_DOWNLOAD_PATCH_ERROR);
    }

    public void progress(BaseDownloadTask task, int soFarBytes, int totalBytes) {
        UpdateLog.d("progress() called with: task = [" + task + "], soFarBytes = [" + soFarBytes + "], totalBytes = [" + totalBytes + "]");
        if (UpdateManager.isShowUI() && !UpdateManager.isSilence()) {
            NotifyUtil.create(context, task.getId()).notify_progress(null,
                    applicationIcon, "开始升级", applicationLabel, "正在下载...", false, false, false, totalBytes, soFarBytes, false);
        }
        final OnDownloadListener downloadListener = UpdateManager.getDownloadListener();
        if (downloadListener != null) {
            downloadListener.onProgress(soFarBytes, totalBytes);
        }
        downloadObserver.onProgress(soFarBytes, totalBytes);
        statusChange(isApk() ? UpdateStatus.STATUS_DOWNLOADING : UpdateStatus.STATUS_DOWNLOADING_PATCH);

    }

    private static void statusChange(int state) {
        CurrentStatus.dispatchStatusChange(state);
    }

}
