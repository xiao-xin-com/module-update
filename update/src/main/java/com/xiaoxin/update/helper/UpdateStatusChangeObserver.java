package com.xiaoxin.update.helper;

import android.database.Observable;

import com.xiaoxin.update.listener.OnUpdateStatusChangeListener;
import com.xiaoxin.update.util.UITask;
import com.xiaoxin.update.util.UpdateLog;

/**
 * Created by liyuanbiao on 2016/11/16.
 */

public class UpdateStatusChangeObserver extends Observable<OnUpdateStatusChangeListener> implements OnUpdateStatusChangeListener {

    @Override
    public void onUpdateStatusChange(final int status) {
        UITask.post(new Runnable() {
            @Override
            public void run() {
                synchronized (mObservers) {
                    UpdateLog.i("onUpdateStatusChange() called with: status = [" + status + "]");
                    CurrentStatus.setStatus(status);
                    for (int i = mObservers.size() - 1; i >= 0; i--) {
                        mObservers.get(i).onUpdateStatusChange(status);
                    }
                }
            }
        });

    }

    public void registerUpdateStatusChangeListener(OnUpdateStatusChangeListener observer) {
        super.registerObserver(observer);
    }

    public void unregisterUpdateStatusChangeListener(OnUpdateStatusChangeListener observer) {
        super.unregisterObserver(observer);
    }

    public void unregisterAllUpdateStatusChangeListener() {
        super.unregisterAll();
    }
}
