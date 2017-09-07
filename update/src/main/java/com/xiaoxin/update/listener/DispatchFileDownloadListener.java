package com.xiaoxin.update.listener;

import com.liulishuo.filedownloader.BaseDownloadTask;
import com.xiaoxin.update.helper.DispatchDownloadEvent;
import com.xiaoxin.update.listener.simple.SimpleFileDownloadListener;

/**
 * Created by liyuanbiao on 2017/9/7.
 */

public class DispatchFileDownloadListener extends SimpleFileDownloadListener {
    private DispatchDownloadEvent downloadEvent;

    public DispatchFileDownloadListener(DispatchDownloadEvent downloadEvent) {
        this.downloadEvent = downloadEvent;
    }

    @Override
    public void progress(BaseDownloadTask task, int soFarBytes, int totalBytes) {
        super.progress(task, soFarBytes, totalBytes);
        downloadEvent.progress(task, soFarBytes, totalBytes);
    }

    @Override
    public void completed(BaseDownloadTask task) {
        super.completed(task);
        downloadEvent.completed(task);
    }

    @Override
    public void error(BaseDownloadTask task, Throwable e) {
        super.error(task, e);
        downloadEvent.error(task, e);
    }

    @Override
    public void warn(BaseDownloadTask task) {
        super.warn(task);
        downloadEvent.warn(task);
    }

    @Override
    protected void started(BaseDownloadTask task) {
        super.started(task);
        downloadEvent.started(task);
    }
}
