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
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
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
import com.liulishuo.filedownloader.model.FileDownloadStatus;
import com.xiaoxin.update.XXDefaultVersionProvider;
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
import com.xiaoxin.update.util.XXNetUtil;
import com.xiaoxin.update.util.XXNotifyUtil;
import com.xiaoxin.update.util.XXUITask;
import com.xiaoxin.update.util.XXUtil;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class XXUpdateService extends Service {

    private final int INSTALL_START = 0;
    private final int INSTALL_COMPLETE = 1;
    private final int INSTALL_APP = 2;

    public static final String ACTION_CHECK_UPDATE = "com.xiaoxin.update.action.service";
    public static final int AUTO_RETRY_TIMES = 10000;

    public static final int INTERNAL_CHECK_TIME = 1000 * 60 * 60 * 3;
    public static final int DELAY_CHECK_TIME = (int) (1000 * 60 * 60 * 0.5);

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
//    private Map<Integer, Boolean> stateMap = new HashMap<>();

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

    private UpdateBinder updateBinder = new UpdateBinder();

    //有activity绑定时，如果是提示升级，而且版本信息下载完了，则去显示Dialog
    @Override
    public IBinder onBind(Intent intent) {
        XXLogUtil.d("onBind() called with: intent = [" + intent + "]");
        return updateBinder;
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
    public void onCreate() {
        XXLogUtil.d("onCreate() called");
        super.onCreate();
        initApplicationInfo();
        //初始化Volley
        initVolley();
        registerUpdateReceiver();
        //在服务开启的时候就去检测有没有版本要更新
        if (XXNetUtil.isAvailable(this)) {
            checkUpdateInfo();
        }
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
        XXLogUtil.d("initApplicationInfo() applicationLable -> " + applicationLable);
    }

    private Timer timer;


    private void timerCheck() {
        timer = new Timer();
        timer.schedule(new UpdateTask(), DELAY_CHECK_TIME, INTERNAL_CHECK_TIME);
    }

    private class UpdateTask extends TimerTask {

        @Override
        public void run() {
            XXLogUtil.d("timer check() called");
            XXUITask.post(new Runnable() {
                @Override
                public void run() {
                    check();
                }
            });
        }
    }

    /**
     * 注册更新请求和网络状态改变的广播
     */
    private void registerUpdateReceiver() {
        if (updateReceiver == null) {
            XXLogUtil.d("registerUpdateReceiver() called");
            updateReceiver = new UpdateReceiver();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(ACTION_CHECK_UPDATE);
            intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
            registerReceiver(updateReceiver, intentFilter);
        }
    }

    /**
     * 取消注册广播
     */
    private void unRegisterUpdateReceiver() {
        if (updateReceiver != null) {
            XXLogUtil.d("unRegisterUpdateReceiver() called");
            unregisterReceiver(updateReceiver);
            updateReceiver = null;
        }
    }

    //初始化Volley
    private void initVolley() {
        XXLogUtil.d("initVolley() called");
        queue = Volley.newRequestQueue(this);
    }

    private boolean checking() {
        return updateRequest != null;
    }

    private boolean downloading() {
        return downloadId != -1 &&
                !TextUtils.isEmpty(XXUpdateManager.getTargetFile()) &&
                !FileDownloadStatus.isOver(FileDownloader.getImpl().
                        getStatus(downloadId, XXUpdateManager.getTargetFile()));
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
                statusChange(XXOnUpdateStatusChangeListener.STATUS_CHECK_COMPLETE);
                onGetUpdateInfo(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                XXLogUtil.e("网络错误...");
                XXLogUtil.e(error);
                updateRequest = null;
                statusChange(XXOnUpdateStatusChangeListener.STATUS_CHECK_ERROR);
            }
        }).setRetryPolicy(new DefaultRetryPolicy() {
            @Override
            public int getCurrentTimeout() {
                return 30000;
            }
        }));
        statusChange(XXOnUpdateStatusChangeListener.STATUS_CHECK_START);
    }

    //解析服务器上的apk版本信息
    private void onGetUpdateInfo(String response) {
        XXLogUtil.d("onGetUpdateInfo() called with: response = [" + response + "]");
        XXVersionInfoProvider versionInfoProvider = XXUpdateManager.getVersionInfoProvider();
        if (versionInfoProvider == null) {
            XXLogUtil.e("VersionInfoProvider为空,不能解析更新内容");
            return;
        }

        versionInfo = versionInfoProvider.provider(response);
        if (versionInfo == null) {
            XXLogUtil.e("VersionInfo为空,不能解析更新内容");
            return;
        }

        String downloadUrl = versionInfo.getDownloadUrl();
        if (TextUtils.isEmpty(downloadUrl)) {
            downloadUrl = XXUpdateManager.getDownloadUrl();
        }

        if (TextUtils.isEmpty(downloadUrl)) {
            XXLogUtil.e("下载链接为空,不能下载安装包");
            return;
        }

        downloadUrl = downloadUrl.trim();
        String lowerCase = downloadUrl.toLowerCase();
        if (!lowerCase.startsWith("http://") && !lowerCase.startsWith("https://")) {
            XXLogUtil.e("这不是一个下载链接 DownloadUrl --> " + downloadUrl);
            return;
        }
        XXUpdateManager.setDownloadUrl(downloadUrl);

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
            if (!downloading()) {
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
//            stateMap.put(task.getId(), true);
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
//            stateMap.put(task.getId(), false);
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

        @Override
        protected void warn(BaseDownloadTask task) {
            super.warn(task);
            XXLogUtil.d("warn() called with: task = [" + task + "]");
            if (XXUpdateManager.isShowUI() && !XXUpdateManager.isSilence()) {
                XXNotifyUtil.create(XXUpdateService.this, task.getId()).notify_progress(null,
                        applicationIcon, "开始升级", applicationLable, "下载错误", false, false, false, 0, 0, false);
            }
        }

        @Override
        protected void error(BaseDownloadTask task, Throwable e) {
            super.error(task, e);
            XXLogUtil.d("error() called with: task = [" + task + "], e = [" + e + "]");
            statusChange(XXOnUpdateStatusChangeListener.STATUS_DOWNLOAD_ERROR);
            if (XXUpdateManager.isShowUI() && !XXUpdateManager.isSilence()) {
                XXNotifyUtil.create(XXUpdateService.this, task.getId()).notify_progress(null,
                        applicationIcon, "开始升级", applicationLable, "下载错误", false, false, false, 0, 0, false);
            }
        }
    };


    private void startInstallApp() {
        new Thread() {
            @Override
            public void run() {
                if (isNeedDownload()) return;
                String targetFile = XXUpdateManager.getTargetFile();
                if (!XXUpdateManager.isSilence()) {
                    startInstall(targetFile);
                } else if (XXUpdateManager.isSilence()) {
                    if (XXUpdateManager.isUsePm()) {
                        installPackage(new File(targetFile));  //pm安装
                    } else if (XXCmdUtil.isRoot()) {
                        try {
                            silentInstall(targetFile);//非pm安装下，有root权限adb安装
                        } catch (Exception e) {
                            startInstall(targetFile); //异常则普通安装
                        }
                    } else {
                        startInstall(targetFile);  //无root权限普通安装
                    }
                }
            }

            private void silentInstall(String targetFile) throws IOException, InterruptedException {
                XXUtil.silentInstall(targetFile);
            }

            private void startInstall(String targetFile) {
                XXUtil.startInstall(XXUpdateService.this, new File(targetFile));
            }
        }.start();
    }

    private void ifFriendlyShowPop() {
        if (XXUpdateManager.isFriendly() &&
                XXUpdateManager.getActivityContext() != null &&
                XXUpdateManager.getActivityContext().get() != null) {
            XXUpdateManager.post(new Runnable() {
                @Override
                public void run() {
                    showFriendlyPop();
                }
            });
        }
    }

    private void showFriendlyPop() {
        if (XXUpdateManager.getActivityContext() != null &&
                XXUpdateManager.getActivityContext().get() != null) {
            new AlertDialog.Builder(XXUpdateManager.getActivityContext().get()).setTitle("应用升级中，请稍候...")
                    .setMessage(getFriendlyMsg()).show();
        }
    }

    private String getFriendlyMsg() {
        StringBuilder msgBuilder = new StringBuilder();
        msgBuilder.append("更新如下：").append("\r\n");
        if (versionInfo == null || versionInfo.getUpdateList() == null || versionInfo.getUpdateList().isEmpty()) {
            msgBuilder.append("此版本暂无更新内容");
        } else {
            List<String> updateList = versionInfo.getUpdateList();
            if (updateList.size() > 3) {
                updateList = updateList.subList(0, 3);
            }
            String updateInfo = XXDefaultVersionProvider.getUpdateInfo(updateList);
            msgBuilder.append(updateInfo);
        }
        return msgBuilder.toString();
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
        if (!TextUtils.equals(XXGetAppInfo.getAPKPackageName(context, targetFile),
                XXGetAppInfo.getAppPackageName(context))) {
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
        statusChange(XXOnUpdateStatusChangeListener.STATUS_DOWNLOADING);
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
        private boolean first = true;

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            XXLogUtil.d("onReceive: action -> " + action);
            if (TextUtils.equals(action, XXUpdateService.ACTION_CHECK_UPDATE)) {
                if (XXNetUtil.isAvailable(context)) {
                    check();
                }
            } else if (TextUtils.equals(action, ConnectivityManager.CONNECTIVITY_ACTION)) {
                XXLogUtil.d("onReceive: first -> " + first);
                if (XXNetUtil.isAvailable(context) && first) {
                    check();
                    first = false;
                }
            }
        }
    }

    private void check() {
        //如果当前正在显示对话框不去检测升级
        if (dialog != null && dialog.isShowing()) return;
        //如果升级请求没结束，不再发起第二次请求
        if (checking()) return;
        //从网络上获取最新的版本信息
        XXLogUtil.d("check() called");
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
                    ifFriendlyShowPop();
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


    class PackageInstallObserver extends IPackageInstallObserver.Stub {
        public void packageInstalled(String packageName, int returnCode) {
            Message.obtain(mHandler, INSTALL_COMPLETE, returnCode, 0).sendToTarget();
        }
    }

    private void installPackage(File file) {
        XXLogUtil.d("installPackage() called with: file = [" + file + "]");
        try {
            Message.obtain(mHandler, INSTALL_START).sendToTarget();
            PackageInstallObserver observer = new PackageInstallObserver();
            XXUtil.installPackage(XXUpdateService.this, file, observer);
        } catch (NoSuchMethodException e) {
            XXLogUtil.e("installPackage: ", e);
        } catch (InvocationTargetException e) {
            XXLogUtil.e("installPackage: ", e);
        } catch (IllegalAccessException e) {
            XXLogUtil.e("installPackage: ", e);
        }
    }

}
