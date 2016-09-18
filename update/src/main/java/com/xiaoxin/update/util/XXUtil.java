package com.xiaoxin.update.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;

import java.io.File;
import java.io.IOException;

/**
 * Created by liyuanbiao on 2016/9/17.
 */

public class XXUtil {

    public static PackageInfo getPackageInfo(Context context, String apkPath) {
        PackageManager pm = context.getPackageManager();
        PackageInfo info = pm.getPackageArchiveInfo(apkPath, PackageManager.GET_ACTIVITIES);
        return info;
    }

    public static void slientInstall(String filePath) throws IOException, InterruptedException {
        execRootCmdSilent("pm install -r " + filePath);
    }

    public static void startInstall(Context context, File apkFile) {
        if (!apkFile.exists()) {
            return;
        }
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(Uri.parse("file://" + apkFile.toString()), "application/vnd.android.package-archive");
        context.startActivity(intent);
    }

    private static void execRootCmdSilent(String s) throws IOException, InterruptedException {
        XXCmdUtil.execRootCmdSilent(s);
    }
}
