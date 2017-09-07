package com.xiaoxin.update;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.liulishuo.filedownloader.FileDownloader;
import com.xiaoxin.update.config.InstallMode;
import com.xiaoxin.update.config.UpdateConfiguration;
import com.xiaoxin.update.exception.ServiceConnectedException;
import com.xiaoxin.update.helper.ConnectObserver;
import com.xiaoxin.update.helper.CurrentStatus;
import com.xiaoxin.update.helper.ListenerHelper;
import com.xiaoxin.update.listener.OnConnectListener;
import com.xiaoxin.update.listener.OnDownloadListener;
import com.xiaoxin.update.service.UpdateService;
import com.xiaoxin.update.util.UITask;

import java.lang.ref.WeakReference;

/**
 * Created by liyuanbiao on 2016/9/17.
 */

public class UpdateManager {
    //全局的application context
    @SuppressLint("StaticFieldLeak")
    private static Context context;
    //更新配置
    @SuppressLint("StaticFieldLeak")
    private static UpdateConfiguration configuration;
    //用于更新的服务
    private static UpdateService.UpdateBinder updateBinder;
    //显示dialog的activity
    private static WeakReference<Context> activityContext;
    //是否被初始化
    private static boolean isInit;

    //把任务加到UIThread
    public static void post(Runnable runnable) {
        UITask.post(runnable);
    }

    //把任务加到UIThread，如果当前在主线程则直接执行
    public static void autoPost(Runnable runnable) {
        UITask.autoPost(runnable);
    }

    //连接状态分发
    private static ConnectObserver connectObserver = ListenerHelper.getConnectObserver();

    public static void registerOnConnectListener(OnConnectListener observer) {
        connectObserver.registerOnConnectListener(observer);
    }

    public static void unregisterOnConnectListener(OnConnectListener observer) {
        connectObserver.unregisterOnConnectListener(observer);
    }

    public static void unregisterAllOnConnectListener() {
        connectObserver.unregisterAllOnConnectListener();
    }

    //初始化
    public static void init(Context context, UpdateConfiguration configuration) {
        if (context == null || configuration == null) {
            throw new NullPointerException();
        }
        if (!isInit()) {
            UpdateManager.context = context.getApplicationContext();
            UpdateManager.configuration = configuration;
            FileDownloader.setupOnApplicationOnCreate(((Application) UpdateManager.context));
            startUpdateService();
        }
    }

    public static boolean isInit() {
        return isInit;
    }

    //开启更新服务
    private static void startUpdateService() {
        Intent intent = new Intent(getContext(), UpdateService.class);
        getContext().startService(intent);
        bindUpdateService(getContext());
    }

    //绑定更新服务
    private static void bindUpdateService(Context context) {
        Intent intent = new Intent(context, UpdateService.class);
        context.bindService(intent, conn, Context.BIND_AUTO_CREATE);
    }

    //解绑更新服务
    private static void unBindUpdateService(Context context) {
        context.unbindService(conn);
    }

    //销毁更新服务
    public static void release() {
        Context context = getContext();
        if (context != null) {
            Intent intent = new Intent(context, UpdateService.class);
            unBindUpdateService(context);
            context.stopService(intent);
            isInit = false;
        }
    }

    public static Context getContext() {
        return configuration.getContext();
    }

    public static void setContext(Context context) {
        configuration.setContext(context);
    }

    public static String getUpdateUrl() {
        return configuration.getUpdateUrl();
    }

    public static void setUpdateUrl(String updateUrl) {
        configuration.setUpdateUrl(updateUrl);
    }

    public static String getTargetFile() {
        return configuration.getTargetFile();
    }

    public static void setTargetFile(String targetFile) {
        configuration.setTargetFile(targetFile);
    }

    public static boolean isDebug() {
        return configuration.isDebug();
    }

    public static void setDebug(boolean debug) {
        configuration.setDebug(debug);
    }

    public static boolean isSilence() {
        return configuration.isSilence();
    }

    public static void setSilence(boolean silence) {
        configuration.setSilence(silence);
    }

    public static boolean isShowUI() {
        return configuration.isShowUI();
    }

    public static void setShowUI(boolean showUI) {
        configuration.setShowUI(showUI);
    }

    public static boolean isIncrement() {
        return configuration.isIncrement();
    }

    public static void setIncrement(boolean increment) {
        configuration.setIncrement(increment);
    }

    public static InstallMode getInstallMode() {
        return configuration.getInstallMode();
    }

    public static void setInstallMode(InstallMode installMode) {
        configuration.setInstallMode(installMode);
    }

    public static boolean isFriendly() {
        return configuration.isFriendly();
    }

    public static void setFriendly(boolean friendly) {
        configuration.setFriendly(friendly);
    }

    public static int getIcon() {
        return configuration.getIcon();
    }

    public static void setIcon(int icon) {
        configuration.setIcon(icon);
    }

    public static VersionInfoProvider getVersionInfoProvider() {
        return configuration.getVersionInfoProvider();
    }

    public static void setVersionInfoProvider(VersionInfoProvider versionInfoProvider) {
        configuration.setVersionInfoProvider(versionInfoProvider);
    }

    public static OnDownloadListener getDownloadListener() {
        return configuration.getDownloadListener();
    }

    public static void setDownloadListener(OnDownloadListener downloadListener) {
        configuration.setDownloadListener(downloadListener);
    }

    public static long getCheckSpan() {
        return configuration.getCheckSpan();
    }

    public static void setCheckSpan(long checkSpan) {
        configuration.setCheckSpan(checkSpan);
    }

    public static String getDownloadUrl() {
        return configuration.getDownloadUrl();
    }

    public static void setDownloadUrl(String downloadUrl) {
        configuration.setDownloadUrl(downloadUrl);
    }

    public static String getPatchTargetFile() {
        return configuration.getPatchTargetFile();
    }

    public static void setPatchTargetFile(String patchTargetFile) {
        configuration.setPatchTargetFile(patchTargetFile);
    }

    private static ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            if (service instanceof UpdateService.UpdateBinder) {
                isInit = true;
                connectObserver.onConnected(null);
                updateBinder = (UpdateService.UpdateBinder) service;
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            connectObserver.onConnected(new ServiceConnectedException(name));
        }
    };

    public static UpdateConfiguration getConfiguration() {
        return configuration;
    }

    public static int getStatus() {
        if (updateBinder != null && updateBinder.isBinderAlive()) {
            return updateBinder.getStatus();
        }
        return CurrentStatus.getStatus();
    }


    public static WeakReference<Context> getActivityContext() {
        return activityContext;
    }

    public static void onResume(Context context) {
        if (activityContext == null || activityContext.get() == null) {
            activityContext = new WeakReference<>(context);
//            bindUpdateService(context);
        }
    }

    public static void onPause(Context context) {
        activityContext.clear();
//        unBindUpdateService(context);
    }


    public static void check(Context context) {
        context.sendBroadcast(new Intent(UpdateService.ACTION_CHECK_UPDATE));
    }

}
