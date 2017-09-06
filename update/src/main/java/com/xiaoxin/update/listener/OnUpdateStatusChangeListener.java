package com.xiaoxin.update.listener;

/**
 * Created by liyuanbiao on 2016/11/16.
 */

public interface OnUpdateStatusChangeListener {
    int STATUS_NONE = 0x00;
    int STATUS_CHECK_START = 0x01;
    int STATUS_CHECK_ERROR = 0x02;
    int STATUS_CHECK_COMPLETE = 0x03;
    int STATUS_DOWNLOAD_START = 0x04;
    int STATUS_DOWNLOADING = 0x05;
    int STATUS_DOWNLOAD_COMPLETE = 0x06;
    int STATUS_DOWNLOAD_ERROR = 0x07;
    int STATUS_INSTALL_START = 0x08;
    int STATUS_INSTALL_COMPLETE = 0x09;

    void onUpdateStatusChange(int status);

    OnUpdateStatusChangeListener DEFAULT = new OnUpdateStatusChangeListener() {
        @Override
        public void onUpdateStatusChange(int status) {

        }
    };
}
