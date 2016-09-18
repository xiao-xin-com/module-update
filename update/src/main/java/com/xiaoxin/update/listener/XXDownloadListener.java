package com.xiaoxin.update.listener;

/**
 * Created by liyuanbiao on 2016/9/17.
 */

public interface XXDownloadListener {
    void onStart();

    void onProgress(long current, long total);

    void onComplete();

    XXDownloadListener EMPTY = new XXDownloadListener() {
        @Override
        public void onStart() {

        }

        @Override
        public void onProgress(long current, long total) {

        }

        @Override
        public void onComplete() {

        }
    };
}
