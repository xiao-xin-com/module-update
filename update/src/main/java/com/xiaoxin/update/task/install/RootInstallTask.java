package com.xiaoxin.update.task.install;

import com.xiaoxin.update.helper.UpdateStatusChangeObserver;
import com.xiaoxin.update.listener.UpdateStatus;
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
        UpdateStatusChangeObserver statusChangeObserver = getStatusChangeObserver();
        try {
            dispatchOnStart();
            statusChangeObserver.onUpdateStatusChange(UpdateStatus.STATUS_INSTALL_START);
            UpdateUtil.startRootInstall(getFilePath());
            dispatchOnComplete();
            statusChangeObserver.onUpdateStatusChange(UpdateStatus.STATUS_INSTALL_COMPLETE);
        } catch (Exception e) {
            dispatchOnError(e);
            statusChangeObserver.onUpdateStatusChange(UpdateStatus.STATUS_INSTALL_ERROR);
        }
    }
}
