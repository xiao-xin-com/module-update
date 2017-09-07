package com.xiaoxin.update.task.install;

import com.xiaoxin.update.helper.ListenerHelper;
import com.xiaoxin.update.helper.UpdateStatusChangeObserver;
import com.xiaoxin.update.listener.OnInstallListener;

/**
 * Created by liyuanbiao on 2017/9/6.
 */

abstract class InstallTask implements Runnable {
    private final UpdateStatusChangeObserver statusChangeObserver;
    private String filePath;
    private OnInstallListener onInstallListener;

    protected void dispatchOnStart() {
        if (onInstallListener != null) {
            onInstallListener.onStart();
        }
    }

    protected void dispatchOnComplete() {
        if (onInstallListener != null) {
            onInstallListener.onComplete();
        }
    }

    protected void dispatchOnError(Throwable e) {
        if (onInstallListener != null) {
            onInstallListener.onError(e);
        }
    }

    public void setOnInstallListener(OnInstallListener onInstallListener) {
        this.onInstallListener = onInstallListener;
    }

    UpdateStatusChangeObserver getStatusChangeObserver() {
        return statusChangeObserver;
    }

    public String getFilePath() {
        return filePath;
    }

    public InstallTask(String filePath) {
        this.filePath = filePath;
        statusChangeObserver = ListenerHelper.getStatusChangeObserver();
    }

    public void install() {
        run();
    }
}
