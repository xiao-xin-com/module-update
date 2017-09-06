package com.xiaoxin.update.service;

import android.app.AlarmManager;
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
import com.xiaoxin.update.DefaultVersionProvider;
import com.xiaoxin.update.UpdateManager;
import com.xiaoxin.update.VersionInfoProvider;
import com.xiaoxin.update.bean.VersionInfo;
import com.xiaoxin.update.helper.DownloadObserver;
import com.xiaoxin.update.helper.ListenerHelper;
import com.xiaoxin.update.helper.PatchObserver;
import com.xiaoxin.update.helper.UpdateStatusChangeObserver;
import com.xiaoxin.update.listener.OnDownloadListener;
import com.xiaoxin.update.listener.OnUpdateStatusChangeListener;
import com.xiaoxin.update.net.UpdateStringRequest;
import com.xiaoxin.update.util.CmdUtil;
import com.xiaoxin.update.util.GetAppInfo;
import com.xiaoxin.update.util.NetUtil;
import com.xiaoxin.update.util.NotifyUtil;
import com.xiaoxin.update.util.UpdateLog;
import com.xiaoxin.update.util.UpdateUtil;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class UpdateService extends Service {

    private final int INSTALL_START = 0;
    private final int INSTALL_COMPLETE = 1;
    private final int INSTALL_APP = 2;

    public static final String ACTION_CHECK_UPDATE = "com.xiaoxin.update.action.service";
    public static final int AUTO_RETRY_TIMES = 10000;
    //是否是由闹钟启动的
    public static final String EXTRA_IS_ALARM = "isAlarm";

//    public static final long INTERNAL_CHECK_TIME = AlarmManager.INTERVAL_HALF_DAY;
//    public static final int DELAY_CHECK_TIME = (int) (1000 * 60 * 60 * 0.5);

    //    public static final long INTERNAL_CHECK_TIME = AlarmManager.INTERVAL_FIFTEEN_MINUTES / 5;
//    public static final int DELAY_CHECK_TIME = (int) (1000 * 60);

    //请求队列
    private RequestQueue queue;

    //服务器上APP的版本信息
    private VersionInfo versionInfo;

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

    private boolean first = true;

    private DownloadObserver downloadObserver;
    private UpdateStatusChangeObserver statusChangeObserver;
    private PatchObserver patchObserver;

    {
        downloadObserver = ListenerHelper.getDownloadObserver();
        statusChangeObserver = ListenerHelper.getStatusChangeObserver();
        patchObserver = ListenerHelper.getPatchObserver();
    }

    private UpdateBinder updateBinder = new UpdateBinder();

    //有activity绑定时，如果是提示升级，而且版本信息下载完了，则去显示Dialog
    @Override
    public IBinder onBind(Intent intent) {
        UpdateLog.d("onBind() called with: intent = [" + intent + "]");
        return updateBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        UpdateLog.d("onUnbind() called with: intent = [" + intent + "]");
        return super.onUnbind(intent);
    }

    public class UpdateBinder extends Binder {
        public int getStatus() {
            return UpdateService.this.getStatus();
        }
    }

    private void showUpdateDialog() {
        if (versionInfo != null) {
            if (!UpdateManager.isSilence() && UpdateManager.getActivityContext() != null &&
                    UpdateManager.getActivityContext().get() != null) {
                if (GetAppInfo.getAppVersionCode(this) < versionInfo.getVersionCode()) {
                    showDialog(UpdateManager.getActivityContext().get(), versionInfo.getDetail());
                }
            }
        }
    }

    @Override
    public void onCreate() {
        UpdateLog.d("onCreate() called");
        super.onCreate();
        first = true;
        initApplicationInfo();
        //初始化Volley
        initVolley();
        registerUpdateReceiver();
        initRepeatingCheck();
    }

    private void initRepeatingCheck() {
        UpdateLog.d("initRepeatingCheck() called");
        long checkSpan = UpdateManager.getCheckSpan();
        UpdateLog.d("initRepeatingCheck: checkSpan -> " + checkSpan);
        if (checkSpan > 0) {
            setRepeatingCheck();
        }
    }

    private void initApplicationInfo() {
        try {
            PackageManager packageManager = getPackageManager();
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(getPackageName(), 0);
            applicationIcon = UpdateManager.getIcon();
            applicationLable = (String) packageManager.getApplicationLabel(applicationInfo);
        } catch (PackageManager.NameNotFoundException e) {
            applicationIcon = android.R.drawable.sym_def_app_icon;
            applicationLable = getPackageName();
        }
        UpdateLog.d("initApplicationInfo() applicationLable -> " + applicationLable);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        UpdateLog.d("onStartCommand() called with: intent = [" + intent + "], flags = [" + flags + "], startId = [" + startId + "]");
        boolean isAlarm = intent != null && intent.getBooleanExtra(EXTRA_IS_ALARM, false);
        UpdateLog.d("onStartCommand: isAlarm -> " + isAlarm);
        if (isAlarm || startId == 1) {
            boolean netAvailable = NetUtil.isAvailable(getApplicationContext());
            UpdateLog.d("onStartCommand: netAvailable -> " + netAvailable);
            if (netAvailable) {
                check();
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void setRepeatingCheck() {
        UpdateLog.d("setRepeatingCheck() called");
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(getApplicationContext(), UpdateService.class);
        intent.putExtra(EXTRA_IS_ALARM, true);
        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        //取消掉之前的，防止重复
        alarmManager.cancel(pendingIntent);
        long intervalMillis = UpdateManager.getCheckSpan();
        long triggerAtMillis = System.currentTimeMillis() + intervalMillis;
        UpdateLog.d("setRepeatingCheck: 下次触发时间 " + new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date(triggerAtMillis)));
        //api19之后不精确，但是更新间隔不需要它精确
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, triggerAtMillis, intervalMillis, pendingIntent);
    }

    /**
     * 注册更新请求和网络状态改变的广播
     */
    private void registerUpdateReceiver() {
        if (updateReceiver == null) {
            UpdateLog.d("registerUpdateReceiver() called");
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
            UpdateLog.d("unRegisterUpdateReceiver() called");
            unregisterReceiver(updateReceiver);
            updateReceiver = null;
        }
    }

    //初始化Volley
    private void initVolley() {
        UpdateLog.d("initVolley() called");
        queue = Volley.newRequestQueue(this);
    }

    private boolean checking() {
        return updateRequest != null;
    }

    private boolean downloading() {
        return downloadId != -1 &&
                !TextUtils.isEmpty(UpdateManager.getTargetFile()) &&
                !FileDownloadStatus.isOver(FileDownloader.getImpl().
                        getStatus(downloadId, UpdateManager.getTargetFile()));
    }

    //获取服务器的版本
    private void checkUpdateInfo() {
        UpdateLog.d("checkUpdateInfo() called");
        String updateUrl = UpdateManager.getUpdateUrl();
        if (TextUtils.isEmpty(updateUrl)) {
            UpdateLog.e("更新链接为空");
            return;
        }

        updateRequest = queue.add(new UpdateStringRequest(updateUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                updateRequest = null;
                statusChange(OnUpdateStatusChangeListener.STATUS_CHECK_COMPLETE);
                onGetUpdateInfo(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                UpdateLog.e("网络错误...");
                UpdateLog.e(error);
                updateRequest = null;
                statusChange(OnUpdateStatusChangeListener.STATUS_CHECK_ERROR);
            }
        }).setRetryPolicy(new DefaultRetryPolicy() {
            @Override
            public int getCurrentTimeout() {
                return 30000;
            }
        }));
        statusChange(OnUpdateStatusChangeListener.STATUS_CHECK_START);
    }

    //解析服务器上的apk版本信息
    private void onGetUpdateInfo(String response) {
        UpdateLog.d("onGetUpdateInfo() called with: response = [" + response + "]");
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
        if (TextUtils.isEmpty(downloadUrl)) {
            downloadUrl = UpdateManager.getDownloadUrl();
        }

        if (TextUtils.isEmpty(downloadUrl)) {
            UpdateLog.e("下载链接为空,不能下载安装包");
            return;
        }

        int versionCode = GetAppInfo.getAppVersionCode(this);
        if (versionCode >= versionInfo.getVersionCode()) {
            UpdateLog.d("当前版本 " + versionCode + " ，" +
                    "服务端版本 " + versionInfo.getVersionCode());
            return;
        }

        downloadUrl = downloadUrl.trim();
        String lowerCase = downloadUrl.toLowerCase();
        if (!lowerCase.startsWith("http://") && !lowerCase.startsWith("https://")) {
            UpdateLog.e("这不是一个下载链接 DownloadUrl --> " + downloadUrl);
            return;
        }
        UpdateManager.setDownloadUrl(downloadUrl);

        if (!TextUtils.isEmpty(UpdateManager.getDownloadUrl())) {
            if (UpdateManager.isSilence()) {
                downloadOrInstall();
            } else {
                showUpdateDialog();
            }
        } else {
            UpdateLog.e("下载链接为空");
        }
    }

    private void downloadOrInstall() {
        UpdateLog.d("downloadOrInstall() called");
        if (isNeedDownload()) {
            downloadApk();
        } else {
            startInstallApp();
        }
    }

    private void downloadApk() {
        final String apkDownloadUrl = UpdateManager.getDownloadUrl();
        final String targetFile = UpdateManager.getTargetFile();
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
            UpdateLog.d("started() called with: task = [" + task + "]");
//            stateMap.put(task.getId(), true);
            dispatchDownloadStart();
            if (UpdateManager.isShowUI() && !UpdateManager.isSilence()) {
                NotifyUtil.create(UpdateService.this, task.getId()).notify_progress(null,
                        applicationIcon, "开始升级", applicationLable, "开始下载", false, false, false, 0, 100, false);
            }
        }

        @Override
        protected void progress(BaseDownloadTask task, int soFarBytes, int totalBytes) {
            UpdateLog.d("progress() called with: task = [" + task + "], soFarBytes = [" + soFarBytes + "], totalBytes = [" + totalBytes + "]");
            dispatchDownloadProgress(soFarBytes, totalBytes);
            if (UpdateManager.isShowUI() && !UpdateManager.isSilence()) {
                NotifyUtil.create(UpdateService.this, task.getId()).notify_progress(null,
                        applicationIcon, "开始升级", applicationLable, "正在下载...", false, false, false, totalBytes, soFarBytes, false);
            }
        }

        @Override
        protected void completed(BaseDownloadTask task) {
            UpdateLog.d("completed() called with: task = [" + task + "]");
//            stateMap.put(task.getId(), false);
            dispatchDownloadComplete();
            if (UpdateManager.isShowUI() && !UpdateManager.isSilence()) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setDataAndType(Uri.parse("file://" + task.getPath()), "application/vnd.android.package-archive");
                PendingIntent pendingIntent = PendingIntent.getActivity(UpdateService.this, task.getId(), intent, 0);
                NotifyUtil.create(UpdateService.this, task.getId()).notify_progress(pendingIntent,
                        applicationIcon, "开始升级", applicationLable, "下载完成", false, false, false, 100, 100, false);
            }
        }

        @Override
        protected void warn(BaseDownloadTask task) {
            super.warn(task);
            UpdateLog.d("warn() called with: task = [" + task + "]");
            if (UpdateManager.isShowUI() && !UpdateManager.isSilence()) {
                NotifyUtil.create(UpdateService.this, task.getId()).notify_progress(null,
                        applicationIcon, "开始升级", applicationLable, "下载错误", false, false, false, 0, 0, false);
            }
        }

        @Override
        protected void error(BaseDownloadTask task, Throwable e) {
            super.error(task, e);
            UpdateLog.d("error() called with: task = [" + task + "], e = [" + e + "]");
            statusChange(OnUpdateStatusChangeListener.STATUS_DOWNLOAD_ERROR);
            if (UpdateManager.isShowUI() && !UpdateManager.isSilence()) {
                NotifyUtil.create(UpdateService.this, task.getId()).notify_progress(null,
                        applicationIcon, "开始升级", applicationLable, "下载错误", false, false, false, 0, 0, false);
            }
        }
    };


    private void startInstallApp() {
        new Thread() {
            @Override
            public void run() {
                if (isNeedDownload()) return;
                String targetFile = UpdateManager.getTargetFile();
                if (!UpdateManager.isSilence()) {
                    startInstall(targetFile);
                } else if (UpdateManager.isSilence()) {
                    if (UpdateManager.isUsePm()) {
                        installPackage(new File(targetFile));  //pm安装
                    } else if (CmdUtil.isRoot()) {
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
                UpdateUtil.silentInstall(targetFile);
            }

            private void startInstall(String targetFile) {
                UpdateUtil.startInstall(UpdateService.this, new File(targetFile));
            }
        }.start();
    }

    private void ifFriendlyShowPop() {
        if (UpdateManager.isFriendly() &&
                UpdateManager.getActivityContext() != null &&
                UpdateManager.getActivityContext().get() != null) {
            UpdateManager.post(new Runnable() {
                @Override
                public void run() {
                    showFriendlyPop();
                }
            });
        }
    }

    private void showFriendlyPop() {
        if (UpdateManager.getActivityContext() != null &&
                UpdateManager.getActivityContext().get() != null) {
            new AlertDialog.Builder(UpdateManager.getActivityContext().get()).setTitle("应用升级中，请稍候...")
                    .setMessage(getFriendlyMsg()).show();
        }
    }

    private String getFriendlyMsg() {
        StringBuilder msgBuilder = new StringBuilder();
        msgBuilder.append("更新如下：").append("\r\n");
        if (versionInfo == null || versionInfo.getUpdateInfo() == null || versionInfo.getUpdateInfo().isEmpty()) {
            msgBuilder.append("此版本暂无更新内容");
        } else {
            List<String> updateList = versionInfo.getUpdateInfo();
            if (updateList.size() > 3) {
                updateList = updateList.subList(0, 3);
            }
            String updateInfo = DefaultVersionProvider.getUpdateInfo(updateList);
            msgBuilder.append(updateInfo);
        }
        return msgBuilder.toString();
    }

    //
    private boolean isNeedDownload() {
        //文件不存在不升级
        final Context context = UpdateManager.getContext();
        final String targetFile = UpdateManager.getTargetFile();
        if (TextUtils.isEmpty(targetFile) || !new File(targetFile).exists()) {
            return true;
        }
        //下载的应用于本应用包名不对不升级
        if (!TextUtils.equals(GetAppInfo.getAPKPackageName(context, targetFile),
                GetAppInfo.getAppPackageName(context))) {
            return true;
        }
        //versionCode小于等于本地应用也不升级
        PackageInfo packageInfo = UpdateUtil.getPackageInfo(context, targetFile);
        if (packageInfo == null || packageInfo.versionCode <= GetAppInfo.getAppVersionCode(context)) {
            new File(targetFile).delete();
            return true;
        }

        return false;
    }

    //分发下载进度事件
    private void dispatchDownloadProgress(int soFarBytes, int totalBytes) {
        final OnDownloadListener downloadListener = UpdateManager.getDownloadListener();
        if (downloadListener != null) {
            downloadListener.onProgress(soFarBytes, totalBytes);
        }
        downloadObserver.onProgress(soFarBytes, totalBytes);
        statusChange(OnUpdateStatusChangeListener.STATUS_DOWNLOADING);
    }

    //分发下载开始事件
    private void dispatchDownloadStart() {
        final OnDownloadListener downloadListener = UpdateManager.getDownloadListener();
        if (downloadListener != null) {
            downloadListener.onStart();
        }
        downloadObserver.onStart();
        statusChange(OnUpdateStatusChangeListener.STATUS_DOWNLOAD_START);
    }

    //分发下载完成事件
    private void dispatchDownloadComplete() {
        final OnDownloadListener downloadListener = UpdateManager.getDownloadListener();
        if (downloadListener != null) {
            downloadListener.onComplete();
        }
        downloadObserver.onComplete();
        statusChange(OnUpdateStatusChangeListener.STATUS_DOWNLOAD_COMPLETE);
        Message.obtain(mHandler, INSTALL_APP).sendToTarget();
    }

    private int status = OnUpdateStatusChangeListener.STATUS_NONE;

    private void statusChange(int status) {
        this.status = status;
        statusChangeObserver.onUpdateStatusChange(status);
    }

    public int getStatus() {
        return status;
    }


    //提示升级显示对话框
    private void showDialog(Context context, String updateInfo) {
        UpdateLog.d("showDialog() called with: context = [" + context + "], updateInfo = [" + updateInfo + "]");
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
        UpdateLog.d("onDestroy() called");
        super.onDestroy();
        stopRequestQueue();
        pauseDownload();
        unRegisterUpdateReceiver();
    }

    private void pauseDownload() {
        UpdateLog.d("pauseDownload() called");
        if (downloadId != -1) {
            FileDownloader.getImpl().pause(downloadId);
        }
    }

    private void stopRequestQueue() {
        UpdateLog.d("stopRequestQueue() called");
        if (queue != null) {
            queue.stop();
        }
    }

    private class UpdateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            UpdateLog.d("onReceive: action -> " + action);
            if (TextUtils.equals(action, UpdateService.ACTION_CHECK_UPDATE)) {
                if (NetUtil.isAvailable(context)) {
                    check();
                }
            } else if (TextUtils.equals(action, ConnectivityManager.CONNECTIVITY_ACTION)) {
                UpdateLog.d("onReceive: first -> " + first);
                if (NetUtil.isAvailable(context) && first) {
                    check();
                    first = false;
                }
            }
        }
    }

    private void check() {
        UpdateLog.d("check() called start");
        //如果当前正在显示对话框不去检测升级
        if (dialog != null && dialog.isShowing()) return;
        //如果升级请求没结束，不再发起第二次请求
        if (checking()) return;
        //从网络上获取最新的版本信息
        UpdateLog.d("check() called end");
        checkUpdateInfo();
    }

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case INSTALL_COMPLETE:
                    UpdateLog.d("PM安装完成");
                    statusChange(OnUpdateStatusChangeListener.STATUS_INSTALL_COMPLETE);
                    break;
                case INSTALL_START:
                    UpdateLog.d("PM开始安装");
                    ifFriendlyShowPop();
                    statusChange(OnUpdateStatusChangeListener.STATUS_INSTALL_START);
                    break;
                case INSTALL_APP:
                    UpdateLog.d("安装APP");
                    startInstallApp();
                    break;
                default:
                    break;
            }
        }
    };


    private class PackageInstallObserver extends IPackageInstallObserver.Stub {
        public void packageInstalled(String packageName, int returnCode) {
            Message.obtain(mHandler, INSTALL_COMPLETE, returnCode, 0).sendToTarget();
        }
    }

    private void installPackage(File file) {
        UpdateLog.d("installPackage() called with: file = [" + file + "]");
        try {
            Message.obtain(mHandler, INSTALL_START).sendToTarget();
            PackageInstallObserver observer = new PackageInstallObserver();
            UpdateUtil.installPackage(UpdateService.this, file, observer);
        } catch (NoSuchMethodException e) {
            UpdateLog.e("installPackage: NoSuchMethodException", e);
        } catch (InvocationTargetException e) {
            UpdateLog.e("installPackage: InvocationTargetException", e);
        } catch (IllegalAccessException e) {
            UpdateLog.e("installPackage: IllegalAccessException", e);
        }
    }

}
