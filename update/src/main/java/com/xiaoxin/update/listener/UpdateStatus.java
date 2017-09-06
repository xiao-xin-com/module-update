package com.xiaoxin.update.listener;

/**
 * Created by liyuanbiao on 2017/9/6.
 */

public interface UpdateStatus {
    //状态无
    int STATUS_NONE = 0x00;

    //检查更新
    int STATUS_CHECK_START = 0x01;
    int STATUS_CHECK_ERROR = 0x02;
    int STATUS_CHECK_COMPLETE = 0x03;

    //下载整包
    int STATUS_DOWNLOAD_START = 0x04;
    int STATUS_DOWNLOADING = 0x05;
    int STATUS_DOWNLOAD_COMPLETE = 0x06;
    int STATUS_DOWNLOAD_ERROR = 0x07;

    //下载差分包
    int STATUS_DOWNLOAD_PATCH_START = 0x08;
    int STATUS_DOWNLOADING_PATCH = 0x09;
    int STATUS_DOWNLOAD_PATCH_COMPLETE = 0x10;
    int STATUS_DOWNLOAD_PATCH_ERROR = 0x11;

    //组合差分包成整包
    int STATUS_PATCH_PREPARE = 0x12;
    int STATUS_PATCH_COMPLETE = 0x13;
    int STATUS_PATCH_ERROR = 0x14;

    //安装apk
    int STATUS_INSTALL_START = 0x15;
    int STATUS_INSTALL_COMPLETE = 0x16;
    int STATUS_INSTALL_ERROR = 0x17;
}
