package com.xiaoxin.update.service;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageInstallObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadSampleListener;
import com.liulishuo.filedownloader.FileDownloader;
import com.xiaoxin.update.XXUpdateManager;
import com.xiaoxin.update.XXVersionInfoProvider;
import com.xiaoxin.update.bean.XXVersionInfo;
import com.xiaoxin.update.listener.XXDownloadListener;
import com.xiaoxin.update.listener.XXDownloadObserver;
import com.xiaoxin.update.listener.XXListenerHelper;
import com.xiaoxin.update.listener.XXOnUpdateStatusChangeListener;
import com.xiaoxin.update.listener.XXUpdateStatusChangeObserver;
import com.xiaoxin.update.net.XXStringRequest;
import com.xiaoxin.update.util.XXCmdUtil;
import com.xiaoxin.update.util.XXGetAppInfo;
import com.xiaoxin.update.util.XXLogUtil;
import com.xiaoxin.update.util.XXNotifyUtil;
import com.xiaoxin.update.util.XXUITask;
import com.xiaoxin.update.util.XXUtil;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import static com.xiaoxin.update.util.XXGetAppInfo.getAPKPackageName;

public class XXUpdateService extends Service {
    private static final String TAG = "XXUpdateService";

    public static final String ACTION_CHECK_UPDATE = "com.xiaoxin.update.action.service";

    public static final int AUTO_RETRY_TIMES = 10000;
    //请求队列
    private RequestQueue queue;

    //服务器上APP的版本信息
    private XXVersionInfo versionInfo;

    //下载apk的id，用于取消
    private int downloadId = -1;

    //接收检测更新的receiver();
    private UpdateReceiver updateReceiver;

    //用于展示更新内容的Dialog
    private AlertDialog dialog;

    //标记当前下载的状态
    private Map<Integer, Boolean> stateMap = new HashMap<>();

    //从服务器获取最新版本的请求
    private Request updateRequest;
    private String applicationLable;
    private int applicationIcon;

    private XXDownloadObserver downloadObserver;
    private XXUpdateStatusChangeObserver statusChangeObserver;

    {
        downloadObserver = XXListenerHelper.getDownloadObserver();
        statusChangeObserver = XXListenerHelper.getStatusChangeObserver();
    }

    //有activity绑定时，如果是提示升级，而且版本信息下载完了，则去显示Dialog
    @Override
    public IBinder onBind(Intent intent) {
        XXLogUtil.d("onBind() called with: intent = [" + intent + "]");
        showUpdateDialog();
        return new UpdateBinder();
    }

    private void showUpdateDialog() {
        if (versionInfo != null) {
            if (!XXUpdateManager.isSilence() && XXUpdateManager.getActivityContext() != null &&
                    XXUpdateManager.getActivityContext().get() != null) {
                if (XXGetAppInfo.getAppVersionCode(this) < versionInfo.getVersionCode()) {
                    showDialog(XXUpdateManager.getActivityContext().get(), versionInfo.getUpdateInfo());
                }
            }
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        XXLogUtil.d("onUnbind() called with: intent = [" + intent + "]");
        return super.onUnbind(intent);
    }

    public class UpdateBinder extends Binder {

        public int getStatus() {
            return XXUpdateService.this.getStatus();
        }
    }

    @Override
    public void onCreate() {
        XXLogUtil.d("onCreate() called");
        super.onCreate();
        initApplicationInfo();
        //初始化Volley
        initVolley();
        registerUpdateReceiver();
        //在服务开启的时候就去检测有没有版本要更新
        checkUpdateInfo();
        timerCheck();
    }

    private void initApplicationInfo() {
        try {
            PackageManager packageManager = getPackageManager();
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(getPackageName(), 0);
            applicationIcon = XXUpdateManager.getIcon();
            applicationLable = (String) packageManager.getApplicationLabel(applicationInfo);
        } catch (PackageManager.NameNotFoundException e) {
            applicationIcon = android.R.drawable.sym_def_app_icon;
            applicationLable = getPackageName();
        }
    }

    private Timer timer;
    public static final int INTERNAL_CHECK_TIME = 1000 * 60 * 60 * 3;
    public static final int DELAY_CHECK_TIME = (int) (1000 * 60 * 60 * 0.5);

    private void timerCheck() {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                XXUITask.post(new Runnable() {
                    @Override
                    public void run() {
                        check();
                    }
                });
            }
        }, DELAY_CHECK_TIME, INTERNAL_CHECK_TIME);
    }

    private void registerUpdateReceiver() {
        if (updateReceiver == null) {
            updateReceiver = new UpdateReceiver();
        }
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_CHECK_UPDATE);
        LocalBroadcastManager.getInstance(this).registerReceiver(updateReceiver, intentFilter);
    }

    private void unRegisterUpdateReceiver() {
        if (updateReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(updateReceiver);
            updateReceiver = null;
        }
    }

    //初始化Volley
    private void initVolley() {
        XXLogUtil.d("initVolley() called");
        queue = Volley.newRequestQueue(this);
    }

    //获取服务器的版本
    private void checkUpdateInfo() {
        XXLogUtil.d("checkUpdateInfo() called");
        String updateUrl = XXUpdateManager.getUpdateUrl();
        if (TextUtils.isEmpty(updateUrl)) {
            XXLogUtil.e("更新链接为空");
            return;
        }

        updateRequest = queue.add(new XXStringRequest(updateUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                updateRequest = null;
                onGetUpdateInfo(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                XXLogUtil.e("网络错误...");
                XXLogUtil.e(error);
                updateRequest = null;
            }
        }).setRetryPolicy(new DefaultRetryPolicy() {
            @Override
            public int getCurrentTimeout() {
                return 30000;
            }
        }));
    }

    //解析服务器上的apk版本信息
    private void onGetUpdateInfo(String response) {
        XXLogUtil.d("onGetUpdateInfo() called with: response = [" + response + "]");
        XXVersionInfoProvider versionInfoProvider = XXUpdateManager.getVersionInfoProvider();
        if (versionInfoProvider != null) {
            versionInfo = versionInfoProvider.provider(response);
            if (versionInfo != null) {
                String downloadUrl = versionInfo.getDownloadUrl();
                if (!TextUtils.isEmpty(downloadUrl)) {
                    if (downloadUrl.startsWith("http://") || downloadUrl.startsWith("https://")) {
                        XXUpdateManager.setDownloadUrl(downloadUrl);
                    } else {
                        XXLogUtil.e("这不是一个下载链接 DownloadUrl --> " + downloadUrl);
                    }
                }
                if (!TextUtils.isEmpty(XXUpdateManager.getDownloadUrl())) {
                    if (XXUpdateManager.isSilence()) {
                        downloadOrInstall();
                    } else {
                        showUpdateDialog();
                    }
                } else {
                    XXLogUtil.e("下载链接为空");
                }
            }
        } else {
            XXLogUtil.e("VersionInfoProvider为空,不能解析更新内容");
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
        final String apkDownloadUrl = XXUpdateManager.getDownloadUrl();
        final String targetFile = XXUpdateManager.getTargetFile();
        if (TextUtils.isEmpty(apkDownloadUrl) || TextUtils.isEmpty(targetFile)) {
            return;
        }
        File file = new File(targetFile);
        if (file.exists()) {
            file.delete();
        }
        if (!TextUtils.isEmpty(apkDownloadUrl) && !TextUtils.isEmpty(targetFile)) {
            //如果这是第一次下载，或者这次下载已经完成就去下载，这样做是为了下载不重复
            if (downloadId == -1 || stateMap.get(downloadId) == null || stateMap.get(downloadId) == false) {
                downloadId = FileDownloader.getImpl().create(apkDownloadUrl).
                        setListener(fileDownloadSampleListener).setPath(targetFile).setAutoRetryTimes(AUTO_RETRY_TIMES).start();
            }
        }
    }

    //apk下载安装监听
    private FileDownloadSampleListener fileDownloadSampleListener = new FileDownloadSampleListener() {
        @Override
        protected void started(BaseDownloadTask task) {
            XXLogUtil.d("started() called with: task = [" + task + "]");
            stateMap.put(task.getId(), true);
            dispatchDownloadStart();
            if (XXUpdateManager.isShowUI() && !XXUpdateManager.isSilence()) {
                XXNotifyUtil.create(XXUpdateService.this, task.getId()).notify_progress(null,
                        applicationIcon, "开始升级", applicationLable, "开始下载", false, false, false, 0, 100, false);
            }
        }

        @Override
        protected void progress(BaseDownloadTask task, int soFarBytes, int totalBytes) {
            XXLogUtil.d("progress() called with: task = [" + task + "], soFarBytes = [" + soFarBytes + "], totalBytes = [" + totalBytes + "]");
            dispatchDownloadProgress(soFarBytes, totalBytes);
            if (XXUpdateManager.isShowUI() && !XXUpdateManager.isSilence()) {
                XXNotifyUtil.create(XXUpdateService.this, task.getId()).notify_progress(null,
                        applicationIcon, "开始升级", applicationLable, "正在下载...", false, false, false, totalBytes, soFarBytes, false);
            }
        }

        @Override
        protected void completed(BaseDownloadTask task) {
            XXLogUtil.d("completed() called with: task = [" + task + "]");
            stateMap.put(task.getId(), false);
            dispatchDownloadComplete();
            if (XXUpdateManager.isShowUI() && !XXUpdateManager.isSilence()) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setDataAndType(Uri.parse("file://" + task.getPath()), "application/vnd.android.package-archive");
                PendingIntent pendingIntent = PendingIntent.getActivity(XXUpdateService.this, task.getId(), intent, 0);
                XXNotifyUtil.create(XXUpdateService.this, task.getId()).notify_progress(pendingIntent,
                        applicationIcon, "开始升级", applicationLable, "下载完成", false, false, false, 100, 100, false);
            }
        }
    };


    private void startInstallApp() {
        new Thread() {
            @Override
            public void run() {
                if (isNeedDownload()) return;
                String targetFile = XXUpdateManager.getTargetFile();
                if ((!XXUpdateManager.isSilence()) || (XXUpdateManager.isFriendly() &&
                        XXUpdateManager.getActivityContext() != null &&
                        XXUpdateManager.getActivityContext().get() != null)) {
                    startInstall(targetFile);
                } else if (XXUpdateManager.isSilence()) {
                    if (XXUpdateManager.isUsePm()) {
                        installPackage(new File(targetFile));  //pm安装
                    } else if (XXCmdUtil.isRoot()) {
                        try {
                            slientInstall(targetFile);//非pm安装下，有root权限adb安装
                        } catch (Exception e) {
                            startInstall(targetFile); //异常则普通安装
                        }
                    } else {
                        startInstall(targetFile);  //无root权限普通安装
                    }
                }
            }

            private void slientInstall(String targetFile) throws IOException, InterruptedException {
                XXUtil.slientInstall(targetFile);
            }

            private void startInstall(String targetFile) {
                XXUtil.startInstall(XXUpdateService.this, new File(targetFile));
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
        String apkPackageName = getAPKPackageName(context, targetFile);
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
        downloadObserver.onProgress(soFarBytes, totalBytes);
        statusChange(XXOnUpdateStatusChangeListener.STATUS_DOWNLOAD_PROGRESS);
    }

    //分发下载开始事件
    private void dispatchDownloadStart() {
        final XXDownloadListener downloadListener = XXUpdateManager.getDownloadListener();
        if (downloadListener != null) {
            downloadListener.onStart();
        }
        downloadObserver.onStart();
        statusChange(XXOnUpdateStatusChangeListener.STATUS_DOWNLOAD_START);
    }

    //分发下载完成事件
    private void dispatchDownloadComplete() {
        final XXDownloadListener downloadListener = XXUpdateManager.getDownloadListener();
        if (downloadListener != null) {
            downloadListener.onComplete();
        }
        downloadObserver.onComplete();
        statusChange(XXOnUpdateStatusChangeListener.STATUS_DOWNLOAD_COMPLETE);
        Message.obtain(mHandler, INSTALL_APP).sendToTarget();
    }

    private int status = XXOnUpdateStatusChangeListener.STATUS_NONE;

    private void statusChange(int status) {
        this.status = status;
        statusChangeObserver.onUpdateStatusChange(status);
    }

    public int getStatus() {
        return status;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    //提示升级显示对话框
    private void showDialog(Context context, String updateInfo) {
        XXLogUtil.d("showDialog() called with: context = [" + context + "], updateInfo = [" + updateInfo + "]");
        dialog = new AlertDialog.Builder(context).setTitle("升级提示").
                setMessage(updateInfo).setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                downloadOrInstall();
                dialog.dismiss();
            }
        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
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

        if (timer != null) {
            timer.cancel();
        }
        unRegisterUpdateReceiver();
    }

    private class UpdateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (TextUtils.equals(intent.getAction(), XXUpdateService.ACTION_CHECK_UPDATE)) {
                check();
            }
        }
    }

    private void check() {
        //如果当前正在显示对话框不去检测升级
        if (dialog != null && dialog.isShowing()) return;
        //如果升级请求没结束，不再发起第二次请求
        if (updateRequest != null) return;
        //从网络上获取最新的版本信息
        checkUpdateInfo();
    }

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case INSTALL_COMPLETE:
                    XXLogUtil.d("PM安装完成");
                    statusChange(XXOnUpdateStatusChangeListener.STATUS_INSTALL_COMPLETE);
                    break;
                case INSTALL_START:
                    XXLogUtil.d("PM开始安装");
                    statusChange(XXOnUpdateStatusChangeListener.STATUS_INSTALL_START);
                    break;
                case INSTALL_APP:
                    XXLogUtil.d("安装APP");
                    startInstallApp();
                    break;
                default:
                    break;
            }
        }
    };

    private final int INSTALL_START = 0;
    private final int INSTALL_COMPLETE = 1;
    private final int INSTALL_APP = 2;


    class PackageInstallObserver extends IPackageInstallObserver.Stub {
        public void packageInstalled(String packageName, int returnCode) {
            Message.obtain(mHandler, INSTALL_COMPLETE, returnCode, 0).sendToTarget();
        }
    }

    private void installPackage(File file) {
        XXLogUtil.d("installPackage() called with: file = [" + file + "]");
        try {
            PackageInstallObserver observer = new PackageInstallObserver();
            Message.obtain(mHandler, INSTALL_START).sendToTarget();
            XXUtil.installPackage(this, file, observer);
        } catch (NoSuchMethodException e) {
            XXLogUtil.e("installPackage: ", e);
        } catch (InvocationTargetException e) {
            XXLogUtil.e("installPackage: ", e);
        } catch (IllegalAccessException e) {
            XXLogUtil.e("installPackage: ", e);
        }
    }

}
