package com.xiaoxin.update.listener;

/**
 * Created by liyuanbiao on 2016/9/17.
 */

public interface OnDownloadListener {
    void onStart(String path);

    void onProgress(long current, long total);

    void onComplete(String path);

    void onError(String path, Throwable e);

    OnDownloadListener EMPTY = new SimpleDownloadListener();
}
