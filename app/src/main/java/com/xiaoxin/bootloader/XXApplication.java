package com.xiaoxin.bootloader;

import android.app.Application;
import android.content.Context;
import android.os.Environment;

import com.xiaoxin.update.UpdateManager;
import com.xiaoxin.update.config.InstallMode;
import com.xiaoxin.update.config.UpdateConfiguration;
import com.xiaoxin.update.util.GetAppInfo;

import java.io.File;


/**
 * Created by liyuanbiao on 2016/9/17.
 */

public class XXApplication extends Application {
    public static final boolean DEBUG = true;
    private static XXApplication app;

    public static XXApplication getApp() {
        return app;
    }

    public XXApplication() {
        app = this;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        UpdateConfiguration configuration = new UpdateConfiguration.Builder(this)
                .setDebug(DEBUG)//debug
                .setIncrement(true) //是否增量更新
                .setSilence(false)//是否静默升级
                .setShowUI(true)//是否显示进度条
                .setFriendly(true) //静默更新前是否显示详情
                .setInstallMode(InstallMode.SYSTEM)//设置安装模式
                .setCheckSpan(1000 * 60 * 60)//设置检查间隔
                .setIcon(R.mipmap.friends)//设置显示的图标
                .setTargetFile(new File(Environment.getExternalStorageDirectory(), "test.apk").getAbsolutePath())//apk存放地址
//                .setVersionInfoProvider(versionInfoProvider)
                .setPatchTargetFile(new File(Environment.getExternalStorageDirectory(), "test.patch").getAbsolutePath())
                .setUpdateUrl(getUpdateUrl(this))//检测升级的url
//                .setDownloadUrl("http://120.76.232.3:1337/download/update_test_1114.apk")//文件下载的url
                .build();
        UpdateManager.init(this, configuration);
    }

    //http://192.168.1.241/
    public static String getUpdateUrl(Context context) {
        return getUpdateUrl(context, "test");

    }

    public static String getUpdateUrl(Context context, String model) {
        return new StringBuilder("http://192.168.1.77:1337/appVersion/verify?")
                .append("packageName=").append(GetAppInfo.getAppPackageName(context))
                .append("&platform=Android&versionCode=")
                .append(GetAppInfo.getAppVersionCode(context))
                .append("&model=").append(model == null ? "" : model)
                .toString();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }
}
