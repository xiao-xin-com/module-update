package com.xiaoxin.update.listener;

/**
 * Created by liyuanbiao on 2017/9/7.
 */

public interface OnInstallListener {
    void onStart();
    void onComplete();
    void onError(Throwable e);
}
