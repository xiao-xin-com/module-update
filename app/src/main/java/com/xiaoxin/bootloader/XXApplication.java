package com.xiaoxin.bootloader;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;

import com.xiaoxin.update.XXUpdateManager;
import com.xiaoxin.update.config.XXUpdateConfiguration;

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

        XXUpdateConfiguration configuration = new XXUpdateConfiguration.Builder()
                .setDebug(DEBUG)
                .setSilence(false)
                .setShowUI(true)
                .setIcon(R.mipmap.friends)
                .setTargetFile(new File(Environment.getExternalStorageDirectory(), "update.apk").getAbsolutePath())
//                .setVersionInfoProvider(versionInfoProvider)
                .setUpdateUrl(getUpdateUrl(this))
                .setApkDownloadUrl("http://120.76.232.3:1337/download/update_test_1114.apk")
                .build();
        XXUpdateManager.init(this, configuration);
    }

    public static String getUpdateUrl(Context context) {
        try {
            return new StringBuilder("http://120.76.232.3:1337/appVersion/verify?")
                    .append("packageName=").append(context.getPackageName()).append("&")
                    .append("platform=Android&versionCode=")
                    .append(context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode)
                    .toString();
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }
}
