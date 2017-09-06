package com.xiaoxin.update.listener;

/**
 * Created by liyuanbiao on 2016/11/16.
 */

public interface OnUpdateStatusChangeListener extends UpdateStatus {

    void onUpdateStatusChange(int status);

    OnUpdateStatusChangeListener DEFAULT = new OnUpdateStatusChangeListener() {
        @Override
        public void onUpdateStatusChange(int status) {

        }
    };
}
