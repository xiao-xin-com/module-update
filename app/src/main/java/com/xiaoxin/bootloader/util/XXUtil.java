package com.xiaoxin.bootloader.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

/**
 * Created by liyuanbiao on 2016/9/17.
 */

public class XXUtil {

    public static PackageInfo getPackageInfo(Context context, String apkPath) {
        PackageManager pm = context.getPackageManager();
        PackageInfo info = pm.getPackageArchiveInfo(apkPath, PackageManager.GET_ACTIVITIES);
        return info;
    }

    public static void slientInstall(String filePath) {
        execRootCmdSilent("pm install -r " + filePath);
}

    private static void execRootCmdSilent(String s) {
        XXCmdUtil.execRootCmdSilent(s);
    }
}
