package com.xiaoxin.update.task.install;

import com.xiaoxin.update.helper.ListenerHelper;
import com.xiaoxin.update.helper.UpdateStatusChangeObserver;

/**
 * Created by liyuanbiao on 2017/9/6.
 */

public abstract class InstallTask implements Runnable {
    private final UpdateStatusChangeObserver statusChangeObserver;
    private String filePath;

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
