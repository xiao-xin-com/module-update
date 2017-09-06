package com.xiaoxin.update.helper;

import android.database.Observable;

import com.xiaoxin.update.listener.OnConnectListener;

/**
 * Created by liyuanbiao on 2016/11/16.
 */

public class ConnectObserver extends Observable<OnConnectListener> implements OnConnectListener {

    public void registerOnConnectListener(OnConnectListener observer) {
        super.registerObserver(observer);
    }

    public void unregisterOnConnectListener(OnConnectListener observer) {
        super.unregisterObserver(observer);
    }

    public void unregisterAllOnConnectListener() {
        super.unregisterAll();
    }

    @Override
    public void onConnected(Exception e) {
        synchronized (mObservers) {
            for (int i = mObservers.size() - 1; i >= 0; i--) {
                mObservers.get(i).onConnected(e);
            }
        }
    }
}
