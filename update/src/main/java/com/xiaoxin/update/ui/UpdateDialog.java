package com.xiaoxin.update.ui;

import android.content.Context;
import android.content.DialogInterface;

import com.xiaoxin.update.UpdateManager;
import com.xiaoxin.update.bean.VersionInfo;
import com.xiaoxin.update.task.download.DownloadApkOrPatch;
import com.xiaoxin.update.util.GetAppInfo;
import com.xiaoxin.update.util.ThreadTask;
import com.xiaoxin.update.util.UpdateLog;

import androidx.appcompat.app.AlertDialog;

/**
 * Created by liyuanbiao on 2017/9/7.
 */

public class UpdateDialog {
    private Context context;
    private VersionInfo versionInfo;

    public UpdateDialog(Context context, VersionInfo versionInfo) {
        this.context = context;
        this.versionInfo = versionInfo;
    }

    public void showUpdateDialog() {
        if (versionInfo != null) {
            if (!UpdateManager.isSilence() && UpdateManager.getActivityContext() != null &&
                    UpdateManager.getActivityContext().get() != null) {
                if (GetAppInfo.getAppVersionCode(context) < versionInfo.getVersionCode()) {
                    showDialog(UpdateManager.getActivityContext().get(), versionInfo.getDetail());
                }
            }
        }
    }

    //提示升级显示对话框
    private void showDialog(final Context context, String updateInfo) {
        UpdateLog.d("UpdateDialog showDialog() called with: context = [" + context + "], updateInfo = [" + updateInfo + "]");
        AlertDialog dialog = new AlertDialog.Builder(context).setTitle("升级提示").
                setMessage(updateInfo).setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ThreadTask.execute(new DownloadApkOrPatch(context, versionInfo));
                dialog.dismiss();
            }
        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).create();
        dialog.show();
    }
}
