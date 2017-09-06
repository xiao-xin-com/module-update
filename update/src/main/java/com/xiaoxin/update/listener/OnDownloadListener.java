package com.xiaoxin.update.listener;

/**
 * Created by liyuanbiao on 2016/9/17.
 */

public interface OnDownloadListener {
    void onStart();

    void onProgress(long current, long total);

    void onComplete();

    OnDownloadListener EMPTY = new SimpleDownloadListener();
}
