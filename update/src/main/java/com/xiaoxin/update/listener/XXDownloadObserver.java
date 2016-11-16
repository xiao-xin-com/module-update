package com.xiaoxin.update.listener;

import android.database.Observable;

/**
 * Created by liyuanbiao on 2016/11/16.
 */

public class XXDownloadObserver extends Observable<XXDownloadListener> implements XXDownloadListener {
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

    public void registerDownloadListener(XXDownloadListener observer) {
        super.registerObserver(observer);
    }

    public void unregisterDownloadListener(XXDownloadListener observer) {
        super.unregisterObserver(observer);
    }

    public void unregisterAllDownloadListener() {
        super.unregisterAll();
    }
}
