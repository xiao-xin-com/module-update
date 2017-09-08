package com.xiaoxin.update.task.install;

import android.content.Context;
import android.content.pm.IPackageInstallObserver;
import android.os.RemoteException;
import android.text.TextUtils;

import com.xiaoxin.update.UpdateManager;
import com.xiaoxin.update.bean.VersionInfo;
import com.xiaoxin.update.config.InstallMode;
import com.xiaoxin.update.listener.simple.SimpleInstallListener;
import com.xiaoxin.update.ui.FriendlyDialog;
import com.xiaoxin.update.util.CmdUtil;
import com.xiaoxin.update.util.UpdateLog;

import java.io.File;

/**
 * Created by liyuanbiao on 2017/9/7.
 */

public class InstallApkThread implements Runnable {
    private Context context;
    private VersionInfo versionInfo;
    private FriendlyDialog friendlyDialog;

    public InstallApkThread(Context context, VersionInfo versionInfo) {
        this.context = context;
        this.versionInfo = versionInfo;
        this.friendlyDialog = new FriendlyDialog(versionInfo);
    }

    @Override
    public void run() {
        UpdateLog.d("InstallApkThread run() called");
        final String targetFile = UpdateManager.getTargetFile();
        //不存在就不需要操作
        if (TextUtils.isEmpty(targetFile) ||
                !new File(targetFile).exists()) {
            UpdateLog.d("InstallApkThread run: 文件不存在");
            return;
        }
        //如果选择系统安装界面，直接调起
        if (UpdateManager.getInstallMode() == InstallMode.SYSTEM) {
            systemInstall(targetFile);
        } else if (UpdateManager.isSilence()) { //选择静默安装，分为root和pm两种
            //选择pm安装
            if (UpdateManager.getInstallMode() == InstallMode.PM) {
                UpdateLog.d("InstallApkThread run: InstallMode PM");
                PmInstallTask pmInstallTask = new PmInstallTask(context, targetFile, new PackageInstallObserver());//pm安装
                pmInstallTask.setOnInstallListener(new SimpleInstallListener() {
                    @Override
                    public void onStart() {
                        super.onStart();
                        UpdateLog.d("InstallApkThread run: InstallMode PM START");
                        friendlyDialog.ifFriendlyShowDialog();
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        UpdateLog.d("InstallApkThread run: InstallMode PM ERROR");
                        //pm安装失败选择系统安装
                        systemInstall(targetFile);
                    }
                });
                pmInstallTask.install();
            } else if (CmdUtil.isRoot()) {//如果存在root权限的话
                UpdateLog.d("InstallApkThread run: InstallMode ROOT");
                RootInstallTask rootInstallTask = new RootInstallTask(targetFile);
                rootInstallTask.setOnInstallListener(new SimpleInstallListener() {
                    @Override
                    public void onStart() {
                        super.onStart();
                        UpdateLog.d("InstallApkThread run: InstallMode ROOT START");
                        friendlyDialog.ifFriendlyShowDialog();
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        UpdateLog.d("InstallApkThread run: InstallMode ROOT ERROR");
                        //root安装失败选择系统安装
                        systemInstall(targetFile);
                    }
                });
                rootInstallTask.install();
            } else {
                //选择了静默安装，但是又不是pm和root模式，还是只能系统安装了
                systemInstall(targetFile);
            }
        }
    }

    private void systemInstall(String targetFile) {
        UpdateLog.d("run: InstallMode SYSTEM");
        new SystemInstallTask(context, targetFile).install();
    }

    private static class PackageInstallObserver extends IPackageInstallObserver.Stub {

        @Override
        public void packageInstalled(String packageName, int returnCode) throws RemoteException {

        }
    }
}
