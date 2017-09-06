package com.xiaoxin.update.helper;

import android.database.Observable;

import com.xiaoxin.update.listener.OnDownloadListener;

/**
 * Created by liyuanbiao on 2016/11/16.
 */

public class DownloadObserver extends Observable<OnDownloadListener> implements OnDownloadListener {
    @Override
    public void onStart() {
        synchronized (mObservers) {
            for (int i = mObservers.size() - 1; i >= 0; i--) {
                mObservers.get(i).onStart();
            }
        }
    }

    @Override
    public void onProgress(long current, long total) {
        synchronized (mObservers) {
            for (int i = mObservers.size() - 1; i >= 0; i--) {
                mObservers.get(i).onProgress(current, total);
            }
        }
    }

    @Override
    public void onComplete() {
        synchronized (mObservers) {
            for (int i = mObservers.size() - 1; i >= 0; i--) {
                mObservers.get(i).onComplete();
            }
        }
    }

    public void registerDownloadListener(OnDownloadListener observer) {
        super.registerObserver(observer);
    }

    public void unregisterDownloadListener(OnDownloadListener observer) {
        super.unregisterObserver(observer);
    }

    public void unregisterAllDownloadListener() {
        super.unregisterAll();
    }
}
