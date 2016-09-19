package com.xiaoxin.update.service;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.os.Binder;
import android.os.IBinder;
import android.text.TextUtils;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadSampleListener;
import com.liulishuo.filedownloader.FileDownloader;
import com.xiaoxin.update.R;
import com.xiaoxin.update.XXUpdateManager;
import com.xiaoxin.update.XXVersionInfoProvider;
import com.xiaoxin.update.bean.XXVersionInfo;
import com.xiaoxin.update.listener.XXDownloadListener;
import com.xiaoxin.update.net.XXStringRequest;
import com.xiaoxin.update.util.XXCmdUtil;
import com.xiaoxin.update.util.XXGetAppInfo;
import com.xiaoxin.update.util.XXLogUtil;
import com.xiaoxin.update.util.XXUtil;

import java.io.File;
import java.lang.ref.WeakReference;

public class XXUpdateService extends Service {
    private static final String TAG = "XXUpdateService";
    //请求队列
    private RequestQueue queue;
    //服务器上APP的版本信息
    private XXVersionInfo versionInfo;
    //下载apk的id，用于取消
    private int downloadId = -1;

    //有activity绑定时，如果是提示升级，而且版本信息下载完了，则去显示Dialog
    @Override
    public IBinder onBind(Intent intent) {
        XXLogUtil.d("onBind() called with: intent = [" + intent + "]");
        if (versionInfo != null) {
            if (!XXUpdateManager.isSilence() && XXUpdateManager.getActivityContext() != null &&
                    XXUpdateManager.getActivityContext().get() != null) {
                showDialog(XXUpdateManager.getActivityContext().get(), versionInfo.getUpdateInfo());
            }
        }
        return new UpdateBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        XXLogUtil.d("onUnbind() called with: intent = [" + intent + "]");
        return super.onUnbind(intent);
    }

    public static class UpdateBinder extends Binder {

    }

    @Override
    public void onCreate() {
        XXLogUtil.d("onCreate() called");
        super.onCreate();
        initVolley();
        getUpdateInfo();
    }

    //初始化Volley
    private void initVolley() {
        XXLogUtil.d("initVolley() called");
        queue = Volley.newRequestQueue(this);
    }

    //获取服务器的版本
    private void getUpdateInfo() {
        XXLogUtil.d("getUpdateInfo() called");
        String updateUrl = XXUpdateManager.getUpdateUrl();
        if (TextUtils.isEmpty(updateUrl)) return;
        final StringRequest stringRequest = new XXStringRequest(updateUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                onGetUpdateInfo(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                XXLogUtil.d(error);
            }
        });
        stringRequest.setRetryPolicy(new DefaultRetryPolicy() {
            @Override
            public int getCurrentTimeout() {
                return 30000;
            }
        });
        queue.add(stringRequest);
    }

    //解析服务器上的apk版本信息
    private void onGetUpdateInfo(String response) {
        XXLogUtil.d("onGetUpdateInfo() called with: response = [" + response + "]");
        XXVersionInfoProvider versionInfoProvider = XXUpdateManager.getVersionInfoProvider();
        if (versionInfoProvider != null) {
            versionInfo = versionInfoProvider.provider(response);
            if (versionInfo != null) {
                if (!TextUtils.isEmpty(versionInfo.getDownloadUrl())) {
                    XXUpdateManager.setApkDownloadUrl(versionInfo.getDownloadUrl());
                }
                if (!TextUtils.isEmpty(XXUpdateManager.getApkDownloadUrl())) {
                    if (XXUpdateManager.isSilence()) {
                        downloadOrInstall();
                    } else {
                        WeakReference<Context> activityContext = XXUpdateManager.getActivityContext();
                        if (activityContext != null && activityContext.get() != null) {
                            showDialog(activityContext.get(), versionInfo.getUpdateInfo());
                        }
                    }
                }
            }
        }
    }

    private void downloadOrInstall() {
        XXLogUtil.d("downloadOrInstall() called");
        if (isNeedDownload()) {
            downloadApk();
        } else {
            startInstallApp();
        }
    }

    private void downloadApk() {
        final String apkDownloadUrl = XXUpdateManager.getApkDownloadUrl();
        final String targetFile = XXUpdateManager.getTargetFile();
        if (TextUtils.isEmpty(apkDownloadUrl) || TextUtils.isEmpty(targetFile)) {
            return;
        }
        if (new File(targetFile).exists()) {

        }
        if (!TextUtils.isEmpty(apkDownloadUrl) && !TextUtils.isEmpty(targetFile)) {
            downloadId = FileDownloader.getImpl().create(apkDownloadUrl).
                    setListener(fileDownloadSampleListener).setPath(targetFile).start();
        }
    }

    //apk下载安装监听
    private FileDownloadSampleListener fileDownloadSampleListener = new FileDownloadSampleListener() {
        @Override
        protected void started(BaseDownloadTask task) {
            XXLogUtil.d("started() called with: task = [" + task + "]");
            dispatchDownloadStart();
        }

        @Override
        protected void progress(BaseDownloadTask task, int soFarBytes, int totalBytes) {
            XXLogUtil.d("progress() called with: task = [" + task + "], soFarBytes = [" + soFarBytes + "], totalBytes = [" + totalBytes + "]");
            dispatchDownloadProgress(soFarBytes, totalBytes);
        }

        @Override
        protected void completed(BaseDownloadTask task) {
            XXLogUtil.d("completed() called with: task = [" + task + "]");
            dispatchDownloadComplete();
        }
    };

    //分发下载完成事件
    private void dispatchDownloadComplete() {
        final XXDownloadListener downloadListener = XXUpdateManager.getDownloadListener();
        if (downloadListener != null) {
            downloadListener.onComplete();
        }
        startInstallApp();
    }

    private void startInstallApp() {
        new Thread() {
            @Override
            public void run() {
                if (isNeedDownload()) return;
                //开始安装，如果选择静默安装，并且手机已经root，选择pm安装，其他情况全部打开android安装界面
                String targetFile = XXUpdateManager.getTargetFile();
                if (XXUpdateManager.isSilence()) {
                    if (XXCmdUtil.isRoot()) {
                        try {
                            XXUtil.slientInstall(targetFile);
                        } catch (Exception e) {
                            XXUtil.startInstall(XXUpdateService.this, new File(targetFile));
                        }
                    } else {
                        XXUtil.startInstall(XXUpdateService.this, new File(targetFile));
                    }
                } else {
                    XXUtil.startInstall(XXUpdateService.this, new File(targetFile));
                }
            }
        }.start();
    }

    //
    private boolean isNeedDownload() {
        //文件不存在不升级
        final Context context = XXUpdateManager.getContext();
        final String targetFile = XXUpdateManager.getTargetFile();
        if (TextUtils.isEmpty(targetFile) || !new File(targetFile).exists()) {
            return true;
        }
        //下载的应用于本应用包名不对不升级
        String apkPackageName = XXGetAppInfo.getAPKPackageName(context, targetFile);
        if (!TextUtils.equals(apkPackageName, XXGetAppInfo.getAppPackageName(context))) {
            return true;
        }
        //versionCode小于等于本地应用也不升级
        PackageInfo packageInfo = XXUtil.getPackageInfo(context, targetFile);
        if (packageInfo == null || packageInfo.versionCode <= XXGetAppInfo.getAppVersionCode(context)) {
            new File(targetFile).delete();
            return true;
        }
        return false;
    }

    //分发下载进度事件
    private void dispatchDownloadProgress(int soFarBytes, int totalBytes) {
        final XXDownloadListener downloadListener = XXUpdateManager.getDownloadListener();
        if (downloadListener != null) {
            downloadListener.onProgress(soFarBytes, totalBytes);
        }
    }

    //分发下载开始事件
    private void dispatchDownloadStart() {
        final XXDownloadListener downloadListener = XXUpdateManager.getDownloadListener();
        if (downloadListener != null) {
            downloadListener.onStart();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    //提示升级显示对话框
    private void showDialog(Context context, String updateInfo) {
        XXLogUtil.d("showDialog() called with: context = [" + context + "], updateInfo = [" + updateInfo + "]");
        AlertDialog dialog = new AlertDialog.Builder(context).setTitle(R.string.updatehint).
                setMessage(updateInfo).setPositiveButton(R.string.positive, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                downloadOrInstall();
                dialog.dismiss();
            }
        }).setNegativeButton(R.string.negative, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).create();
        dialog.show();
    }

    @Override
    public void onDestroy() {
        XXLogUtil.d("onDestroy() called");
        super.onDestroy();
        if (queue != null) {
            queue.stop();
        }

        if (downloadId != -1) {
            FileDownloader.getImpl().pause(downloadId);
        }
    }
}
