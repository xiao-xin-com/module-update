package com.xiaoxin.update.task.download;

import com.liulishuo.filedownloader.FileDownloadListener;
import com.liulishuo.filedownloader.FileDownloader;
import com.xiaoxin.update.util.UpdateLog;

import java.util.concurrent.Callable;

/**
 * Created by liyuanbiao on 2017/9/6.
 */

class BaseDownloadTask implements Callable<Integer> {
    private FileDownloader fileDownloader;
    private String url;
    private String targetFile;
    private FileDownloadListener fileDownloadListener;
    private int retryTime;

    public BaseDownloadTask() {
        fileDownloader = FileDownloader.getImpl();
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTargetFile() {
        return targetFile;
    }

    public void setTargetFile(String targetFile) {
        this.targetFile = targetFile;
    }

    public FileDownloadListener getFileDownloadListener() {
        return fileDownloadListener;
    }

    public void setFileDownloadListener(FileDownloadListener fileDownloadListener) {
        this.fileDownloadListener = fileDownloadListener;
    }

    public int getRetryTime() {
        return retryTime;
    }

    public void setRetryTime(int retryTime) {
        this.retryTime = retryTime;
    }

    public FileDownloader getFileDownloader() {
        return fileDownloader;
    }

    public BaseDownloadTask(String url, String targetFile, FileDownloadListener fileDownloadListener, int retryTime) {
        this.url = url;
        this.targetFile = targetFile;
        this.fileDownloadListener = fileDownloadListener;
        this.retryTime = retryTime;
        fileDownloader = FileDownloader.getImpl();
    }

    public BaseDownloadTask(String url, String targetFile, FileDownloadListener fileDownloadListener) {
        this(url, targetFile, fileDownloadListener, 10000);
    }

    @Override
    public Integer call() throws Exception {
        UpdateLog.d("startDownload() called with: url = [" + url + "], targetFile = [" + targetFile + "], retryTime = [" + retryTime + "]");
        return fileDownloader.create(url).
                setListener(fileDownloadListener)
                .setPath(targetFile)
                .setAutoRetryTimes(retryTime).start();
    }

    public Integer startDownload() throws Exception {
        return call();
    }

}
