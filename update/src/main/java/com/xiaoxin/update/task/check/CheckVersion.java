package com.xiaoxin.update.task.check;

import android.content.Context;
import android.text.TextUtils;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.liulishuo.filedownloader.FileDownloader;
import com.xiaoxin.update.UpdateManager;
import com.xiaoxin.update.VersionInfoProvider;
import com.xiaoxin.update.bean.VersionInfo;
import com.xiaoxin.update.helper.CurrentStatus;
import com.xiaoxin.update.helper.ListenerHelper;
import com.xiaoxin.update.listener.OnUpdateStatusChangeListener;
import com.xiaoxin.update.listener.UpdateStatus;
import com.xiaoxin.update.net.UpdateStringRequest;
import com.xiaoxin.update.task.download.DownloadApkOrPatch;
import com.xiaoxin.update.ui.UpdateDialog;
import com.xiaoxin.update.util.FileUtil;
import com.xiaoxin.update.util.GetAppInfo;
import com.xiaoxin.update.util.ThreadTask;
import com.xiaoxin.update.util.UpdateLog;

import java.io.File;

/**
 * Created by liyuanbiao on 2017/9/6.
 */

public class CheckVersion {

    private final RequestQueue queue;
    private Request<String> updateRequest;
    private VersionInfo versionInfo;
    private Context context;
    private boolean first = true;

    public boolean isFirst() {
        return first;
    }

    public CheckVersion(Context context) {
        this.context = context.getApplicationContext();
        queue = Volley.newRequestQueue(context);
    }

    public VersionInfo getVersionInfo() {
        return versionInfo;
    }

    public boolean isChecking() {
        return updateRequest != null;
    }

    public void check() {
        UpdateLog.d("CheckVersion check() called");
        checkUpdateInfo();
    }

    public void release() {
        UpdateLog.d("CheckVersion release() called");
        updateRequest.cancel();
        updateRequest = null;
        first = true;
        queue.stop();
        FileDownloader.getImpl().pauseAll();
    }

    //获取服务器的版本
    private void checkUpdateInfo() {
        UpdateLog.d("CheckVersion checkUpdateInfo() called");
        String updateUrl = UpdateManager.getUpdateUrl();
        if (TextUtils.isEmpty(updateUrl)) {
            UpdateLog.e("CheckVersion 验证版本的链接为空");
            return;
        }

        if (updateRequest != null) {
            UpdateLog.e("CheckVersion 当前正在请求，请不要那么频繁的发起。。。");
            return;
        }

        int status = CurrentStatus.getStatus();
        UpdateLog.d("CheckVersion CurrentStatus -> " + status);
        if (status == UpdateStatus.STATUS_DOWNLOAD_PATCH_START ||
                status == UpdateStatus.STATUS_DOWNLOADING_PATCH ||
                status == UpdateStatus.STATUS_DOWNLOAD_START ||
                status == UpdateStatus.STATUS_DOWNLOADING ||
                status == UpdateStatus.STATUS_INSTALL_START
                ) {
            UpdateLog.d("CheckVersion , 当前有下载或安装任务正在进行");
            return;
        }

        updateRequest = new UpdateStringRequest(updateUrl, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                UpdateLog.d("CheckVersion onResponse: -> " + response);
                first = false;
                updateRequest = null;
                statusChange(OnUpdateStatusChangeListener.STATUS_CHECK_COMPLETE);
                onGetUpdateInfo(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                updateRequest = null;
                UpdateLog.e("CheckVersion 网络错误...");
                UpdateLog.e(error);
                statusChange(OnUpdateStatusChangeListener.STATUS_CHECK_ERROR);
            }
        });

        updateRequest.setRetryPolicy(new MyRetryPolicy());
        queue.add(updateRequest);

        statusChange(OnUpdateStatusChangeListener.STATUS_CHECK_START);
    }

    private static class MyRetryPolicy extends DefaultRetryPolicy {
        @Override
        public int getCurrentTimeout() {
            return 30000;
        }
    }

    private void statusChange(int status) {
        ListenerHelper.getStatusChangeObserver().onUpdateStatusChange(status);
    }

    //解析服务器上的apk版本信息
    private void onGetUpdateInfo(String response) {
        UpdateLog.d("CheckVersion onGetUpdateInfo() called");
        if (!verifyVersion(response)) {
//            deleteApkAndPatch();
            return;
        }

        if (UpdateManager.isSilence()) {
            ThreadTask.execute(new DownloadApkOrPatch(context, versionInfo));
        } else {
            UpdateDialog updateDialog = new UpdateDialog(context, versionInfo);
            updateDialog.showUpdateDialog();
        }
    }

    /**
     * 下载前删除
     */
    private static void deleteApkAndPatch() {
        UpdateLog.d("CheckVersion deleteApkAndPatch() called");

        String targetFile = UpdateManager.getTargetFile();
        if (!TextUtils.isEmpty(targetFile)) {
            FileUtil.delete(new File(targetFile));
        }

        String patchTargetFile = UpdateManager.getPatchTargetFile();
        if (!TextUtils.isEmpty(patchTargetFile)) {
            FileUtil.delete(new File(patchTargetFile));
        }
    }

    /**
     * @param response 服务端返回的结果
     * @return false 不能解析，或者已经是最新版本 true，有新版本
     */
    private boolean verifyVersion(String response) {
        VersionInfoProvider versionInfoProvider = UpdateManager.getVersionInfoProvider();
        if (versionInfoProvider == null) {
            UpdateLog.e("CheckVersion VersionInfoProvider为空,不能解析更新内容");
            return false;
        }

        versionInfo = versionInfoProvider.provider(response);
        if (versionInfo == null) {
            UpdateLog.e("CheckVersion VersionInfo为空,不能解析更新内容");
            return false;
        }

        String downloadUrl = versionInfo.getUpdateUrl();
        if (TextUtils.isEmpty(downloadUrl) ||
                TextUtils.isEmpty(downloadUrl.trim())) {
            UpdateLog.e("CheckVersion Apk下载链接为空,不能下载安装包");
            return false;
        }

        int versionCode = GetAppInfo.getAppVersionCode(context);
        if (versionCode >= versionInfo.getVersionCode()) {
            UpdateLog.d("CheckVersion 当前版本 " + versionCode + " ，" +
                    "服务端版本 " + versionInfo.getVersionCode());
            return false;
        }

        UpdateManager.setDownloadUrl(downloadUrl);
        return true;
    }

}
