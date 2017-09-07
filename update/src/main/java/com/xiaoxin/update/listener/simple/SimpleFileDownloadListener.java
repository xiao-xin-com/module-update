package com.xiaoxin.update.listener.simple;

import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadListener;

/**
 * Created by liyuanbiao on 2017/9/7.
 */

public class SimpleFileDownloadListener extends FileDownloadListener {
    @Override
    public void pending(BaseDownloadTask task, int soFarBytes, int totalBytes) {

    }

    @Override
    public void progress(BaseDownloadTask task, int soFarBytes, int totalBytes) {

    }

    @Override
    public void completed(BaseDownloadTask task) {

    }

    @Override
    public void paused(BaseDownloadTask task, int soFarBytes, int totalBytes) {

    }

    @Override
    public void error(BaseDownloadTask task, Throwable e) {

    }

    @Override
    public void warn(BaseDownloadTask task) {

    }
}
