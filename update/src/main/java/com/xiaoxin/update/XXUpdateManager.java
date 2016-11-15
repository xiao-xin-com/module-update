package com.xiaoxin.update;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;

import com.liulishuo.filedownloader.FileDownloader;
import com.xiaoxin.update.config.XXUpdateConfiguration;
import com.xiaoxin.update.listener.XXDownloadListener;
import com.xiaoxin.update.service.XXUpdateService;
import com.xiaoxin.update.util.XXUITask;

import java.lang.ref.WeakReference;

/**
 * Created by liyuanbiao on 2016/9/17.
 */

public class XXUpdateManager {
    //全局的application context
    private static Context context;
    //更新配置
    private static XXUpdateConfiguration configuration;
    //用于更新的服务
    private static XXUpdateService.UpdateBinder updateBinder;
    //显示dialog的activity
    private static WeakReference<Context> activityContext;

    //把任务加到UIThread
    public static void post(Runnable runnable) {
        XXUITask.post(runnable);
    }

    public static void autoPost(Runnable runnable) {
        XXUITask.autoPost(runnable);
    }

    //初始化
    public static void init(Context context, XXUpdateConfiguration configuration) {
        if (context == null || configuration == null) {
            throw new NullPointerException();
        }
        FileDownloader.init(context.getApplicationContext());
        XXUpdateManager.context = context.getApplicationContext();
        XXUpdateManager.configuration = configuration;
        startUpdateService();
    }

    //开启更新服务
    private static void startUpdateService() {
        Intent intent = new Intent(getContext(), XXUpdateService.class);
        getContext().startService(intent);
    }

    //销毁更新服务
    public static void unInit() {
        if (XXUpdateManager.context != null) {
            Intent intent = new Intent(getContext(), XXUpdateService.class);
            getContext().stopService(intent);
        }
    }

    public static XXUpdateConfiguration setFriendly(boolean friendly) {
        return configuration.setFriendly(friendly);
    }

    public static boolean isFriendly() {
        return configuration.isFriendly();
    }

    public static int getIcon() {
        return configuration.getIcon();
    }

    public static XXUpdateConfiguration setIcon(int icon) {
        return configuration.setIcon(icon);
    }

    public static boolean isShowUI() {
        return configuration.isShowUI();
    }

    public static XXUpdateConfiguration setShowUI(boolean showUI) {
        return configuration.setShowUI(showUI);
    }

    public static String getUpdateUrl() {
        return configuration.getUpdateUrl();
    }

    public static String getTargetFile() {
        return configuration.getTargetFile();
    }

    public static void setTargetFile(String targetFile) {
        configuration.setTargetFile(targetFile);
    }

    public static XXDownloadListener getDownloadListener() {
        return configuration.getDownloadListener();
    }

    public static boolean isDebug() {
        return configuration.isDebug();
    }

    public static void setUpdateUrl(String updateUrl) {
        configuration.setUpdateUrl(updateUrl);
    }

    public static void setDebug(boolean debug) {
        configuration.setDebug(debug);
    }

    public static void setDownloadListener(XXDownloadListener downloadListener) {
        configuration.setDownloadListener(downloadListener);
    }

    public static XXVersionInfoProvider getVersionInfoProvider() {
        return configuration.getVersionInfoProvider();
    }

    public static boolean isSilence() {
        return configuration.isSilence();
    }

    public static XXUpdateConfiguration setSilence(boolean silence) {
        return configuration.setSilence(silence);
    }

    public static XXUpdateConfiguration setVersionInfoProvider(XXVersionInfoProvider versionInfoProvider) {
        return configuration.setVersionInfoProvider(versionInfoProvider);
    }

    public static boolean isUsePm() {
        return configuration.isUsePm();
    }

    public static String getDownloadUrl() {
        return configuration.getDownloadUrl();
    }

    public static XXUpdateConfiguration setUsePm(boolean usePm) {
        return configuration.setUsePm(usePm);
    }

    public static XXUpdateConfiguration setDownloadUrl(String downloadUrl) {
        return configuration.setDownloadUrl(downloadUrl);
    }

    public static Context getContext() {
        return context;
    }

    public static WeakReference<Context> getActivityContext() {
        return activityContext;
    }

    private static ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            updateBinder = (XXUpdateService.UpdateBinder) service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    public static void onResume(Context context) {
        if (activityContext == null || activityContext.get() == null) {
            activityContext = new WeakReference<>(context);
//            bindUpdateService(context);
        }
    }

    private static void bindUpdateService(Context context) {
        Intent intent = new Intent(context, XXUpdateService.class);
        context.bindService(intent, conn, Context.BIND_AUTO_CREATE);
    }

    public static void onPause(Context context) {
        activityContext.clear();
//        unBindUpdateService(context);
    }

    private static void unBindUpdateService(Context context) {
        context.unbindService(conn);
    }


    public static void check(Context context) {
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(XXUpdateService.ACTION_CHECK_UPDATE));
    }

}
