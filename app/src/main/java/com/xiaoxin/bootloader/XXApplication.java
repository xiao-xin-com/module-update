package com.xiaoxin.bootloader;

import android.app.Application;
import android.os.Environment;

import com.xiaoxin.update.XXUpdateManager;
import com.xiaoxin.update.config.XXUpdateConfiguration;

import java.io.File;

import static com.xiaoxin.bootloader.util.XXJsonUtil.versionInfoProvider;


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

        XXUpdateConfiguration configuration = new XXUpdateConfiguration.Builder()
                .setDebug(DEBUG)
                .setSilence(false)
                .setTargetFile(new File(Environment.getExternalStorageDirectory(), "xiaoxintong.apk").getAbsolutePath())
                .setVersionInfoProvider(versionInfoProvider)
                .setUpdateUrl("http://192.168.1.76/update.json")
                .setApkDownloadUrl("http://192.168.1.76/update.apk")
                .build();
        XXUpdateManager.init(this, configuration);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }
}
