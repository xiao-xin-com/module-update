package com.xiaoxin.update.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.IPackageInstallObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.text.TextUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by liyuanbiao on 2016/9/17.
 */

public class UpdateUtil {

    /**
     * 获得本地apk的PackageInfo
     *
     * @param context
     * @param apkPath
     * @return
     */
    public static PackageInfo getPackageInfo(Context context, String apkPath) {
        PackageManager pm = context.getPackageManager();
        PackageInfo info = pm.getPackageArchiveInfo(apkPath, PackageManager.GET_ACTIVITIES);
        return info;
    }

    /**
     * pm命令安装，有root权限可用
     *
     * @param filePath
     * @throws IOException
     * @throws InterruptedException
     */
    public static void silentInstall(String filePath) throws IOException, InterruptedException {
        if (TextUtils.isEmpty(filePath) || !new File(filePath).exists()) return;
        execRootCmdSilent("pm install -r " + filePath);
    }

    /**
     * 调用系统安装界面
     *
     * @param context
     * @param apkFile
     */
    public static void startInstall(Context context, File apkFile) {
        if (apkFile == null || !apkFile.exists()) return;

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(Uri.parse("file://" + apkFile.toString()), "application/vnd.android.package-archive");
        context.startActivity(intent);
    }

    /**
     * 执行终端命令
     *
     * @param s
     * @throws IOException
     * @throws InterruptedException
     */
    private static void execRootCmdSilent(String s) throws IOException, InterruptedException {
        CmdUtil.execRootCmdSilent(s);
    }

    /**
     * 需要反射的方法
     */
    private static final String INSTALL_METHOD = "installPackage";
    /**
     * 覆盖安装
     */
    private static final int INSTALL_REPLACE_EXISTING = 0x00000002;
    /**
     * 安装时不杀死App，但是还是杀死了，我也不明白
     */
    private static final int INSTALL_DONT_KILL_APP = 0x00001000;

    /**
     * 反射PackageManager安装，拥有系统签名可用
     *
     * @param context
     * @param file
     * @param observer
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    public static void installPackage(Context context, File file, IPackageInstallObserver observer)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        PackageManager packageManager = context.getPackageManager();
        Method method = PackageManager.class.getDeclaredMethod(INSTALL_METHOD, Uri.class,
                IPackageInstallObserver.class, int.class, String.class);
        method.invoke(packageManager, Uri.fromFile(file), observer, INSTALL_REPLACE_EXISTING | INSTALL_DONT_KILL_APP, GetAppInfo.getAPKPackageName(context, file.getAbsolutePath()));
    }
}
