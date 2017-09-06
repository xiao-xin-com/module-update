package com.xiaoxin.update.helper;

import android.database.Observable;

import com.xiaoxin.update.bean.PatchInfo;
import com.xiaoxin.update.listener.OnPatchListener;

/**
 * Created by liyuanbiao on 2016/11/16.
 */

public class PatchObserver extends Observable<OnPatchListener> implements OnPatchListener {

    public void registerPatchListener(OnPatchListener observer) {
        super.registerObserver(observer);
    }

    public void unregisterPatchListener(OnPatchListener observer) {
        super.unregisterObserver(observer);
    }

    public void unregisterAllPatchListener() {
        super.unregisterAll();
    }

    @Override
    public void onPrepare(PatchInfo patchInfo) {
        synchronized (mObservers) {
            for (int i = mObservers.size() - 1; i >= 0; i--) {
                mObservers.get(i).onPrepare(patchInfo);
            }
        }
    }

    @Override
    public void onComplete(PatchInfo patchInfo) {
        synchronized (mObservers) {
            for (int i = mObservers.size() - 1; i >= 0; i--) {
                mObservers.get(i).onComplete(patchInfo);
            }
        }
    }

    @Override
    public void onError(Exception e) {
        synchronized (mObservers) {
            for (int i = mObservers.size() - 1; i >= 0; i--) {
                mObservers.get(i).onError(e);
            }
        }
    }
}
