package com.xiaoxin.update.listener;

/**
 * Created by liyuanbiao on 2016/11/16.
 */

public interface XXOnUpdateStatusChangeListener {
    int STATUS_NONE = 0x11;
    int STATUS_DOWNLOAD_START = 0x00;
    int STATUS_DOWNLOAD_PROGRESS = 0x01;
    int STATUS_DOWNLOAD_COMPLETE = 0x02;
    int STATUS_INSTALL_START = 0x03;
    int STATUS_INSTALL_COMPLETE = 0x04;

    void onUpdateStatusChange(int status);

    XXOnUpdateStatusChangeListener DEFAULT = new XXOnUpdateStatusChangeListener() {
        @Override
        public void onUpdateStatusChange(int status) {

        }
    };
}
