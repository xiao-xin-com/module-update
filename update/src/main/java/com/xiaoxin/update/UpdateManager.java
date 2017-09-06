package com.xiaoxin.update;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.liulishuo.filedownloader.FileDownloader;
import com.xiaoxin.update.config.UpdateConfiguration;
import com.xiaoxin.update.listener.OnDownloadListener;
import com.xiaoxin.update.listener.OnUpdateStatusChangeListener;
import com.xiaoxin.update.service.UpdateService;
import com.xiaoxin.update.util.UITask;

import java.lang.ref.WeakReference;

/**
 * Created by liyuanbiao on 2016/9/17.
 */

public class UpdateManager {
    //全局的application context
    private static Context context;
    //更新配置
    private static UpdateConfiguration configuration;
    //用于更新的服务
    private static UpdateService.UpdateBinder updateBinder;
    //显示dialog的activity
    private static WeakReference<Context> activityContext;

    private static boolean isInit;

    //把任务加到UIThread
    public static void post(Runnable runnable) {
        UITask.post(runnable);
    }

    //把任务加到UIThread
    public static void autoPost(Runnable runnable) {
        UITask.autoPost(runnable);
    }

    //初始化
    public static void init(Context context, UpdateConfiguration configuration) {
        if (context == null || configuration == null) {
            throw new NullPointerException();
        }
        if (!isInit()) {
            UpdateManager.context = context.getApplicationContext();
            UpdateManager.configuration = configuration;
            FileDownloader.init(getContext());
            isInit = true;
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

    //销毁更新服务
    public static void unInit() {
        if (getContext() != null) {
            Intent intent = new Intent(getContext(), UpdateService.class);
            unBindUpdateService(getContext());
            getContext().stopService(intent);
            isInit = false;
        }
    }

    public static int getStatus() {
        if (updateBinder != null) {
            return updateBinder.getStatus();
        }
        return OnUpdateStatusChangeListener.STATUS_NONE;
    }

    public static UpdateConfiguration setFriendly(boolean friendly) {
        return configuration.setFriendly(friendly);
    }

    public static boolean isFriendly() {
        return configuration.isFriendly();
    }

    public static int getIcon() {
        return configuration.getIcon();
    }

    public static UpdateConfiguration setIcon(int icon) {
        return configuration.setIcon(icon);
    }

    public static boolean isShowUI() {
        return configuration.isShowUI();
    }

    public static UpdateConfiguration setShowUI(boolean showUI) {
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

    public static OnDownloadListener getDownloadListener() {
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

    public static void setDownloadListener(OnDownloadListener downloadListener) {
        configuration.setDownloadListener(downloadListener);
    }

    public static VersionInfoProvider getVersionInfoProvider() {
        return configuration.getVersionInfoProvider();
    }

    public static boolean isSilence() {
        return configuration.isSilence();
    }

    public static UpdateConfiguration setSilence(boolean silence) {
        return configuration.setSilence(silence);
    }

    public static UpdateConfiguration setVersionInfoProvider(VersionInfoProvider versionInfoProvider) {
        return configuration.setVersionInfoProvider(versionInfoProvider);
    }

    public static boolean isUsePm() {
        return configuration.isUsePm();
    }

    public static String getDownloadUrl() {
        return configuration.getDownloadUrl();
    }

    public static UpdateConfiguration setUsePm(boolean usePm) {
        return configuration.setUsePm(usePm);
    }

    public static UpdateConfiguration setDownloadUrl(String downloadUrl) {
        return configuration.setDownloadUrl(downloadUrl);
    }

    public static long getCheckSpan() {
        return configuration.getCheckSpan();
    }

    public static void setCheckSpan(long checkSpan) {
        configuration.setCheckSpan(checkSpan);
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
            if (service instanceof UpdateService.UpdateBinder) {
                updateBinder = (UpdateService.UpdateBinder) service;
            }
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
        Intent intent = new Intent(context, UpdateService.class);
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
        context.sendBroadcast(new Intent(UpdateService.ACTION_CHECK_UPDATE));
    }

}