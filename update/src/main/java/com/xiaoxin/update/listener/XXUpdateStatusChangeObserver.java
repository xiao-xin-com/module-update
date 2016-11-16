package com.xiaoxin.update.listener;

import android.database.Observable;

/**
 * Created by liyuanbiao on 2016/11/16.
 */

public class XXUpdateStatusChangeObserver extends Observable<XXOnUpdateStatusChangeListener> implements XXOnUpdateStatusChangeListener {
    @Override
    public void onUpdateStatusChange(int status) {
        synchronized (mObservers) {
            for (int i = mObservers.size() - 1; i >= 0; i--) {
                mObservers.get(i).onUpdateStatusChange(status);
            }
        }
    }

    public void registerUpdateStatusChangeListener(XXOnUpdateStatusChangeListener observer) {
        super.registerObserver(observer);
    }

    public void unregisterUpdateStatusChangeListener(XXOnUpdateStatusChangeListener observer) {
        super.unregisterObserver(observer);
    }

    public void unregisterAllUpdateStatusChangeListener() {
        super.unregisterAll();
    }
}
