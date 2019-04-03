package com.xiaoxin.update.ui;

import androidx.appcompat.app.AlertDialog;

import com.xiaoxin.update.DefaultVersionProvider;
import com.xiaoxin.update.UpdateManager;
import com.xiaoxin.update.bean.VersionInfo;
import com.xiaoxin.update.util.UpdateLog;

import java.util.List;

/**
 * Created by liyuanbiao on 2017/9/7.
 */

public class FriendlyDialog {

    private VersionInfo versionInfo;

    public FriendlyDialog(VersionInfo versionInfo) {
        this.versionInfo = versionInfo;
    }

    public void ifFriendlyShowDialog() {
        if (UpdateManager.isFriendly() &&
                UpdateManager.getActivityContext() != null &&
                UpdateManager.getActivityContext().get() != null) {
            UpdateManager.post(new Runnable() {
                @Override
                public void run() {
                    showFriendlyDialog();
                }
            });
        }
    }

    private void showFriendlyDialog() {
        if (UpdateManager.getActivityContext() != null &&
                UpdateManager.getActivityContext().get() != null) {
            new AlertDialog.Builder(UpdateManager.getActivityContext().get())
                    .setTitle("应用升级中，请稍候...")
                    .setMessage(getFriendlyMessage())
                    .show();
            UpdateLog.d("FriendlyDialog showFriendlyDialog: show");
        }
    }

    private String getFriendlyMessage() {
        StringBuilder msgBuilder = new StringBuilder();
        msgBuilder.append("更新如下：").append("\r\n");
        if (versionInfo == null || versionInfo.getUpdateInfo() == null || versionInfo.getUpdateInfo().isEmpty()) {
            msgBuilder.append("此版本暂无更新内容");
        } else {
            List<String> updateList = versionInfo.getUpdateInfo();
            if (updateList.size() > 3) {
                updateList = updateList.subList(0, 3);
            }
            String updateInfo = DefaultVersionProvider.getUpdateInfo(updateList);
            msgBuilder.append(updateInfo);
        }
        UpdateLog.d("FriendlyDialog getFriendlyMessage: " + msgBuilder);
        return msgBuilder.toString();
    }

}
