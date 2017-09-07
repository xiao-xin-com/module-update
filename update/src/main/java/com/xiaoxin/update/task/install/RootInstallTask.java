package com.xiaoxin.update.task.install;

import com.xiaoxin.update.helper.UpdateStatusChangeObserver;
import com.xiaoxin.update.listener.UpdateStatus;
import com.xiaoxin.update.util.UpdateLog;
import com.xiaoxin.update.util.UpdateUtil;

/**
 * Created by liyuanbiao on 2017/9/6.
 */

class RootInstallTask extends InstallTask {
    public RootInstallTask(String filePath) {
        super(filePath);
    }

    @Override
    public void run() {
        UpdateLog.d("RootInstallTask run() called");
        UpdateStatusChangeObserver statusChangeObserver = getStatusChangeObserver();
        try {
            UpdateLog.d("RootInstallTask run() start");
            dispatchOnStart();
            statusChangeObserver.onUpdateStatusChange(UpdateStatus.STATUS_INSTALL_START);
            UpdateUtil.startRootInstall(getFilePath());
            UpdateLog.d("RootInstallTask run() complete");
            dispatchOnComplete();
            statusChangeObserver.onUpdateStatusChange(UpdateStatus.STATUS_INSTALL_COMPLETE);
        } catch (Exception e) {
            UpdateLog.d("RootInstallTask run() error");
            dispatchOnError(e);
            statusChangeObserver.onUpdateStatusChange(UpdateStatus.STATUS_INSTALL_ERROR);
        }
    }
}
