package com.xiaoxin.update.listener;

/**
 * Created by liyuanbiao on 2017/9/6.
 */

public interface UpdateStatus {
    //状态无
    int STATUS_NONE = 0;

    //检查更新
    int STATUS_CHECK_START = 1;
    int STATUS_CHECK_ERROR = 2;
    int STATUS_CHECK_COMPLETE = 3;

    //下载整包
    int STATUS_DOWNLOAD_START = 4;
    int STATUS_DOWNLOADING = 5;
    int STATUS_DOWNLOAD_COMPLETE = 6;
    int STATUS_DOWNLOAD_ERROR = 7;

    //下载差分包
    int STATUS_DOWNLOAD_PATCH_START = 8;
    int STATUS_DOWNLOADING_PATCH = 9;
    int STATUS_DOWNLOAD_PATCH_COMPLETE = 10;
    int STATUS_DOWNLOAD_PATCH_ERROR = 11;

    //组合差分包成整包
    int STATUS_PATCH_PREPARE = 12;
    int STATUS_PATCH_COMPLETE = 13;
    int STATUS_PATCH_ERROR = 14;

    //安装apk
    int STATUS_INSTALL_START = 15;
    int STATUS_INSTALL_COMPLETE = 16;
    int STATUS_INSTALL_ERROR = 17;
}
