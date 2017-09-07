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
import com.xiaoxin.update.helper.ListenerHelper;
import com.xiaoxin.update.listener.OnUpdateStatusChangeListener;
import com.xiaoxin.update.net.UpdateStringRequest;
import com.xiaoxin.update.task.download.DownloadApkOrPatch;
import com.xiaoxin.update.ui.UpdateDialog;
import com.xiaoxin.update.util.GetAppInfo;
import com.xiaoxin.update.util.UpdateLog;

/**
 * Created by liyuanbiao on 2017/9/6.
 */

public class CheckVersion {

    private final RequestQueue queue;
    private Request<String> updateRequest;
    private VersionInfo versionInfo;
    private Context context;

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
        checkUpdateInfo();
    }

    public void release() {
        queue.stop();
        FileDownloader.getImpl().pauseAll();
    }

    //获取服务器的版本
    private void checkUpdateInfo() {
        UpdateLog.d("checkUpdateInfo() called");
        String updateUrl = UpdateManager.getUpdateUrl();
        if (TextUtils.isEmpty(updateUrl)) {
            UpdateLog.e("验证版本的链接为空");
            return;
        }

        updateRequest = new UpdateStringRequest(updateUrl, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                UpdateLog.d("onResponse: -> " + response);
                updateRequest = null;
                statusChange(OnUpdateStatusChangeListener.STATUS_CHECK_COMPLETE);
                onGetUpdateInfo(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                updateRequest = null;
                UpdateLog.e("网络错误...");
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
        UpdateLog.d("onGetUpdateInfo() called");
        VersionInfoProvider versionInfoProvider = UpdateManager.getVersionInfoProvider();
        if (versionInfoProvider == null) {
            UpdateLog.e("VersionInfoProvider为空,不能解析更新内容");
            return;
        }

        versionInfo = versionInfoProvider.provider(response);
        if (versionInfo == null) {
            UpdateLog.e("VersionInfo为空,不能解析更新内容");
            return;
        }

        String downloadUrl = versionInfo.getUpdateUrl();
        if (TextUtils.isEmpty(downloadUrl) ||
                TextUtils.isEmpty(downloadUrl.trim())) {
            UpdateLog.e("Apk下载链接为空,不能下载安装包");
            return;
        }

        int versionCode = GetAppInfo.getAppVersionCode(context);
        if (versionCode >= versionInfo.getVersionCode()) {
            UpdateLog.d("当前版本 " + versionCode + " ，" +
                    "服务端版本 " + versionInfo.getVersionCode());
            return;
        }

        UpdateManager.setDownloadUrl(downloadUrl);
        if (UpdateManager.isSilence()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    DownloadApkOrPatch downloadApkOrPatch = new DownloadApkOrPatch(context, versionInfo);
                    downloadApkOrPatch.download();
                }
            }).start();

        } else {

            UpdateDialog updateDialog = new UpdateDialog(context, versionInfo);
            updateDialog.showUpdateDialog();
        }
    }

}
