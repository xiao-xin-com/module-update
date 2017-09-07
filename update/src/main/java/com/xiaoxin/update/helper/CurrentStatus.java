package com.xiaoxin.update.helper;

import com.xiaoxin.update.listener.UpdateStatus;

/**
 * Created by liyuanbiao on 2017/9/6.
 */

public class CurrentStatus implements UpdateStatus {
    private static int status;

    public static int getStatus() {
        return status;
    }

    static void setStatus(int status) {
        CurrentStatus.status = status;
    }

    static void dispatchStatusChange(int state) {
        ListenerHelper.getStatusChangeObserver().onUpdateStatusChange(state);
    }

}
