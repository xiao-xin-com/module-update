package com.xiaoxin.update.helper;

import android.database.Observable;

import com.xiaoxin.update.listener.OnDownloadListener;

/**
 * Created by liyuanbiao on 2016/11/16.
 */

public class DownloadObserver extends Observable<OnDownloadListener> implements OnDownloadListener {
    @Override
    public void onStart(String path) {
        synchronized (mObservers) {
            for (int i = mObservers.size() - 1; i >= 0; i--) {
                mObservers.get(i).onStart(path);
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
    public void onComplete(String path) {
        synchronized (mObservers) {
            for (int i = mObservers.size() - 1; i >= 0; i--) {
                mObservers.get(i).onComplete(path);
            }
        }
    }

    @Override
    public void onError(String path, Throwable e) {
        synchronized (mObservers) {
            for (int i = mObservers.size() - 1; i >= 0; i--) {
                mObservers.get(i).onError(path, e);
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
